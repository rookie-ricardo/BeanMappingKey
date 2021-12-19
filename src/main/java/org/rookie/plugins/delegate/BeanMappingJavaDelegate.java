package org.rookie.plugins.delegate;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import org.apache.commons.lang3.StringUtils;
import org.rookie.plugins.utils.ClipboardUtil;
import org.rookie.plugins.utils.IntellijNotifyUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class BeanMappingJavaDelegate implements BeanMappingDelegate {

    private static final String INFO_MSG = "BeanMapping : success !";
    private static final String ERROR_MSG = "BeanMapping : 当前所选对象不支持 (The currently selected object is not supported)";
    private boolean isSuccessful = true;

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
        if (isNotAvailablePsiMethod(psiMethod)) {
            return;
        }

        String fromVarName = psiMethod.getParameterList().getParameter(0).getName();
        PsiClass from = PsiTypesUtil.getPsiClass(psiMethod.getParameterList().getParameter(0).getType());
        PsiClass to = PsiTypesUtil.getPsiClass(psiMethod.getReturnType());

        // process from field and getMethod
        Map<String, String> fromFieldMap = Arrays.stream(from.getAllFields())
                .collect(Collectors.toMap(PsiField::getName, PsiField::getName));

        Arrays.stream(from.getAllMethods()).forEach(m -> {
            if (m.getName().startsWith("get")) {
                fromFieldMap.keySet().forEach(k -> {
                    if (m.getName().equalsIgnoreCase(("get" + k))) {
                        fromFieldMap.put(k, m.getName());
                    }
                });
            }
        });

        StringBuilder context = new StringBuilder();

        if (Arrays.stream(to.getInnerClasses()).anyMatch(c -> c.getName().contains("Builder"))) {
            // builder model
            context.append("\t\treturn ").append(to.getName()).append(".builder()\n");
            Arrays.stream(to.getAllFields()).forEach(f -> {
                String val = fromFieldMap.get(f.getName());
                if (StringUtils.isNotEmpty(val)) {
                    context.append("\t\t\t\t.").append(f.getName())
                            .append("(").append(fromVarName).append(".").append(val).append("())\n");
                }
            });
            context.append("\t\t\t\t.build();");

        } else {
            // set model
            String localVar = String.valueOf(to.getName().charAt(0)).toLowerCase() + to.getName().substring(1);
            context.append("\t\t").append(to.getName()).append(" ").append(localVar)
                    .append(" = new ").append(to.getName()).append("();\n");
            Arrays.stream(to.getAllFields()).forEach(f -> {
                String val = fromFieldMap.get(f.getName());
                if (StringUtils.isNotEmpty(val)) {
                    context.append("\t\t").append(localVar).append(".").append("set")
                            .append(String.valueOf(f.getName().charAt(0)).toUpperCase())
                            .append(f.getName().substring(1))
                            .append("(").append(fromVarName).append(".").append(val).append("());\n");
                }
            });
            context.append("\t\treturn ").append(localVar).append(";");
        }

        Document document = FileDocumentManager.getInstance()
                .getDocument(psiMethod.getContainingFile().getVirtualFile());
        WriteCommandAction.runWriteCommandAction(psiMethod.getProject(),
                () -> document.insertString(psiMethod.getBody().getTextOffset() + 1, "\n" + context));
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
            localVar = String.valueOf(psiClass.getName().charAt(0)).toLowerCase() + psiClass.getName().substring(1);
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

    private boolean isNotAvailablePsiMethod(PsiMethod psiMethod) {
        if (psiMethod.getParameterList().isEmpty() || PsiType.VOID.equals(psiMethod.getReturnType())) {
            doErrorNotify(psiMethod.getProject());
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
