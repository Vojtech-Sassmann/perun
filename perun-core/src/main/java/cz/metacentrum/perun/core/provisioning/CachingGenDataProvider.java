package cz.metacentrum.perun.core.provisioning;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class CachingGenDataProvider implements GenDataProvider {

	private final PerunSessionImpl sess;
	private final Service service;
	private final Facility facility;

	private final Map<String, List<Attribute>> attributesByHash = new HashMap<>();

	private List<Attribute> facilityAttrs;

	private Map<Member, List<Attribute>> memberResourceAttrs = new HashMap<>();
	private Map<Group, List<Attribute>> groupResourceAttrs = new HashMap<>();
	private Map<Member, List<Attribute>> memberGroupAttrs = new HashMap<>();

	private final Map<Member, List<Attribute>> memberAttrs = new HashMap<>();
	private final Map<Group, List<Attribute>> groupAttrs = new HashMap<>();
	private final Map<User, List<Attribute>> userAttrs = new HashMap<>();
	private final Map<User, List<Attribute>> userFacilityAttrs = new HashMap<>();
	private final Map<Resource, List<Attribute>> resourceAttrs = new HashMap<>();
	private final Map<Integer, List<Attribute>> voAttrs = new HashMap<>();

	private Group lastLoadedGroup;
	private Resource lastLoadedResource;

	private final Map<Integer, User> loadedUsersById = new HashMap<>();
	private final Set<Member> processedMembers = new HashSet<>();
	private final Set<Group> processedGroups = new HashSet<>();

	private final Hasher hasher = new IdHasher();

	public CachingGenDataProvider(PerunSessionImpl sess, Service service, Facility facility) {
		this.sess = sess;
		this.service = service;
		this.facility = facility;
	}

	@Override
	public void loadFacilitySpecificAttributes() {
		facilityAttrs =
				sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility);
	}

	@Override
	public void loadGroupsSpecificAttributes(Resource resource, List<Group> groups) {
		groupResourceAttrs = new HashMap<>();
		// FIXME - attributes could be loaded at once to get a better performance

		for (Group group: groups) {
			try {
				groupResourceAttrs.put(group,
						sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group));
			} catch (GroupResourceMismatchException e) {
				throw new InternalErrorException(e);
			}
		}

		List<Group> notYetProcessedGroups = new ArrayList<>(groups);
		notYetProcessedGroups.removeAll(processedGroups);
		processedGroups.addAll(notYetProcessedGroups);

		groupAttrs.putAll(
			sess.getPerunBl().getAttributesManagerBl().getRequiredAttributesForGroups(sess, service, groups)
		);
	}

	@Override
	public void loadMemberGroupAttributes(Group group, List<Member> members) {
		lastLoadedGroup = group;
		memberGroupAttrs = new HashMap<>();
		try {
			memberGroupAttrs.putAll(
					sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, members, group)
			);
		} catch (MemberGroupMismatchException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void loadResourceSpecificAttributes(Resource resource, List<Member> members, boolean loadVoAttributes) {
		lastLoadedResource = resource;

		if (!resourceAttrs.containsKey(resource)) {
			resourceAttrs.put(resource,
					sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource));
		}

		if (loadVoAttributes) {
			loadVoSpecificAttributes(resource);
		}

		memberResourceAttrs =
				sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource, members);

		// we don't need to load again attributes for the already processed members
		List<Member> notYetProcessedMembers = new ArrayList<>(members);
		notYetProcessedMembers.removeAll(processedMembers);
		processedMembers.addAll(notYetProcessedMembers);

		loadMemberSpecificAttributes(notYetProcessedMembers);
	}

	@Override
	public List<String> getFacilityAttributesHashes() {
		String hash = hasher.hashFacility(facility);

		if (!attributesByHash.containsKey(hash)) {
			if (facilityAttrs == null) {
				throw new IllegalStateException("Facility attributes need to be loaded first.");
			}
			attributesByHash.put(hash, facilityAttrs);
		}

		return attributesByHash.get(hash).isEmpty() ? emptyList() : singletonList(hash);
	}

	@Override
	public List<String> getAllResourceAttributesHashes(Resource resource, boolean addVoAttributes) {
		List<String> hashes = getResourceAttributesHashes(resource);

		if (addVoAttributes) {
			hashes.addAll(getVoAttributesHashes(resource.getVoId()));
		}

		return hashes;
	}

	@Override
	public List<String> getAllMemberSpecificAttributesHashes(Resource resource, Member member) {
		List<String> hashes = new ArrayList<>();

		User user = loadedUsersById.get(member.getUserId());

		hashes.addAll(getMemberAttributesHashes(member));
		hashes.addAll(getUserAttributesHashes(user));
		hashes.addAll(getUserFacilityAttributesHashes(user, facility));
		hashes.addAll(getMemberResourceAttributesHashes(member, resource));

		return hashes;
	}

	@Override
	public List<String> getAllMemberSpecificAttributesHashes(Resource resource, Member member, Group group) {
		List<String> hashes = getAllMemberSpecificAttributesHashes(resource, member);

		hashes.addAll(getMemberGroupAttributesHashes(member, group));

		return hashes;
	}

	@Override
	public List<String> getAllGroupSpecificAttributesHashes(Resource resource, Group group) {
		List<String> hashes = new ArrayList<>();

		hashes.addAll(getGroupAttributesHashes(group));
		hashes.addAll(getGroupResourceAttributesHashes(group, resource));

		return hashes;
	}

	@Override
	public Map<String, List<Attribute>> getAllFetchedAttributes() {
		return attributesByHash.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private List<String> getVoAttributesHashes(int voId) {
		String hash = hasher.hashVo(voId);

		return getAndStoreHash(hash, voId, voAttrs);
	}

	private List<String> getMemberGroupAttributesHashes(Member member, Group group) {
		if (!group.equals(lastLoadedGroup)) {
			throw new IllegalStateException();
		}
		String hash = hasher.hashMemberGroup(member, group);

		return getAndStoreHash(hash, member, memberGroupAttrs);
	}

	private List<String> getMemberResourceAttributesHashes(Member member, Resource resource) {
		if (!resource.equals(lastLoadedResource)) {
			throw new IllegalStateException("The last loaded resource is different than the required one. Required: " +
					resource + ", Last loaded: " + lastLoadedResource);
		}
		String hash = hasher.hashMemberResource(member, resource);

		return getAndStoreHash(hash, member, memberResourceAttrs);
	}

	private List<String> getResourceAttributesHashes(Resource resource) {
		String hash = hasher.hashResource(resource);

		return getAndStoreHash(hash, resource, resourceAttrs);
	}

	private List<String> getMemberAttributesHashes(Member member) {
		String hash = hasher.hashMember(member);

		return getAndStoreHash(hash, member, memberAttrs);
	}


	private List<String> getGroupAttributesHashes(Group group) {
		String hash = hasher.hashGroup(group);

		return getAndStoreHash(hash, group, groupAttrs);
	}

	private List<String> getGroupResourceAttributesHashes(Group group, Resource resource) {
		if (!resource.equals(lastLoadedResource)) {
			throw new IllegalStateException("The last loaded resource is different than the required one. Required: " +
					resource + ", Last loaded: " + lastLoadedResource);
		}

		String hash = hasher.hashGroupResource(group, resource);

		return getAndStoreHash(hash, group, groupResourceAttrs);
	}

	private List<String> getUserAttributesHashes(User user) {
		String hash = hasher.hashUser(user);

		return getAndStoreHash(hash, user, userAttrs);
	}

	private List<String> getUserFacilityAttributesHashes(User user, Facility facility) {
		String hash = hasher.hashUserFacility(user, facility);

		return getAndStoreHash(hash, user, userFacilityAttrs);
	}

	private <T> List<String> getAndStoreHash(String hash, T entity, Map<T, List<Attribute>> map) {
		if (!attributesByHash.containsKey(hash)) {
			if (!map.containsKey(entity)) {
				return emptyList();
			}
			attributesByHash.put(hash, map.get(entity));
		}
		return attributesByHash.get(hash).isEmpty() ? emptyList() : singletonList(hash);
	}

	private void loadMemberSpecificAttributes(List<Member> members) {
		memberAttrs.putAll(
				sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, null, service, members)
		);

		List<Integer> userIds = members.stream()
				.map(Member::getUserId)
				.collect(toList());

		List<User> users = sess.getPerunBl().getUsersManagerBl().getUsersByIds(sess, userIds);

		Map<Integer, User> usersById = users.stream()
				.collect(toMap(User::getId, Function.identity()));
		loadedUsersById.putAll(usersById);

		loadUserSpecificAttributes(users);
	}

	private void loadUserSpecificAttributes(List<User> users) {
		userAttrs.putAll(
				sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, users)
		);

		userFacilityAttrs.putAll(
				sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility, users)
		);
	}

	private void loadVoSpecificAttributes(Resource resource) {
		if (!voAttrs.containsKey(resource.getVoId())) {
			Vo vo;
			try {
				vo = sess.getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId());
			} catch (VoNotExistsException e) {
				throw new InternalErrorException(e);
			}
			voAttrs.put(resource.getVoId(),
					sess.getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, vo));
		}
	}
}
