package uk.ac.ed.acp.cw2.data;

import uk.ac.ed.acp.cw2.entity.Drone;

import java.lang.reflect.Method;
import java.math.BigDecimal;

public class DynamicQueries {
    public static Object readProperty(Drone d, String propertyName) {
        Class<?> cls = d.getClass();
        String cap = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

        // try getter methods: getXxx() then isXxx()
        try {
            Method m = cls.getMethod("get" + cap);
            return m.invoke(d);
        } catch (Exception ignored) {}
        try {
            Method m = cls.getMethod("is" + cap);
            return m.invoke(d);
        } catch (Exception ignored) {}
        return null;
    }

    public static boolean attributeEquals(Object attrVal, String stringValue) {
        if (attrVal instanceof String) {
            return attrVal.equals(stringValue);
        }

        if (attrVal instanceof Boolean) {
            return Boolean.valueOf(stringValue).equals(attrVal);
        }

        if (attrVal instanceof Number) {
            BigDecimal a = new BigDecimal(attrVal.toString());
            BigDecimal b = new BigDecimal(stringValue);
            return a.compareTo(b) == 0;
        }

        return false;
    }

    public static boolean attributeLessThan(Object attrVal, String stringValue) {
        if (attrVal instanceof Number) {
            BigDecimal a = new BigDecimal(attrVal.toString());
            BigDecimal b = new BigDecimal(stringValue);
            return a.compareTo(b) < 0;
        }
        return false;
    }

    public static boolean attributeGreaterThan(Object attrVal, String stringValue) {
        if (attrVal instanceof Number) {
            BigDecimal a = new BigDecimal(attrVal.toString());
            BigDecimal b = new BigDecimal(stringValue);
            return a.compareTo(b) > 0;
        }
        return false;
    }
}
