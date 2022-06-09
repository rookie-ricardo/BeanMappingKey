package org.rookie.plugins.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import org.apache.commons.lang3.StringUtils;

public class JavaClassTypeUtil {

    public static final String[] BASIC_TYPE = new String[]{
            "boolean", "byte", "char", "double", "float", "short", "int", "long"};

    public static final String[] WRAP_TYPE = new String[]{
            "java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Double",
            "java.lang.Float", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.String"};

    public static final String[] LIST_TYPE = new String[]{"java.util.List", "java.util.Collection"};

    public static final String[] MAP_TYPE = new String[]{"java.util.Map"};

    public static boolean isBasic(String type) {
        for (String str : BASIC_TYPE) {
            if (str.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotBasic(String type) {
        return !isBasic(type);
    }

    public static boolean isEnum(String type) {
        return "enum".equalsIgnoreCase(type);
    }

    public static boolean isOptional(String type) {
        return "Optional".equalsIgnoreCase(type);
    }

    public static boolean isArray(PsiClass psiClass) {
        return isArray(psiClass.getName());
    }

    public static boolean isArray(String type) {
        if (StringUtils.isBlank(type)) {
            return false;
        }
        return type.endsWith("[]");
    }

    public static boolean isWrap(PsiClass psiClass) {
        return isWrap(psiClass.getQualifiedName());
    }

    public static boolean isWrap(String type) {
        for (String str : WRAP_TYPE) {
            if (str.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isList(PsiClass psiClass) {
        if (psiClass.isInterface()) {
            for (String str : LIST_TYPE) {
                if (str.equalsIgnoreCase(psiClass.getQualifiedName())) {
                    return true;
                }
            }
        } else if (psiClass.getImplementsList() != null) {
            for (PsiJavaCodeReferenceElement type : psiClass.getImplementsList().getReferenceElements()) {
                for (String str : LIST_TYPE) {
                    if (type.getQualifiedName().contains(str)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isMap(PsiClass psiClass) {
        if (psiClass.isInterface()) {
            for (String str : MAP_TYPE) {
                if (str.equalsIgnoreCase(psiClass.getQualifiedName())) {
                    return true;
                }
            }
        } else if (psiClass.getImplementsList() != null) {
            for (PsiJavaCodeReferenceElement type : psiClass.getImplementsList().getReferenceElements()) {
                for (String str : MAP_TYPE) {
                    if (type.getQualifiedName().contains(str)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isEntityClass(PsiType psiType) {
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiType);
        assert psiClass != null;
        return isEntityClass(psiClass);
    }

    public static boolean isEntityClass(PsiClass psiClass) {
        if (isBasic(psiClass.getName()) || isEnum(psiClass.getName()) || isWrap(psiClass)
                || isArray(psiClass) || isList(psiClass) || isMap(psiClass) || isOptional(psiClass.getName())) {
            return false;
        }
        return true;
    }
}
