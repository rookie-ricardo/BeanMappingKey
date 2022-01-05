package org.rookie.plugins.utils;

public class JavaClassUtil {

    public static final String[] BASIC_TYPE = new String[]{
            "Boolean", "Byte", "Character", "Double", "Float", "Short", "Long", "Integer",
            "String", "boolean", "byte", "char", "double", "float", "short", "int", "long"};

    public static boolean isBasicType(String type) {
        for (String str : BASIC_TYPE) {
            if (str.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotBasicType(String type) {
        return !isBasicType(type);
    }
}
