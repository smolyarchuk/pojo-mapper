package org.pojomapper.copier.copyoperation;

import java.beans.PropertyDescriptor;
import java.util.Objects;

import org.apache.commons.beanutils.PropertyUtils;
import org.pojomapper.copier.Source;
import org.pojomapper.copier.util.PojoUtil;

public class EnumToStringCopyOperation implements PerPropertyCopyOperation {

	@SuppressWarnings("unchecked")
	@Override
	public boolean copy(Source source, Object target, String fromProp, String toProp) {
		if (PojoUtil.hasProperty(source.from(), fromProp) && PojoUtil.hasProperty(target, toProp)) {
			Object valueToWrite = PojoUtil.get(source.from(), fromProp);
			if (source.rewrite() || Objects.isNull(PojoUtil.get(target, toProp))) {
				PropertyDescriptor fromPropDescr = findPropertyDescriptor(source.from(), fromProp);
				PropertyDescriptor toPropDescr = findPropertyDescriptor(target, toProp);
				if (fromPropDescr.getPropertyType().isEnum() && toPropDescr.getPropertyType() == String.class) {
					PojoUtil.set(target, toProp, valueToWrite != null ? valueToWrite.toString() : null);
					return true;
				}
				if (toPropDescr.getPropertyType().isEnum() && fromPropDescr.getPropertyType() == String.class) {
					@SuppressWarnings("rawtypes")
					Class enumClass = toPropDescr.getPropertyType();
					PojoUtil.set(target, toProp,
							valueToWrite != null ? Enum.valueOf(enumClass, valueToWrite.toString()) : null);
					return true;
				}
			}
		}
		return false;
	}

	private PropertyDescriptor findPropertyDescriptor(Object obj, String prop) {
		try {
			return PropertyUtils.getPropertyDescriptor(obj, prop);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
}
