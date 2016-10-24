package org.pojomapper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public final class PojoMapper {
    
    public static <T> T copy(Object from, T to, String... ignore) {
        return copyTo(to).from(from).ignore(ignore).copy();
    }

    @SafeVarargs
    public static void set(Object obj, String prop, Object value, Function<Object, Object>...converter) {
    	try {
            BeanUtils.setProperty(obj, prop, convert(value, converter));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignore) {
            return;
        }
    }
    
    @SafeVarargs
    private static Object convert(Object value, Function<Object, Object>...converter){
        Object copyValue = value;
        if(ArrayUtils.isNotEmpty(converter)){
            for (Function<Object, Object> function : converter) {
                copyValue = function.apply(copyValue);
            }
        }
        return copyValue;
    }
    
    public static <T> Copier<T> copyTo(T to) {
        return new Copier<>(to);
    }

    public static class Copier<T> {

        private T to;
        private List<Source> sources = new ArrayList<>();
        private List<PerPropertyCopyOperation> copyOperations = new ArrayList<>();
        private Set<String> ignores = new HashSet<>();

        public Copier(T to) {
            this.to = to;
            ignores.add("class");
        }

        private static Object get(Object obj, String prop) {
            Iterable<String> pathes = Splitter.on(".").split(prop);
            Object value = obj;
            for (String path : pathes) {
                value = getSimpleProperty(value, path);
                if (Objects.isNull(value)) {
                    return value;
                }
            }
            return value;
        }

        private static Object getSimpleProperty(Object obj, String prop) {
        	try {
                return BeanUtils.getSimpleProperty(obj, prop);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new UnsupportedOperationException(e);
            } catch (NoSuchMethodException e) {
				return null;
			}
        }

        private static boolean hasProperty(Object obj, String prop) {
            Iterable<String> pathes = Splitter.on(".").split(prop);
            Class<?> propClass = obj.getClass();
            for (String path : pathes) {
            	PropertyDescriptor descr;
				try {
					descr = PropertyUtils.getPropertyDescriptor(obj, path);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	                throw new UnsupportedOperationException(e);
	            } catch (NoSuchMethodException e) {
					return false;
				}
                if (Objects.isNull(descr)) {
                    return false;
                }
                propClass = descr.getPropertyType();
            }
            return true;
        }

        public Copier<T> from(Object from) {
            if (Objects.nonNull(from)) {
                sources.add(new Source(from));
            }
            return this;
        }

        public Copier<T> mapper(String prop1, String prop2) {
            Source last = Iterables.getLast(sources, null);
            if (Objects.nonNull(last)) {
                last.mapper.put(prop1, prop2);
            }
            return this;
        }

        public Copier<T> mapper(Map<String, String> map) {
            Source last = Iterables.getLast(sources, null);
            if (Objects.nonNull(last)) {
                last.mapper.putAll(map);
            }
            return this;
        }

        public Copier<T> converter(Function<Object, Object> converter) {
            Source last = Iterables.getLast(sources, null);
            if (Objects.nonNull(last)) {
                last.converters.add(converter);
            }
            return this;
        }

        public Copier<T> rewrite(boolean rewrite) {
            Source last = Iterables.getLast(sources, null);
            if (Objects.nonNull(last)) {
                last.rewrite = rewrite;
            }
            return this;
        }

        public Copier<T> skipNulls(boolean skipNulls) {
            Source last = Iterables.getLast(sources, null);
            if (Objects.nonNull(last)) {
                last.skipNulls = skipNulls;
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
            copyOperations.add(new IgnoreCopyOperation());
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
            for (Map.Entry<String, String> entry : src.mapper.entrySet()) {
                String fromProp = inverse ? entry.getValue() : entry.getKey();
                String toProp = inverse ? entry.getKey() : entry.getValue();


                copy(src, fromProp, toProp);
            }
        }

        private void copyMatchedProperties(Source src) {
            PropertyDescriptor[] descrs = PropertyUtils.getPropertyDescriptors(src.from.getClass());
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

        public static interface PerPropertyCopyOperation {

            boolean copy(Source source, Object target,
                         String fromProp, String toProp);
        }

        private static class Source {

            private Object from;
            private Map<String, String> mapper = new HashMap<>();
            private List<Function<Object, Object>> converters = new ArrayList<>();
            private boolean rewrite = true;
            private boolean skipNulls;

            public Source(Object from) {
                this.from = from;
            }
        }

        private static class DefaultCopyOperation implements PerPropertyCopyOperation {

            @SuppressWarnings("unchecked")
            @Override
            public boolean copy(Source source, Object target, String fromProp, String toProp) {
                if (hasProperty(source.from, fromProp) && hasProperty(target, toProp)) {
                    Object valueToWrite = get(source.from, fromProp);
                    if (Objects.isNull(valueToWrite) && source.skipNulls) {
                        return false;
                    }
                    if (source.rewrite || Objects.isNull(get(target, toProp))) {
                        set(target, toProp, valueToWrite, Iterables.toArray(source.converters, Function.class));

                        return true;
                    }
                }
                return false;
            }
        }

        private class EnumToStringCopyOperation implements PerPropertyCopyOperation {

            @SuppressWarnings("unchecked")
            @Override
            public boolean copy(Source source, Object target, String fromProp, String toProp) {//NOSONAR
                if (hasProperty(source.from, fromProp) && hasProperty(target, toProp)) {
                    Object valueToWrite = get(source.from, fromProp);
                    if (source.rewrite || Objects.isNull(get(target, toProp))) {
                        PropertyDescriptor fromPropDescr = findPropertyDescriptor(source.from,
                                fromProp);
                        PropertyDescriptor toPropDescr = findPropertyDescriptor(target,
                                toProp);
                        if (fromPropDescr.getPropertyType().isEnum() &&
                                toPropDescr.getPropertyType() == String.class) {
                            set(to, toProp, valueToWrite != null ? valueToWrite.toString() : null);
                            return true;
                        }
                        if (toPropDescr.getPropertyType().isEnum() &&
                                fromPropDescr.getPropertyType() == String.class) {
                            @SuppressWarnings("rawtypes")
                            Class enumClass = toPropDescr.getPropertyType();
                            set(to, toProp, valueToWrite != null ?
                                    Enum.valueOf(enumClass, valueToWrite.toString()) : null);
                            return true;
                        }
                    }
                }
                return false;
            }

            private PropertyDescriptor findPropertyDescriptor(Object obj, String prop) {
                Iterable<String> pathes = Splitter.on(".").split(prop);
                Class<?> propClass = obj.getClass();
                PropertyDescriptor found = null;
                for (String path : pathes) {
                    try {
						found = PropertyUtils.getPropertyDescriptor(obj, path);
					}  catch (Exception e) {
		                throw new UnsupportedOperationException(e);
		            } 
                    if (Objects.nonNull(found)) {
                        propClass = found.getPropertyType();
                    }
                }
                return found;
            }
        }

        private class IgnoreCopyOperation implements PerPropertyCopyOperation {

            @Override
            public boolean copy(Source source, Object target, String fromProp, String toProp) {
                return ignores.contains(fromProp) || ignores.contains(toProp);
            }
        }
    }
}
