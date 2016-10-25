package org.pojomapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;

public class PojoMapperTest {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    @Test
    public void copyDomainToDto() {
        Domain domain = setRandomPropertyValues(new Domain());

        Dto dto = PojoMapper.copyTo(new Dto()).from(domain).copy();

        Assert.assertThat(dto.getExtraProp(), equalTo(domain.getExtraProp()));
        Assert.assertThat(dto.getEnumProp(), equalTo(domain.getEnumProp().toString()));
    }

    @Test
    public void copyDomainToDtoEnumIsNull() {
        Domain domain = setRandomPropertyValues(new Domain());
        domain.setEnumProp(null);

        Dto dto = PojoMapper.copyTo(new Dto()).from(domain).copy();

        Assert.assertThat(dto.getExtraProp(), equalTo(domain.getExtraProp()));
        Assert.assertThat(dto.getEnumProp(), nullValue());
    }

    @Test
    public void copyDtoToDomainEnumIsNull() {
        Dto dto = setRandomPropertyValues(new Dto());
        dto.setEnumProp(null);

        Domain domain = PojoMapper.copyTo(new Domain()).from(dto).copy();

        Assert.assertThat(domain.getExtraProp(), equalTo(dto.getExtraProp()));
        Assert.assertThat(domain.getEnumProp(), nullValue());
    }

    @Test
    public void copyDtoToDomain() {
        Dto dto = setRandomPropertyValues(new Dto());
        dto.setEnumProp(Domain.Enum.SECOND.toString());

        Domain domain = PojoMapper.copyTo(new Domain()).from(dto).copy();

        Assert.assertThat(domain.getExtraProp(), equalTo(dto.getExtraProp()));
        Assert.assertThat(String.valueOf(domain.getEnumProp()), equalTo(dto.getEnumProp()));
    }

    @Test
    public void copyNullToDomain() {
        Domain domain = PojoMapper.copyTo(new Domain()).from(null).copy();
        Assert.assertNull(domain);
    }

    @Test
    public void copyDomainToNull() {
        Dto dto = (Dto) PojoMapper.copyTo(null).from(new Domain()).copy();
        Assert.assertThat(dto, nullValue());
    }

    @Test
    public void copyMappedProperties() {
        Domain domain = setRandomPropertyValues(new Domain());
        domain.setNestedProp(setRandomPropertyValues(new Domain.NestedProp()));

        Map<String, String> mapper = new HashMap<>();
        mapper.put("notMatchedProp1", "notMatchedProp3");
        mapper.put("notMatchedProp4", "notMatchedProp2");
        mapper.put("nestedProp.prop1", "notMatchedProp5");
        Dto dto = PojoMapper.copyTo(new Dto()).from(domain).rewrite(rewrite).mapper("prop1", "prop2").ignore("ignorableProperty").mapper(mapper).copy();
        
        SomeTargetPojo resuelt = PojoMapper.copyTo(new SomeTargetPojo())
        		.from(firstSource)
        		.ignore("ignorableProperty")
        		.
        		.mapper(mapper).copy();

        Assert.assertThat(dto.getNotMatchedProp3(), equalTo(domain.getNotMatchedProp1()));
        Assert.assertThat(dto.getNotMatchedProp4(), equalTo(domain.getNotMatchedProp2()));
        Assert.assertThat(dto.getNotMatchedProp5(), equalTo(domain.getNestedProp().getProp1()));
    }

    @Test
    public void copyMappedPropertiesInverseOrder() {
        Domain domain = setRandomPropertyValues(new Domain());
        domain.setNestedProp(setRandomPropertyValues(new Domain.NestedProp()));

        Map<String, String> mapper = new HashMap<>();
        mapper.put("notMatchedProp1", "notMatchedProp3");
        mapper.put("notMatchedProp4", "notMatchedProp2");
        mapper.put("nestedProp.prop1", "notMatchedProp5");

        mapper.put("notMatchedProp3", "notMatchedProp1");
        mapper.put("notMatchedProp2", "notMatchedProp4");
        mapper.put("notMatchedProp5", "nestedProp.prop1");
        Dto dto = PojoMapper.copyTo(new Dto()).from(domain).mapper(mapper).copy();

        Assert.assertThat(dto.getNotMatchedProp3(), equalTo(domain.getNotMatchedProp1()));
        Assert.assertThat(dto.getNotMatchedProp4(), equalTo(domain.getNotMatchedProp2()));
        Assert.assertThat(dto.getNotMatchedProp5(), equalTo(domain.getNestedProp().getProp1()));
    }
    
    @Test
    public void copyMappedPropertiesMixedWithAlternateMapping() {
        Domain domain = setRandomPropertyValues(new Domain());
        domain.setNestedProp(setRandomPropertyValues(new Domain.NestedProp()));

        Map<String, String> mapper = new HashMap<>();
        mapper.put("notMatchedProp1", "notMatchedProp3");
        
        Dto dto = PojoMapper.copyTo(new Dto()).from(domain).
                mapper(mapper).
                mapper("notMatchedProp4", "notMatchedProp2").
                mapper("nestedProp.prop1", "notMatchedProp5").
                mapper("notMatchedProp3", "notMatchedProp1").
                mapper("notMatchedProp2", "notMatchedProp4").
                mapper("notMatchedProp5", "nestedProp.prop1").
        copy();

        Assert.assertThat(dto.getNotMatchedProp3(), equalTo(domain.getNotMatchedProp1()));
        Assert.assertThat(dto.getNotMatchedProp4(), equalTo(domain.getNotMatchedProp2()));
        Assert.assertThat(dto.getNotMatchedProp5(), equalTo(domain.getNestedProp().getProp1()));
    }

    @Test
    public void copyForceRewrite() {
        Domain domain = setRandomPropertyValues(new Domain());

        Map<String, String> mapper = new HashMap<>();
        mapper.put("notMatchedProp1", "notMatchedProp3");

        Dto to = new Dto();
        to.setNotMatchedProp3("current");
        domain.setNotMatchedProp1("ovverriden");
        Dto dto = PojoMapper.copyTo(to).from(domain).mapper(mapper)/*.rewrite(true)*/.copy();

        Assert.assertThat(dto.getNotMatchedProp3(), equalTo("ovverriden"));
    }

    @Test
    public void copySkipRewrite() {
        Domain domain = setRandomPropertyValues(new Domain());

        Map<String, String> mapper = new HashMap<>();
        mapper.put("notMatchedProp1", "notMatchedProp3");

        Dto to = new Dto();
        to.setNotMatchedProp3("current");
        domain.setNotMatchedProp1("ovverriden");
        Dto dto = PojoMapper.copyTo(to).from(domain).mapper(mapper).rewrite(false).copy();

        Assert.assertThat(dto.getNotMatchedProp3(), equalTo("current"));
    }

    @Test
    public void copySkipNulls() {
        Domain domain = setRandomPropertyValues(new Domain());

        domain.setExtraProp(null);

        Dto to = new Dto();
        to.setExtraProp("nonnull");
        Dto dto = PojoMapper.copyTo(to).from(domain).skipNulls(true).copy();

        Assert.assertThat(dto.getExtraProp(), equalTo("nonnull"));
    }

    @Test
    public void copyRewriteToNulls() {
        Domain domain = setRandomPropertyValues(new Domain());

        domain.setExtraProp(null);

        Dto to = new Dto();
        to.setExtraProp("nonnull");
        Dto dto = PojoMapper.copyTo(to).from(domain)/*.skipNulls(false)*/.copy();

        Assert.assertThat(dto.getExtraProp(), nullValue());
    }
    
    @Test
    public void mapperWithoutSource() {
        Dto to = new Dto();
        Dto dto = PojoMapper.copyTo(to).mapper("notExisting1", "notExisting1").copy();
        Assert.assertThat(dto, nullValue());
    }
    
    public static class TestingIllegalAccessException {
        public void setIllegalAccessException(Object illegalAccessException) throws IllegalAccessException {
            throw new IllegalAccessException();
        }
    }

    public static class Domain {

        private String extraProp;
        private Enum enumProp;
        private String notMatchedProp1;
        private String notMatchedProp2;
        private NestedProp nestedProp;
        private LocalDate dateDomain;
        private Object readOnlyProperty;

        public String getExtraProp() {
            return extraProp;
        }

        public void setExtraProp(String extraProp) {
            this.extraProp = extraProp;
        }

        public Enum getEnumProp() {
            return enumProp;
        }

        public void setEnumProp(Enum enumProp) {
            this.enumProp = enumProp;
        }

        public String getNotMatchedProp1() {
            return notMatchedProp1;
        }

        public void setNotMatchedProp1(String notMatchedProp1) {
            this.notMatchedProp1 = notMatchedProp1;
        }

        public String getNotMatchedProp2() {
            return notMatchedProp2;
        }

        public void setNotMatchedProp2(String notMatchedProp2) {
            this.notMatchedProp2 = notMatchedProp2;
        }

        public NestedProp getNestedProp() {
            return nestedProp;
        }

        public void setNestedProp(NestedProp nestedProp) {
            this.nestedProp = nestedProp;
        }

        public LocalDate getDateDomain() {
            return dateDomain;
        }

        public void setDateDomain(LocalDate dateDomain) {
            this.dateDomain = dateDomain;
        }
        
        public Object getReadOnlyProperty() {
            return readOnlyProperty;
        }
        
        public enum Enum {
            FIRST,
            SECOND
        }

        public static class CompositeId {
            private BigInteger firstProp;
            private Long secondProp;

            public BigInteger getFirstProp() {
                return firstProp;
            }

            public void setFirstProp(BigInteger firstProp) {
                this.firstProp = firstProp;
            }

            public Long getSecondProp() {
                return secondProp;
            }

            public void setSecondProp(Long secondProp) {
                this.secondProp = secondProp;
            }
        }

        public static class NestedProp {
            private String prop1;

            public String getProp1() {
                return prop1;
            }

            public void setProp1(String prop1) {
                this.prop1 = prop1;
            }
        }
    }

    public static class Dto {

        private BigInteger firstProp;
        private Long secondProp;
        private String extraProp;
        private String enumProp;
        private String notMatchedProp3;
        private String notMatchedProp4;
        private String notMatchedProp5;
        private Integer date;

        public BigInteger getFirstProp() {
            return firstProp;
        }

        public void setFirstProp(BigInteger firstProp) {
            this.firstProp = firstProp;
        }

        public Long getSecondProp() {
            return secondProp;
        }

        public void setSecondProp(Long secondProp) {
            this.secondProp = secondProp;
        }

        public String getExtraProp() {
            return extraProp;
        }

        public void setExtraProp(String extraProp) {
            this.extraProp = extraProp;
        }

        public String getEnumProp() {
            return enumProp;
        }

        public void setEnumProp(String enumProp) {
            this.enumProp = enumProp;
        }

        public String getNotMatchedProp3() {
            return notMatchedProp3;
        }

        public void setNotMatchedProp3(String notMatchedProp3) {
            this.notMatchedProp3 = notMatchedProp3;
        }

        public String getNotMatchedProp4() {
            return notMatchedProp4;
        }

        public void setNotMatchedProp4(String notMatchedProp4) {
            this.notMatchedProp4 = notMatchedProp4;
        }

        public String getNotMatchedProp5() {
            return notMatchedProp5;
        }

        public void setNotMatchedProp5(String notMatchedProp5) {
            this.notMatchedProp5 = notMatchedProp5;
        }

        public Integer getDate() {
            return date;
        }

        public void setDate(Integer date) {
            this.date = date;
        }
    }
    
    public static <T> T setRandomPropertyValues(T entity) {
        PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(entity);
        for (PropertyDescriptor prop : props) {
            Class<?> type = prop.getPropertyType();
            String name = prop.getName();
            if ("class".equals(name)) {
                continue;
            }
            Object value;
            if ("boolean".equals(type.getName()) || type == Boolean.class) {
                value = RANDOM.nextBoolean();
            } else if ("int".equals(type.getName()) || type == Integer.class) {
                value = RANDOM.nextInt();
            } else if (type == String.class) {
                value = UUID.randomUUID().toString();
            } else if ("long".equals(type.getName()) || type == Long.class) {
                value = RANDOM.nextLong();
            } else if (type == BigDecimal.class) {
                value = BigDecimal.valueOf(RANDOM.nextDouble());
            } else if (type == BigInteger.class) {
                value = BigInteger.valueOf(RANDOM.nextLong());
            } else if (type == LocalDate.class) {
                value = LocalDate.of(RANDOM.nextInt(3000), 1 + RANDOM.nextInt(11), 1 + RANDOM.nextInt(27));
            } else if (type.isEnum()) {
                Object[] constants = type.getEnumConstants();
                value = constants[RANDOM.nextInt(constants.length)];
            } else if (!type.isPrimitive() && !type.isInterface()) {
                try {
					value = type.newInstance();
				} catch (Exception e) {
					continue;
				}
            } else {
                continue;
            }

            try {
                prop.getWriteMethod().invoke(entity, value);
            } catch (Exception ignore) {
                
            }
        }
        return entity;
    }


}