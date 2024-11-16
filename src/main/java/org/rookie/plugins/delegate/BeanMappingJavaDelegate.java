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

import java.util.*;

public class BeanMappingJavaDelegate implements BeanMappingDelegate {

    private static final String INFO_MSG = "BeanMapping : 已复制到粘贴板 (Copied to pasteboard) !";
    private static final String ERROR_MSG = "BeanMapping : 当前所选对象不支持 (The currently selected object is not supported)";
    private boolean isSuccessful = true;
    private IntellijNotifyUtil notifyUtil = new IntellijNotifyUtil();

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
        PsiClass returnClass = PsiTypesUtil.getPsiClass(psiMethod.getReturnType());

        if (returnClass == null) {
            doErrorNotify(psiMethod.getProject());
            return;
        }

        StringBuilder context = new StringBuilder();

        if (Arrays.stream(returnClass.getInnerClasses())
                .anyMatch(c -> c.getName() != null && c.getName().contains("Builder"))) {

            // builder model
            context.append(TabUtil.getDoubleTabSpace()).append("return ").append(returnClass.getName()).append(".builder()\n");
            Arrays.stream(returnClass.getAllFields()).forEach(f -> {

                context.append(TabUtil.getDoubleTabSpace()).append(TabUtil.getDoubleTabSpace())
                        .append(".").append(f.getName()).append("(");
                PsiClass parameterClass = PsiTypesUtil.getPsiClass(f.getType());

                if (parameterClass != null && JavaClassTypeUtil.isEntityClass(parameterClass)) {
                    context.append(parameterClass.getName()).append(".builder()\n");

                    Arrays.stream(parameterClass.getAllFields()).forEach(subF -> {
                        context.append(TabUtil.getDoubleTabSpace())
                                .append(TabUtil.getDoubleTabSpace())
                                .append(TabUtil.getDoubleTabSpace())
                                .append(".").append(subF.getName()).append("(");
                        JavaMetaBean val = fromFieldMap.get(f.getName() + ".get" + HumpNamingUtil.humpFirstUp(subF.getName()));
                        if (!Objects.isNull(val)) {
                            context.append(val.getMethodText());
                        }
                        context.append(")\n");
                    });
                    context.append(TabUtil.getDoubleTabSpace())
                            .append(TabUtil.getDoubleTabSpace())
                            .append(TabUtil.getDoubleTabSpace()).append(".build()");
                } else {
                    JavaMetaBean val = fromFieldMap.get(f.getName());
                    if (!Objects.isNull(val)) {
                        context.append(val.getMethodText());
                    }
                }
                context.append(")\n");
            });

            context.append(TabUtil.getDoubleTabSpace()).append(TabUtil.getDoubleTabSpace()).append(".build();");
        } else {
            // set model
            String localVar = HumpNamingUtil.hump(returnClass.getName());
            context.append(TabUtil.getDoubleTabSpace())
                    .append(returnClass.getName()).append(" ").append(localVar)
                    .append(" = new ").append(returnClass.getName()).append("();\n");

            Arrays.stream(returnClass.getAllFields()).forEach(f -> {

                PsiClass parameterClass = PsiTypesUtil.getPsiClass(f.getType());

                // 嵌套实体
                if (parameterClass != null && JavaClassTypeUtil.isEntityClass(parameterClass)) {

                    context.append(TabUtil.getDoubleTabSpace())
                            .append(parameterClass.getName()).append(" ").append(f.getName())
                            .append(" = new ").append(parameterClass.getName()).append("();\n");

                    Arrays.stream(parameterClass.getAllFields()).forEach(subF -> {
                        context.append(TabUtil.getDoubleTabSpace())
                                .append(f.getName()).append(".").append("set")
                                .append(HumpNamingUtil.humpFirstUp(subF.getName()))
                                .append("(");
                        JavaMetaBean val = fromFieldMap.get(f.getName() + ".get" + HumpNamingUtil.humpFirstUp(subF.getName()));
                        if (!Objects.isNull(val)) {
                            context.append(val.getMethodText());
                        }

                        context.append(");\n");
                    });
                    context.append(TabUtil.getDoubleTabSpace())
                            .append(localVar).append(".").append("set")
                            .append(HumpNamingUtil.humpFirstUp(f.getName()))
                            .append("(")
                            .append(f.getName())
                            .append(");\n");
                } else {
                    context.append(TabUtil.getDoubleTabSpace())
                            .append(localVar).append(".").append("set")
                            .append(HumpNamingUtil.humpFirstUp(f.getName()))
                            .append("(");
                    JavaMetaBean val = fromFieldMap.get(f.getName());
                    if (!Objects.isNull(val)) {
                        context.append(val.getMethodText());
                    }
                    context.append(");\n");
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
            if (parameter == null) {
                continue;
            }
            map.putAll(recursionToJavaMetaBean(parameter.getName(),"", parameter.getType()));
        }
        return map;
    }

    private HashMap<String, JavaMetaBean> recursionToJavaMetaBean(String parameterName, String prefix, PsiType psiType) {
        HashMap<String, JavaMetaBean> map = new HashMap<>();

        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiType);
        if (psiClass == null) return  map;

        // 非基础类型参数
        if (JavaClassTypeUtil.isEntityClass(psiClass)) {

            Arrays.stream(psiClass.getMethods())
                    .filter(m -> m.getName().startsWith("get"))
                    .forEach(m -> {
                        PsiClass parameterClass = PsiTypesUtil.getPsiClass(m.getReturnType());

                        if (parameterClass != null && JavaClassTypeUtil.isEntityClass(parameterClass)) {
                            Arrays.stream(parameterClass.getAllFields()).forEach(f -> {
                                map.putAll(recursionToJavaMetaBean(
                                        HumpNamingUtil.hump(m.getName().replace("get", ""))
                                                + ".get" + HumpNamingUtil.humpFirstUp(f.getName()),
                                        parameterName + "." + m.getName() + "()", f.getType()));
                            });

                        } else {
                            JavaMetaBean bean = new JavaMetaBean();
                            bean.setType(m.getReturnType());
                            bean.setMethodText(parameterName + "." + m.getName() + "()");
                            map.put(HumpNamingUtil.hump(m.getName().replace("get", "")), bean);
                        }
                    });
        } else {
            JavaMetaBean bean = new JavaMetaBean();
            bean.setType(psiType);
            if (StringUtils.isNotEmpty(prefix)) {
                String[] split = parameterName.split("\\.");
                bean.setMethodText(prefix + "." + split[split.length - 1]  + "()");
            } else {
                bean.setMethodText(parameterName);
            }
            map.put(parameterName, bean);
        }
        return map;
    }

    private void doPsiClass(PsiClass psiClass, String localVar) {

        // 基础数据类型 & 枚举类型 处理
        if (Objects.isNull(psiClass) || !JavaClassTypeUtil.isEntityClass(psiClass)) {

            // check
            doErrorNotify(psiClass.getProject());
        } else {

            // 自定义实体类处理
            ClipboardUtil.setClipboard(doEntityClass(psiClass, localVar));
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

    private String doEntityClass(PsiClass psiClass, String localVar) {
        if (psiClass.getInnerClasses().length == 0) {
            return doSetField(psiClass, localVar);
        } else {
            Optional<PsiClass> processClass = Arrays.stream(psiClass.getInnerClasses())
                    .filter(i -> i.getName() != null && i.getName().endsWith("Builder"))
                    .findFirst();

            if (processClass.isPresent()) return doBuildField(psiClass, psiClass.getName());
        }
        return doSetField(psiClass, localVar);
    }

    private String doBuildField(PsiClass psiClass, String className) {

        StringBuilder context = new StringBuilder();

        context.append("return ").append(className).append(".builder()\n");

        Arrays.stream(psiClass.getAllFields()).forEach(f -> {
            PsiClass parameterClass = PsiTypesUtil.getPsiClass(f.getType());
            if (parameterClass != null && JavaClassTypeUtil.isEntityClass(parameterClass)) {
                context.append(".").append(f.getName()).append("(")
                        .append(doBuildField(parameterClass, parameterClass.getName())
                                .replace("return ", "").replace(";", ""))
                        .append("\n").append(")\n");
            } else {
                context.append(".").append(f.getName()).append("()\n");
            }
        });

        context.append(".build();");

        return context.toString();
    }

    private String doSetField(PsiClass psiClass, String localVar) {

        StringBuilder context = new StringBuilder();

        String name = StringUtils.isBlank(localVar) ? HumpNamingUtil.hump(psiClass.getName()) : localVar;

        context.append(psiClass.getName()).append(" ").append(name).append(" = new ")
                .append(psiClass.getName()).append("();\n");

        Arrays.stream(psiClass.getAllMethods()).forEach(m -> {
            if (m.getName().startsWith("set")) {
                PsiParameter parameter = m.getParameterList().getParameter(0);
                PsiClass parameterClass;
                if (parameter != null) {
                    parameterClass = PsiTypesUtil.getPsiClass(parameter.getType());
                } else {
                    parameterClass = null;
                }

                if (parameterClass != null && JavaClassTypeUtil.isEntityClass(parameterClass)) {
                    String result = doSetField(parameterClass, HumpNamingUtil.hump(parameterClass.getName()));
                    context.append(result);
                    context.append(TabUtil.getTabSpace()).append(name).append(".").append(m.getName())
                            .append("(").append(HumpNamingUtil.hump(parameterClass.getName())).append(");\n");
                } else {
                    context.append(TabUtil.getTabSpace()).append(name).append(".").append(m.getName()).append("();\n");
                }
            }
        });

        return context.toString();
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
        if (psiMethod.getParameterList().isEmpty() || PsiTypes.voidType().equals(psiMethod.getReturnType())) {
            doErrorNotify(psiMethod.getProject());
            return true;
        }
        return false;
    }

    private void doInfoNotify(Project project) {
        notifyUtil.notifyInfo(project, INFO_MSG);
    }

    private void doErrorNotify(Project project) {
        this.isSuccessful = false;
        notifyUtil.notifyError(project, ERROR_MSG);
    }

}
