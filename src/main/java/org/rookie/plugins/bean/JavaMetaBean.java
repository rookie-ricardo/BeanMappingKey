package org.rookie.plugins.bean;

import com.intellij.psi.PsiType;

public class JavaMetaBean {

    private PsiType type;

    private String methodText;

    public PsiType getType() {
        return type;
    }

    public void setType(PsiType type) {
        this.type = type;
    }

    public String getMethodText() {
        return methodText;
    }

    public void setMethodText(String methodText) {
        this.methodText = methodText;
    }

    @Override
    public String toString() {
        return "JavaMetaBean{" +
                "type=" + type.getPresentableText() +
                ", methodText='" + methodText + '\'' +
                '}';
    }
}
