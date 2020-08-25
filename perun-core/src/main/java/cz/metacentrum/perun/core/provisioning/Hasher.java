package cz.metacentrum.perun.core.provisioning;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface Hasher {
	String hashFacility(Facility facility);
	String hashResource(Resource resource);
	String hashMember(Member member);
	String hashVo(int id);
	String hashGroup(Group group);
	String hashGroupResource(Group group, Resource resource);
	String hashMemberResource(Member member, Resource resource);
	String hashMemberGroup(Member member, Group group);
	String hashUser(User user);
	String hashUserFacility(User user, Facility facility);
}
