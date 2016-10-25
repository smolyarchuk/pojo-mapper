package org.pojomapper.copier;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.pojomapper.copier.copyoperation.DefaultCopyOperation;
import org.pojomapper.copier.copyoperation.EnumToStringCopyOperation;
import org.pojomapper.copier.copyoperation.IgnoreCopyOperation;
import org.pojomapper.copier.copyoperation.PerPropertyCopyOperation;

/**
 * @author Sergey_Smolyarchuk
 */
public class Copier<T> {

	private T to;
	private List<Source> sources = new ArrayList<>();
	private List<PerPropertyCopyOperation> copyOperations = new ArrayList<>();
	private Set<String> ignores = new HashSet<>();

	public Copier(T to) {
		this.to = to;
		ignores.add("class");
	}

	public Copier<T> from(Object from) {
		if (Objects.nonNull(from)) {
			sources.add(new Source(from));
		}
		return this;
	}

	public Copier<T> mapper(String prop1, String prop2) {
		Source last = getLast(sources, null);
		if (Objects.nonNull(last)) {
			last.mapper().put(prop1, prop2);
		}
		return this;
	}

	public Copier<T> mapper(Map<String, String> map) {
		Source last = getLast(sources, null);
		if (Objects.nonNull(last)) {
			last.mapper().putAll(map);
		}
		return this;
	}

	public Copier<T> converter(Function<Object, Object> converter) {
		Source last = getLast(sources, null);
		if (Objects.nonNull(last)) {
			last.converters().add(converter);
		}
		return this;
	}

	public Copier<T> rewrite(boolean rewrite) {
		Source last = getLast(sources, null);
		if (Objects.nonNull(last)) {
			last.rewrite(rewrite);
		}
		return this;
	}

	public Copier<T> skipNulls(boolean skipNulls) {
		Source last = getLast(sources, null);
		if (Objects.nonNull(last)) {
			last.skipNulls(skipNulls);
		}
		return this;
	}

	public Copier<T> ignore(String... ignore) {
		if (Objects.nonNull(ignore)) {
			ignores.addAll(Arrays.asList(ignore));
		}
		return this;
	}

	public T copy() {
		if (Objects.isNull(to) || sources.isEmpty()) {
			return null;
		}
		copyOperations.add(new IgnoreCopyOperation(ignores));
		copyOperations.add(new EnumToStringCopyOperation());
		copyOperations.add(new DefaultCopyOperation());

		for (Source src : sources) {
			copyMatchedProperties(src);
			copyMappedProperties(src, false);
			copyMappedProperties(src, true);
		}
		return to;
	}

	private void copyMappedProperties(Source src, boolean inverse) {
		for (Map.Entry<String, String> entry : src.mapper().entrySet()) {
			String fromProp = inverse ? entry.getValue() : entry.getKey();
			String toProp = inverse ? entry.getKey() : entry.getValue();

			copy(src, fromProp, toProp);
		}
	}

	private void copyMatchedProperties(Source src) {
		PropertyDescriptor[] descrs = PropertyUtils.getPropertyDescriptors(src.from().getClass());
		for (PropertyDescriptor descr : descrs) {
			copy(src, descr.getName());
		}
	}

	private void copy(Source src, String prop) {
		copy(src, prop, prop);
	}

	private void copy(Source src, String fromProp, String toProp) {
		for (PerPropertyCopyOperation operation : copyOperations) {
			if (operation.copy(src, to, fromProp, toProp)) {
				return;
			}
		}
	}

	private static <T> T getLast(List<? extends T> items, T defaultValue) {
		return CollectionUtils.isEmpty(items) ? defaultValue : items.get(items.size() - 1);
	}
}
