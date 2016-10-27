package util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;

/**
 * Created by rishajai on 9/25/16.
 */
public class IntelliJHelper {

    public static void outputToEventLog (String str, AnAction action) {
        System.out.println(str);
        //Notification.fire(new Notification("code-profiler", "Success", str, NotificationType.INFORMATION), action);
    }
}
