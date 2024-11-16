package org.rookie.plugins.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.rookie.plugins.delegate.BeanMappingProcess;
import org.rookie.plugins.enums.EffectiveFileType;

public class BeanMappingKeyAction extends AnAction {

    private static final BeanMappingProcess process = new BeanMappingProcess();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // check
        if (!isEffective(e)) {
            return;
        }

        // delegate
        process.process(e);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEffective(e));
    }

    private boolean isEffective(AnActionEvent e) {

        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        if (file == null || !EffectiveFileType.contains(file.getFileType().getName())) {
            return false;
        }

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return false;
        }

        String selectedText = editor.getSelectionModel().getSelectedText(true);

        return !StringUtils.isBlank(selectedText);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
