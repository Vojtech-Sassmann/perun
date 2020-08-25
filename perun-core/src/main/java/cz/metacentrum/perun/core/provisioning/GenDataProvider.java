package cz.metacentrum.perun.core.provisioning;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;

import java.util.List;
import java.util.Map;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface GenDataProvider {

	/**
	 * Return all hashes for facility attributes.
	 *
	 * @return list of hashes
	 */
	List<String> getFacilityAttributesHashes();

	/**
	 * Return all hashes for given resource attributes.
	 *
	 * @param resource resource
	 * @return list of hashes
	 */
	List<String> getAllResourceAttributesHashes(Resource resource, boolean addVoAttributes);

	/**
	 * Return all hashes relevant for given member.
	 * Member, User, Member-Resource, User-Facility.
	 *
	 * @param resource resource used to get member-resource attributes hash
	 * @param member given member
	 * @return list of hashes
	 */
	List<String> getAllMemberSpecificAttributesHashes(Resource resource, Member member);

	List<String> getAllMemberSpecificAttributesHashes(Resource resource, Member member, Group group);

	List<String> getAllGroupSpecificAttributesHashes(Resource resource, Group group);

	/**
	 * Loads Facility attributes.
	 */
	void loadFacilitySpecificAttributes();

	/**
	 * Loads Resource and Member specific attributes.
	 * Resouce, Member, User, User-Facility (if not already loaded).
	 * Resource-Member (always)
	 *
	 * @param resource resource
	 * @param members members
	 */
	void loadResourceSpecificAttributes(Resource resource, List<Member> members, boolean loadVoAttributes);

	void loadGroupsSpecificAttributes(Resource resource, List<Group> groups);

	void loadMemberGroupAttributes(Group group, List<Member> members);

	/**
	 * Returns map of all loaded attributes grouped by their hashes.
	 *
	 * @return map of hashes attributes
	 */
	Map<String, List<Attribute>> getAllFetchedAttributes();
}
