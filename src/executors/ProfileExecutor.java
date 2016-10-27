package executors;

import com.intellij.debugger.ui.breakpoints.StepIntoBreakpoint;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.ExecutorRegistryImpl;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.ui.UIBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by rishajai on 10/1/16.
 */
public class ProfileExecutor extends Executor {
    @NonNls
    public static final String EXECUTOR_ID = "CodeProfiler.RunConfiguration";

    public ProfileExecutor() {
        StepIntoBreakpoint b;
    }

    @NotNull
    public String getStartActionText() {
        return "Start profiling...";
    }

    public String getToolWindowId() {
        return "Code Profile";
    }

    public Icon getToolWindowIcon() {
        return AllIcons.Toolwindows.ToolWindowRun;
    }

    @NotNull
    public Icon getIcon() {
        Icon var10000 = AllIcons.Actions.Execute;
        if(AllIcons.Actions.Execute == null) {
            throw new IllegalStateException(String.format("@NotNull method %s.%s must not return null", new
                    Object[]{"executors/ProfileExecutor", "getIcon"}));
        } else {
            return var10000;
        }
    }

    public Icon getDisabledIcon() {
        return AllIcons.Process.DisabledRun;
    }

    public String getDescription() {
        return "Profiles your code...";
    }

    @NotNull
    public String getActionName() {
        return "Code Profile";
    }

    @NotNull
    public String getId() {
        String var10000 = EXECUTOR_ID;
        if(EXECUTOR_ID == null) {
            throw new IllegalStateException(String.format("@NotNull method %s.%s must not return null", new Object[]{"com/intellij/execution/executors/DefaultRunExecutor", "getId"}));
        } else {
            return var10000;
        }
    }

    public String getContextActionId() {
        return "CodeProfileClass";
    }

    public String getHelpId() {
        return "ideaInterface.codeprofile";
    }

    public static Executor getRunExecutorInstance() {
        return ExecutorRegistry.getInstance().getExecutorById(EXECUTOR_ID);
    }

}
