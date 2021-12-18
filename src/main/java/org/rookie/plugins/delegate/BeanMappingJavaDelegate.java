package org.rookie.plugins.delegate;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;
import org.rookie.plugins.utils.ClipboardUtil;
import org.rookie.plugins.utils.IntellijNotifyUtil;

import java.util.Arrays;

public class BeanMappingJavaDelegate implements BeanMappingDelegate{

    private boolean isSuccessful = true;

    private static final String INFO_MSG = "BeanMapping : success !";

    private static final String ERROR_MSG = "BeanMapping : 当前所选对象不支持 (The currently selected object is not supported)";

    @Override
    public void exec(AnActionEvent e) {

        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);

        if (element instanceof PsiMethod) {

            doPsiMethod((PsiMethod) element);

        } else if (element instanceof PsiClass) {

            doPsiClass((PsiClass) element, StringUtils.EMPTY);

        } else if (element instanceof PsiLocalVariable) {

            doPsiLocalVariable((PsiLocalVariable) element);

        } else {

            doErrorNotify(e.getProject());
        }

        if (this.isSuccessful) {
            doInfoNotify(e.getProject());
        }
    }

    private void doPsiMethod(PsiMethod psiMethod) {

    }

    private void doPsiClass(PsiClass psiClass, String localVar) {

        if (isNotAvailablePsiClass(psiClass)) {
            return;
        }

        doSetField(psiClass, localVar);

        Arrays.stream(psiClass.getInnerClasses()).forEach(c -> {
            if (c.getName().contains("Builder")) {
                doBuildField(c, psiClass.getName());
            }
        });
    }

    private void doBuildField(PsiClass psiClass, String className) {

        StringBuilder context = new StringBuilder();
        context.append(className).append(".builder()\n");

        Arrays.stream(psiClass.getAllFields()).forEach(f -> context.append(".").append(f.getName()).append("()\n"));

        context.append(".build();");
        ClipboardUtil.setClipboard(context.toString());
    }

    private void doSetField(PsiClass psiClass, String localVar) {

        StringBuilder context = new StringBuilder();
        if (StringUtils.isBlank(localVar)) {
            localVar = psiClass.getName().charAt(0) + psiClass.getName().substring(1);
            context.append(psiClass.getName()).append(" ").append(localVar).append(" = new ")
                    .append(psiClass.getName()).append("();\n");
        }

        String var = localVar;
        Arrays.stream(psiClass.getAllMethods()).forEach(m -> {
            if (m.getName().startsWith("set")) {
                context.append("\t").append(var).append(".").append(m.getName()).append("();\n");
            }
        });

        ClipboardUtil.setClipboard(context.toString());
    }

    private void doPsiLocalVariable(PsiLocalVariable psiVariable) {
        PsiElement element = psiVariable.getParent();
        if (element instanceof PsiClass) {
            doPsiClass((PsiClass) element, psiVariable.getName());
        } else {
            doErrorNotify(psiVariable.getProject());
        }
    }

    private boolean isNotAvailablePsiClass(PsiClass psiClass) {
        if (psiClass.isInterface()) {
            doErrorNotify(psiClass.getProject());
            return true;
        }
        return false;
    }

    private void doInfoNotify(Project project) {
        IntellijNotifyUtil.notifyInfo(project, INFO_MSG);
    }

    private void doErrorNotify(Project project) {
        this.isSuccessful = false;
        IntellijNotifyUtil.notifyError(project, ERROR_MSG);
    }

}
