package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GenDataNode {

	private final List<String> hashes;
	private final List<GenDataNode> children;
	private final List<GenDataNode> members;

	public GenDataNode(List<String> hashes, List<GenDataNode> children, List<GenDataNode> members) {
		this.hashes = hashes;
		this.children = children;
		this.members = members;
	}

	public void addHashes(Collection<String> hashes) {
		this.hashes.addAll(hashes);
	}

	public void addChildNode(GenDataNode child) {
		this.children.add(child);
	}

	public List<String> getH() {
		return Collections.unmodifiableList(hashes);
	}

	public List<GenDataNode> getC() {
		return Collections.unmodifiableList(children);
	}

	public List<GenDataNode> getM() {
		return Collections.unmodifiableList(members);
	}

	public static class Builder {

		private List<String> hashes = new ArrayList<>();
		private List<GenDataNode> children = new ArrayList<>();
		private List<GenDataNode> members = new ArrayList<>();

		public Builder hashes(List<String> hashes) {
			this.hashes = hashes;
			return this;
		}

		public Builder children(List<GenDataNode> children) {
			this.children = children;
			return this;
		}

		public Builder members(List<GenDataNode> members) {
			this.members = members;
			return this;
		}

		public GenDataNode build() {
			return new GenDataNode(hashes, children, members);
		}
	}
}
