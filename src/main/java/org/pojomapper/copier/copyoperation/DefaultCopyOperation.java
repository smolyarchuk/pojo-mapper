package org.pojomapper.copier.copyoperation;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.pojomapper.copier.Source;
import org.pojomapper.copier.util.PojoUtil;

/**
 * @author Sergey_Smolyarchuk
 */
public class DefaultCopyOperation implements PerPropertyCopyOperation {

    @SuppressWarnings("unchecked")
    @Override
    public boolean copy(Source source, Object target, String fromProp, String toProp) {
        if (PojoUtil.hasProperty(source.from(), fromProp) && PojoUtil.hasProperty(target, toProp)) {
            Object valueToWrite = PojoUtil.get(source.from(), fromProp);
            if (Objects.isNull(valueToWrite) && source.skipNulls()) {
                return false;
            }
            if (source.rewrite() || Objects.isNull(PojoUtil.get(target, toProp))) {
            	
            	List<Function<Object, Object>> converters = source.converters();
				PojoUtil.set(target, toProp, valueToWrite, converters.toArray(new Function[converters.size()]));

                return true;
            }
        }
        return false;
    }
}
