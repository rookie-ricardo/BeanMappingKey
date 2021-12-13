package org.rookie.plugins.entity;

public class MappingJavaBeanMeta {

    private String classQualifiedName;
    private String classSimpleName;

    public String getClassQualifiedName() {
        return classQualifiedName;
    }

    public void setClassQualifiedName(String classQualifiedName) {
        this.classQualifiedName = classQualifiedName;
    }

    public String getClassSimpleName() {
        return classSimpleName;
    }

    public void setClassSimpleName(String classSimpleName) {
        this.classSimpleName = classSimpleName;
    }

    @Override
    public String toString() {
        return "MappingBeanMeta{" +
                "classQualifiedName='" + classQualifiedName + '\'' +
                ", classSimpleName='" + classSimpleName + '\'' +
                '}';
    }

    public static void main(String[] args) {

    }
}
