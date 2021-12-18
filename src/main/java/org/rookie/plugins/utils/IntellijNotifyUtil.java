package org.rookie.plugins.utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class IntellijNotifyUtil {

    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("BeanMappingKey Notification Group",
                    NotificationDisplayType.BALLOON, true);

    public static void notifyError(@Nullable Project project, String content) {
        // 2020.3 before
        NOTIFICATION_GROUP.createNotification(content, NotificationType.ERROR).notify(project);
    }

    public static void notifyWarning(@Nullable Project project, String content) {
        // 2020.3 before
        NOTIFICATION_GROUP.createNotification(content, NotificationType.WARNING).notify(project);
    }

    public static void notifyInfo(@Nullable Project project, String content) {
        // 2020.3 before
        NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION).notify(project);
    }
}
