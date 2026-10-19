/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class Property
implements Serializable {
    private static final long serialVersionUID = 6618989493749845502L;
    private final String name;
    private final Object value;

    public Property(String name, Object value) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (!(value instanceof String || value instanceof Boolean || value instanceof Number)) {
            throw new IllegalArgumentException("value: " + value.getClass());
        }
        this.name = name;
        this.value = value;
    }

    public Property(String s) {
        int endParamName = s.indexOf(61);
        this.name = s.substring(0, endParamName);
        this.value = Property.valueOf(s.substring(endParamName + 1));
    }

    public final String getName() {
        return this.name;
    }

    public final Object getValue() {
        return this.value;
    }

    private static Object valueOf(String s) {
        try {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE : (s.equalsIgnoreCase("false") ? Boolean.FALSE : s);
        }
    }

    public int hashCode() {
        return 31 * this.name.hashCode() + this.value.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Property other = (Property)obj;
        return this.name.equals(other.name) && this.value.equals(other.value);
    }

    public String toString() {
        return this.name + '=' + this.value;
    }

    public void setAt(Object o) {
        block12: {
            String setterName = "set" + this.name.substring(0, 1).toUpperCase(Locale.ENGLISH) + this.name.substring(1);
            try {
                Class<?> clazz = o.getClass();
                if (this.value instanceof String) {
                    clazz.getMethod(setterName, String.class).invoke(o, this.value);
                    break block12;
                }
                if (this.value instanceof Boolean) {
                    clazz.getMethod(setterName, Boolean.TYPE).invoke(o, this.value);
                    break block12;
                }
                try {
                    clazz.getMethod(setterName, Double.TYPE).invoke(o, ((Number)this.value).doubleValue());
                }
                catch (NoSuchMethodException e) {
                    try {
                        clazz.getMethod(setterName, Float.TYPE).invoke(o, Float.valueOf(((Number)this.value).floatValue()));
                    }
                    catch (NoSuchMethodException e2) {
                        try {
                            clazz.getMethod(setterName, Integer.TYPE).invoke(o, ((Number)this.value).intValue());
                        }
                        catch (NoSuchMethodException e3) {
                            throw e;
                        }
                    }
                }
            }
            catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public static Property[] valueOf(String[] ss) {
        Property[] properties = new Property[ss.length];
        for (int i = 0; i < properties.length; ++i) {
            properties[i] = new Property(ss[i]);
        }
        return properties;
    }

    public static <T> T getFrom(Property[] props, String name, T defVal) {
        for (Property prop : props) {
            if (!prop.name.equals(name)) continue;
            return (T)prop.value;
        }
        return defVal;
    }
}

