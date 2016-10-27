package runners;

import breakpoints.ProfilerLineBreakpoint;
import breakpoints.ProfilerLineBreakpointManager;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import executors.ProfileExecutor;
import org.jetbrains.annotations.NotNull;
import profile.ProjectInitializer;

/**
 * Created by rishajai on 10/1/16.
 */
public class ProfilerProgramRunner extends GenericDebuggerRunner {

    private static final String ID = "ProfilerProgramRunner";

    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return executorId.equals(ProfileExecutor.EXECUTOR_ID) && profile instanceof ModuleRunProfile
                && !(profile instanceof RunConfigurationWithSuppressedDefaultDebugAction);
    }

    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
        onSessionStarted(state, env);
        RunContentDescriptor contentDescriptor = super.doExecute(state, env);
        XDebugSessionImpl sessionImpl = (XDebugSessionImpl)(DebuggerManagerEx.getInstanceEx(env.getProject())
                .getContext().getDebuggerSession().getXDebugSession());
        sessionImpl.addSessionListener(new XDebugSessionListener() {
            @Override
            public void sessionStopped() {
                onSessionStopped(state, env);
            }
        });
        return contentDescriptor;
    }

    private void onSessionStarted(RunProfileState state, ExecutionEnvironment env) {
//        System.out.println("session started...");
        ProfilerLineBreakpointManager profilerLineBreakpointManager = ProfilerLineBreakpointManager.getProfilerLineBreakpointManager(env.getProject());
        profilerLineBreakpointManager.profileAllLineBreakpoints();
        ProfileSessionListener sessionListener = env.getProject().getComponent(ProjectInitializer.class).getProfileSessionListener();
        sessionListener.onProfileStarted(state, env);
    }

    private void onSessionStopped(RunProfileState state, ExecutionEnvironment env) {
//        System.out.println("session stopped...");
        ProfileSessionListener sessionListener = env.getProject().getComponent(ProjectInitializer.class).getProfileSessionListener();
        sessionListener.onProfileStopped(state, env);
    }

    public interface ProfileSessionListener {
        public void onProfileStarted(RunProfileState state, ExecutionEnvironment env);
        public void onProfileStopped(RunProfileState state, ExecutionEnvironment env);
    }
}
