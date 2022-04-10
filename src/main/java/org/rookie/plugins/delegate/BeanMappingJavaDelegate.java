package org.rookie.plugins.delegate;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiParameterImpl;
import com.intellij.psi.util.PsiTypesUtil;
import org.apache.commons.lang3.StringUtils;
import org.rookie.plugins.bean.JavaMetaBean;
import org.rookie.plugins.utils.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BeanMappingJavaDelegate implements BeanMappingDelegate {

    private static final String INFO_MSG = "BeanMapping : 已复制到粘贴板 (Copied to pasteboard) !";
    private static final String ERROR_MSG = "BeanMapping : 当前所选对象不支持 (The currently selected object is not supported)";
    private boolean isSuccessful = true;

    @Override
    public void exec(AnActionEvent e) {

        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);

        /**
         * PsiMethod 指选中方法生成代码
         * PsiClass 指选中类生成代码
         * PsiLocalVariable 指选中本地变量生成代码
         * PsiParameterImpl 指选中一个方法中的参数名生成代码
         * 本质上只有 PsiMethod 和 PsiClass 两种，因为只会出现这几种情况，所以使用 if - else 处理
         */
        if (element instanceof PsiMethod) {

            doPsiMethod((PsiMethod) element);

        } else if (element instanceof PsiClass) {

            doPsiClass((PsiClass) element, StringUtils.EMPTY);

        } else if (element instanceof PsiLocalVariable) {

            doPsiLocalVariable((PsiLocalVariable) element);

        } else if (element instanceof PsiParameterImpl) {

            doPsiParameterImpl((PsiParameterImpl) element);

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
        Map<String, JavaMetaBean> fromFieldMap = toParamsFieldMap(psiMethod.getParameterList());
        PsiClass to = PsiTypesUtil.getPsiClass(psiMethod.getReturnType());

        StringBuilder context = new StringBuilder();

        if (Arrays.stream(to.getInnerClasses()).anyMatch(c -> c.getName().contains("Builder"))) {
            // builder model
            context.append(TabUtil.getDoubleTabSpace()).append("return ").append(to.getName()).append(".builder()\n");
            Arrays.stream(to.getAllFields()).forEach(f -> {
                JavaMetaBean val = fromFieldMap.get(f.getName());
                context.append(TabUtil.getDoubleTabSpace()).append(TabUtil.getDoubleTabSpace())
                        .append(".").append(f.getName()).append("(");
                if (!Objects.isNull(val)) {
                    context.append(val.getMethodText());
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
                JavaMetaBean val = fromFieldMap.get(f.getName());
                if (!Objects.isNull(val)) {
                    context.append(TabUtil.getDoubleTabSpace())
                            .append(localVar).append(".").append("set")
                            .append(String.valueOf(f.getName().charAt(0)).toUpperCase())
                            .append(f.getName().substring(1))
                            .append("(").append(val.getMethodText()).append(");\n");
                }
            });
            context.append(TabUtil.getDoubleTabSpace()).append("return ").append(localVar).append(";");
        }

        Document document = FileDocumentManager.getInstance()
                .getDocument(psiMethod.getContainingFile().getVirtualFile());
        WriteCommandAction.runWriteCommandAction(psiMethod.getProject(),
                () -> document.insertString(psiMethod.getBody().getTextOffset() + 1, "\n" + context));
    }

    private HashMap<String, JavaMetaBean> toParamsFieldMap(PsiParameterList list) {
        HashMap<String, JavaMetaBean> map = new HashMap<>();
        for (PsiParameter parameter : list.getParameters()) {
            if (parameter != null) {
                // 非基础数据参数
                if (JavaClassTypeUtil.isEntityClass(parameter.getType())) {
                    PsiClass from = PsiTypesUtil.getPsiClass(parameter.getType());
                    if (from != null) {
                        // 先将实体类的参数作为key写入map
                        HashMap<String, JavaMetaBean> temp = new HashMap<>();
                        Arrays.stream(from.getAllFields())
                                .forEach(f -> temp.put(f.getName(), null));

                        // 填充map的val值
                        String fromVarName = parameter.getName();
                        Arrays.stream(from.getAllMethods()).forEach(m -> {
                            if (m.getName().startsWith("get")) {
                                temp.keySet().forEach(k -> {
                                    if (m.getName().equalsIgnoreCase(("get" + k))) {
                                        JavaMetaBean bean = new JavaMetaBean();
                                        bean.setType(m.getReturnType().getPresentableText());
                                        bean.setMethodText(fromVarName + "." + m.getName() + "()");
                                        temp.put(k, bean);
                                    }
                                });
                            }
                        });
                        map.putAll(temp);
                    }
                } else {
                    JavaMetaBean bean = new JavaMetaBean();
                    bean.setType(parameter.getType().getPresentableText());
                    bean.setMethodText(parameter.getName());
                    map.put(parameter.getName(), bean);
                }
            }
        }
        return map;
    }

    private void doPsiClass(PsiClass psiClass, String localVar) {

        // 基础数据类型 & 枚举类型 处理
        if (Objects.isNull(psiClass)
                || JavaClassTypeUtil.isEnum(psiClass.getName())
                || JavaClassTypeUtil.isBasic(psiClass.getName())
                || JavaClassTypeUtil.isArray(psiClass) || psiClass.isInterface()
                || JavaClassTypeUtil.isList(psiClass) || JavaClassTypeUtil.isMap(psiClass)) {

            // check
            doErrorNotify(psiClass.getProject());
        } else if (JavaClassTypeUtil.isWrap(psiClass)) {

            // 包装类型
            doNewClass(psiClass, localVar);
        } else  {

            // 自定义实体类处理
            doEntityClass(psiClass, localVar);
        }
    }

    private void doNewClass(PsiClass psiClass, String localVar) {

        StringBuilder context = new StringBuilder();

        String name = StringUtils.isBlank(localVar) ? HumpNamingUtil.hump(psiClass.getName()) : localVar;

        context.append(psiClass.getName()).append(" ")
                .append(name)
                .append(" = ").append(psiClass.getName()).append(".valueOf();");

        ClipboardUtil.setClipboard(context.toString());
    }

    private void doEntityClass(PsiClass psiClass, String localVar) {

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

        String name = StringUtils.isBlank(localVar) ? HumpNamingUtil.hump(psiClass.getName()) : localVar;

        context.append(psiClass.getName()).append(" ").append(name).append(" = new ")
                .append(psiClass.getName()).append("();\n");

        Arrays.stream(psiClass.getAllMethods()).forEach(m -> {
            if (m.getName().startsWith("set")) {
                context.append(TabUtil.getTabSpace()).append(name).append(".").append(m.getName()).append("();\n");
            }
        });

        ClipboardUtil.setClipboard(context.toString());
    }

    private void doPsiLocalVariable(PsiLocalVariable psiVariable) {
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiVariable.getType());
        doPsiClass(psiClass, psiVariable.getName());
    }

    private void doPsiParameterImpl(PsiParameterImpl parameter) {
        PsiClass psiClass = PsiTypesUtil.getPsiClass(parameter.getType());
        doPsiClass(psiClass, parameter.getName());
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
