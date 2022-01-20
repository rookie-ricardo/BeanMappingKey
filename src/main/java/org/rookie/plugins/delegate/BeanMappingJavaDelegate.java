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
import org.rookie.plugins.utils.JavaClassUtil;
import org.rookie.plugins.utils.TabUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BeanMappingJavaDelegate implements BeanMappingDelegate {

    private static final String INFO_MSG = "BeanMapping : 已复制到粘贴板 (Copied to pasteboard) !";
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

        // process from field and getMethod
        Map<String, String> fromFieldMap = toParamsFieldMap(psiMethod.getParameterList());
        PsiClass to = PsiTypesUtil.getPsiClass(psiMethod.getReturnType());

        StringBuilder context = new StringBuilder();

        if (Arrays.stream(to.getInnerClasses()).anyMatch(c -> c.getName().contains("Builder"))) {
            // builder model
            context.append(TabUtil.getDoubleTabSpace()).append("return ").append(to.getName()).append(".builder()\n");
            Arrays.stream(to.getAllFields()).forEach(f -> {
                String val = fromFieldMap.get(f.getName());
                context.append(TabUtil.getDoubleTabSpace()).append(TabUtil.getDoubleTabSpace())
                        .append(".").append(f.getName()).append("(");
                if (StringUtils.isNotEmpty(val)) {
                    context.append(val);
                }
                context.append(")\n");
            });
            context.append(TabUtil.getDoubleTabSpace()).append(TabUtil.getDoubleTabSpace()).append(".build();");

        } else {
            // set model
            String localVar = String.valueOf(to.getName().charAt(0)).toLowerCase() + to.getName().substring(1);
            context.append(TabUtil.getDoubleTabSpace())
                    .append(to.getName()).append(" ").append(localVar)
                    .append(" = new ").append(to.getName()).append("();\n");
            Arrays.stream(to.getAllFields()).forEach(f -> {
                String val = fromFieldMap.get(f.getName());
                if (StringUtils.isNotEmpty(val)) {
                    context.append(TabUtil.getDoubleTabSpace())
                            .append(localVar).append(".").append("set")
                            .append(String.valueOf(f.getName().charAt(0)).toUpperCase())
                            .append(f.getName().substring(1))
                            .append("(").append(val).append(");\n");
                }
            });
            context.append(TabUtil.getDoubleTabSpace()).append("return ").append(localVar).append(";");
        }

        Document document = FileDocumentManager.getInstance()
                .getDocument(psiMethod.getContainingFile().getVirtualFile());
        WriteCommandAction.runWriteCommandAction(psiMethod.getProject(),
                () -> document.insertString(psiMethod.getBody().getTextOffset() + 1, "\n" + context));
    }

    private HashMap<String, String> toParamsFieldMap(PsiParameterList list) {
        HashMap<String, String> map = new HashMap<>();
        for (PsiParameter parameter : list.getParameters()) {
            if (parameter != null) {
                if (JavaClassUtil.isNotBasicType(parameter.getType().getPresentableText())) {
                    PsiClass from = PsiTypesUtil.getPsiClass(parameter.getType());
                    if (from != null) {
                        HashMap<String, String> temp = new HashMap<>();
                        Arrays.stream(from.getAllFields())
                                .forEach(f -> temp.put(f.getName(), f.getName()));
                        String fromVarName = parameter.getName();
                        Arrays.stream(from.getAllMethods()).forEach(m -> {
                            if (m.getName().startsWith("get")) {
                                temp.keySet().forEach(k -> {
                                    if (m.getName().equalsIgnoreCase(("get" + k))) {
                                        temp.put(k, fromVarName + "." + m.getName() + "()");
                                    }
                                });
                            }
                        });
                        map.putAll(temp);
                    }
                } else {
                    map.put(parameter.getName(), parameter.getName());
                }
            }
        }
        return map;
    }

    private void doPsiClass(PsiClass psiClass, String localVar) {

        if (isNotAvailablePsiClass(psiClass)) {
            return;
        }

        doSetField(psiClass, localVar);

        Arrays.stream(psiClass.getInnerClasses()).forEach(c -> {
            if (c.getName().endsWith("Builder")) {
                doBuildField(psiClass, psiClass.getName());
            }
        });
    }

    private void doBuildField(PsiClass psiClass, String className) {

        StringBuilder context = new StringBuilder();
        context.append("return ").append(className).append(".builder()\n");

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
                context.append(TabUtil.getTabSpace()).append(var).append(".").append(m.getName()).append("();\n");
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
