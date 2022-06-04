package org.rookie.plugins.utils;

import org.apache.commons.lang3.StringUtils;

public class HumpNamingUtil {

    public static String hump(String name) {
        if (StringUtils.isBlank(name)) {
            return "humpName";
        }
        return String.valueOf(name.charAt(0)).toLowerCase() + name.substring(1);
    }

    public static String humpFirstUp(String name) {
        if (StringUtils.isBlank(name)) {
            return "HumpName";
        }
        return String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
    }
}
