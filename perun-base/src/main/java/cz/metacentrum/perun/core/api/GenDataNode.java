package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GenDataNode {

	private final List<String> hashes;
	private final List<GenDataNode> children;
	private final List<GenMemberDataNode> members;

	private GenDataNode(List<String> hashes, List<GenDataNode> children, List<GenMemberDataNode> members) {
		this.hashes = hashes;
		this.children = children;
		this.members = members;
	}

	public List<String> getH() {
		return Collections.unmodifiableList(hashes);
	}

	public List<GenDataNode> getC() {
		return Collections.unmodifiableList(children);
	}

	public List<GenMemberDataNode> getM() {
		return Collections.unmodifiableList(members);
	}

	public static class Builder {

		private List<String> hashes = new ArrayList<>();
		private List<GenDataNode> children = new ArrayList<>();
		private List<GenMemberDataNode> members = new ArrayList<>();

		public Builder hashes(List<String> hashes) {
			this.hashes = hashes;
			return this;
		}

		public Builder children(List<GenDataNode> children) {
			this.children = children;
			return this;
		}

		public Builder members(List<GenMemberDataNode> members) {
			this.members = members;
			return this;
		}

		public GenDataNode build() {
			return new GenDataNode(hashes, children, members);
		}
	}
}
