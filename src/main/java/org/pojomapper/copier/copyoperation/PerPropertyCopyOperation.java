package org.pojomapper.copier.copyoperation;

import org.pojomapper.copier.Source;

/**
 * @author Sergey_Smolyarchuk
 */
public interface PerPropertyCopyOperation {

	boolean copy(Source source, Object target, String fromProp, String toProp);
}
