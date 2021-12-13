package org.rookie.plugins.enums;

public enum EffectiveFileType {
    JAVA("JAVA");

    private final String type;

    EffectiveFileType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static boolean contains(String fileType) {
        for (EffectiveFileType val : values()) {
            if (fileType.equals(val.getType())) {
                return true;
            }
        }
        return false;
    }

    public static EffectiveFileType get(String fileType) {
        for (EffectiveFileType val : values()) {
            if (fileType.equals(val.getType())) {
                return val;
            }
        }
        return null;
    }
}
