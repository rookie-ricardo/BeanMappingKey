package org.rookie.plugins.utils;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class IntellijNotifyUtil {

    private NotificationGroup getNotificationGroup() {
        return NotificationGroupManager.getInstance()
                .getNotificationGroup("BeanMappingKeyNotification");
    }

    public void notifyError(@Nullable Project project, String content) {
        getNotificationGroup().createNotification(content, NotificationType.ERROR).notify(project);
    }

    public void notifyWarning(@Nullable Project project, String content) {
        getNotificationGroup().createNotification(content, NotificationType.WARNING).notify(project);
    }

    public void notifyInfo(@Nullable Project project, String content) {
        getNotificationGroup().createNotification(content, NotificationType.INFORMATION).notify(project);
    }
}
