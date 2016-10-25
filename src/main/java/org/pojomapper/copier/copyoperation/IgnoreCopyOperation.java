package org.pojomapper.copier.copyoperation;

import java.util.HashSet;
import java.util.Set;

import org.pojomapper.copier.Source;

public class IgnoreCopyOperation implements PerPropertyCopyOperation {

	private Set<String> ignores = new HashSet<>();

	public IgnoreCopyOperation(Set<String> ignores) {
		this.ignores = ignores;
	}

	@Override
	public boolean copy(Source source, Object target, String fromProp, String toProp) {
		return ignores.contains(fromProp) || ignores.contains(toProp);
	}
}