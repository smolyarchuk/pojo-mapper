package org.pojomapper.copier.util;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author Sergey_Smolyarchuk
 */
public class PojoUtil {

	public static Object get(Object obj, String prop) {
		try {
			return PropertyUtils.getProperty(obj, prop);
		} catch (Exception e) {
			return null;
		}
	}

	@SafeVarargs
	public static void set(Object obj, String prop, Object value, Function<Object, Object>... converter) {
		try {
			PropertyUtils.setProperty(obj, prop, convert(value, converter));
		} catch (Exception ignore) {
			return;
		}
	}

	@SafeVarargs
	public static Object convert(Object value, Function<Object, Object>... converter) {
		Object copyValue = value;
		if (converter.length > 0) {
			for (Function<Object, Object> function : converter) {
				copyValue = function.apply(copyValue);
			}
		}
		return copyValue;
	}

	public static boolean hasProperty(Object obj, String prop) {
		try {
			return Objects.nonNull(PropertyUtils.getPropertyDescriptor(obj, prop));
		} catch (Exception e) {
			return false;
		}
	}
}
