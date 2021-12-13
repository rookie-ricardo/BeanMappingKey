package org.rookie.plugins.delegate;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.psi.PsiFile;
import org.rookie.plugins.enums.EffectiveFileType;

import java.util.HashMap;
import java.util.Map;

public class BeanMappingProcess {
    private static final Map<EffectiveFileType, BeanMappingDelegate> delegates = new HashMap<>();

    static {
        delegates.put(EffectiveFileType.JAVA, new BeanMappingJavaDelegate());
    }

    public void process(AnActionEvent e) {
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        if (file != null) {
            BeanMappingDelegate delegate = delegates.get(EffectiveFileType.get(file.getFileType().getName()));
            if (delegate != null) {
                delegate.exec(e);
            }
        }
    }

}
