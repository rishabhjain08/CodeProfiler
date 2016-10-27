package actions;

import com.intellij.execution.actions.ChooseRunConfigurationPopup;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.SliderSelectorAction;
import com.intellij.util.Consumer;
import executors.ProfileExecutor;
import jdi.JDISession;
import jdi.JDISessionFactory;
import jdi.exceptions.AlreadyConnectedException;
import jdi.exceptions.ConnectorNotFoundException;
import jdi.exceptions.UnknownException;
import ui.EditConfigurationsDialog;
import util.IntelliJHelper;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by rishajai on 9/25/16.
 */
public class RunConfigurationAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        ChooseRunConfigurationPopup popup = new ChooseRunConfigurationPopup(project, "", ProfileExecutor.getRunExecutorInstance(), null);
        popup.show();
    }
}
