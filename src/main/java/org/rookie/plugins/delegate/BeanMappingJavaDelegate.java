package org.rookie.plugins.delegate;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.rookie.plugins.entity.MappingJavaBeanMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BeanMappingJavaDelegate implements BeanMappingDelegate{
    private Project project;

    @Override
    public void exec(AnActionEvent e) {
        project = e.getProject();
        PsiJavaFile javaFile = (PsiJavaFile) e.getData(PlatformDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        generate(getSelectClass(javaFile, editor));

    }

    private List<MappingJavaBeanMeta> getSelectClass(PsiJavaFile javaFile, Editor editor) {
        List<MappingJavaBeanMeta> metas = new ArrayList<>();
        if (javaFile == null || editor == null) {
            return metas;
        }

        PsiImportList importList = javaFile.getImportList();
        String selectedText = editor.getSelectionModel().getSelectedText(true);

        if (StringUtils.isNotBlank(selectedText) && importList != null) {

            List<String> selectedClass = Arrays
                    .stream(selectedText.split("\n"))
                    .filter(StringUtils::isNotBlank)
                    .limit(2)
                    .collect(Collectors.toList());

            Arrays.stream(importList.getAllImportStatements()).forEach(state -> {

                String[] classPath = state.getText().split("\\.");
                String className = classPath[classPath.length - 1];
                selectedClass.forEach(selected -> {
                    if (className.equals(selected)) {
                        MappingJavaBeanMeta meta = new MappingJavaBeanMeta();
                        meta.setClassQualifiedName(state.getText().substring(7));
                        meta.setClassSimpleName(selected);
                        metas.add(meta);
                    }
                });
            });
        }

        return metas;
    }

    private void generate(List<MappingJavaBeanMeta> selectClass) {
        if (selectClass.size() == 1) {
            doGenerate(selectClass.get(0).getClassQualifiedName());
        } else {
            doGenerate(selectClass.get(1).getClassQualifiedName(), selectClass.get(0).getClassQualifiedName());
        }
    }

    private void doGenerate(String selectClass) {
        PsiClass target = getPsiClass(selectClass);

    }

    private void doGenerate(String from, String to) {
        PsiClass fromClas = getPsiClass(from);
        PsiClass toClass = getPsiClass(to);

    }

    private PsiClass getPsiClass(String qualifiedName) {
        return JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.projectScope(project));
    }

}
