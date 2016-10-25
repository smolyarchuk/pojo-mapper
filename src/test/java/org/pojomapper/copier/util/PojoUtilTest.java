package org.pojomapper.copier.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sergey_Smolyarchuk
 */
public class PojoUtilTest {
	
	@Test
    public void setValue() {
        Domain domain = new Domain();
        PojoUtil.set(domain, "prop", "new");
        Assert.assertThat(domain.getProp(), equalTo("new"));
    }
	
	@Test
    public void setReadOnlyProperty() {
        Domain pojo = new Domain();
        PojoUtil.set(pojo, "readOnlyProperty", new Object());
        Assert.assertThat(pojo.getReadOnlyProperty(), nullValue());
    }
	
	public static class Domain {
		
		private String prop;
		private Object readOnlyProperty;

		public String getProp() {
			return prop;
		}
		public void setProp(String prop) {
			this.prop = prop;
		}
		public Object getReadOnlyProperty() {
			return readOnlyProperty;
		}
	}
}
