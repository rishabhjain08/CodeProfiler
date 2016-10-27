package profile;

import breakpoints.ProfilerLineBreakpoint;
import breakpoints.ProfilerLineBreakpointManager;
import com.google.inject.Guice;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.evaluation.CodeFragmentKind;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.engine.evaluation.TextWithImportsImpl;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.ui.breakpoints.Breakpoint;
import com.intellij.debugger.ui.breakpoints.BreakpointManager;
import com.intellij.debugger.ui.breakpoints.JavaLineBreakpointType;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.util.ReadTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import runners.ProfilerProgramRunner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by rishajai on 10/2/16.
 */
public class ProjectInitializer implements ProjectComponent {

    private final Project project;
    private volatile boolean someProfilerRunning;
    private TextWithImports profileLineBreakpointCondition;

    private static final String profileLineBreakpointConditionCode = null;//"return true;";

    public ProjectInitializer(Project project) {
        this.project = project;
        this.someProfilerRunning = false;
        profileLineBreakpointCondition = profileLineBreakpointConditionCode == null ? null :
                new TextWithImportsImpl(CodeFragmentKind.CODE_BLOCK, profileLineBreakpointConditionCode);
    }

    ProfilerLineBreakpoint.ProfilerLineBreakpointCallback profilerLineBreakpointCallback =
            new ProfilerLineBreakpoint.ProfilerLineBreakpointCallback() {
        @Override
        public void profilerLineBreakpointHit(ProfilerLineBreakpoint breakpoint) {
            LineBreakpoint lineBreakpoint = breakpoint.getMatchingLineBreakpoint();
            int lineIndex = -1;
            final StringBuilder canonicalPath = new StringBuilder("");
            if (lineBreakpoint != null) {
                lineIndex = lineBreakpoint.getLineIndex();
                new ReadAction() {
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        VirtualFile file = FileDocumentManager.getInstance().getFile(lineBreakpoint.getDocument());
                        canonicalPath.append(file.getCanonicalPath());
                    }
                }.execute();
            }
            String filePath = canonicalPath.toString();
            filePath = filePath.equals("") ? null : filePath;
            // do something with the values
            DebuggerSession debuggerSession = DebuggerManagerEx.getInstanceEx(project).getContext().getDebuggerSession();
            ConsoleView consoleView = debuggerSession == null ? null : debuggerSession.getXDebugSession().getConsoleView();
            if (consoleView != null) {
                String output = filePath + ":" + lineIndex + " time:" + System.currentTimeMillis() + System.lineSeparator();
                consoleView.print(output, ConsoleViewContentType.NORMAL_OUTPUT);
            }
        }
    };

    ProfilerProgramRunner.ProfileSessionListener profileSessionListener =
            new ProfilerProgramRunner.ProfileSessionListener() {

                Set<Long> profileRunners = new HashSet<>();

                @Override
                public void onProfileStarted(RunProfileState state, ExecutionEnvironment env) {
                    profileRunners.add(env.getExecutionId());
                    someProfilerRunning = true;
                }

                @Override
                public void onProfileStopped(RunProfileState state, ExecutionEnvironment env) {
                    profileRunners.remove(env.getExecutionId());
                    if (profileRunners.isEmpty()) {
                        someProfilerRunning = false;
                    }
                    // restore line breakpoint properties
                    if (!someProfilerRunning) {
                        ProfilerLineBreakpointManager profilerLineBreakpointManager = ProfilerLineBreakpointManager
                                .getProfilerLineBreakpointManager(env.getProject());
                        profilerLineBreakpointManager.deProfileAllLineBreakpoints();
                        profilerLineBreakpointManager.cleanupAllZombieProfileLineBreakpoints();
                    }
                }
            };

    @Override
    public void projectOpened() {

        XDebuggerManager.getInstance(project).getBreakpointManager().addBreakpointListener(new XBreakpointListener<XBreakpoint<?>>() {

            private ProfilerLineBreakpointManager profilerLineBreakpointManager = ProfilerLineBreakpointManager
                    .getProfilerLineBreakpointManager(project);
            private BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project)
                    .getBreakpointManager();

            @Override
            public void breakpointAdded(@NotNull XBreakpoint<?> xBreakpoint) {
//                System.out.println("----------------");
//                for (Breakpoint breakpoint : breakpointManager.getBreakpoints()) {
//                    System.out.println(breakpoint.getXBreakpoint().getType() + " -- " + (breakpoint.getXBreakpoint()
//                            .getSourcePosition() != null ? breakpoint.getXBreakpoint().getSourcePosition().getLine()
//                            : -1));
//                }
                if (xBreakpoint.getType().getClass().equals(JavaLineBreakpointType.class)) {
                    LineBreakpoint lineBreakpoint = (LineBreakpoint) BreakpointManager.getJavaBreakpoint(xBreakpoint);
                    if (lineBreakpoint != null) {
                        boolean profilerLineBreakpointAttached = profilerLineBreakpointManager
                                .findProfilerLineBreakpoint(lineBreakpoint) != null;
                        if (!profilerLineBreakpointAttached && someProfilerRunning) {
                            profilerLineBreakpointManager.attachProfilerLineBreakpoint(lineBreakpoint);
                        }
                    }
                }
            }

            @Override
            public void breakpointRemoved(@NotNull XBreakpoint<?> xBreakpoint) {
                if (xBreakpoint.getType().getClass().equals(JavaLineBreakpointType.class)) {
                    LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpointManager.getJavaBreakpoint(xBreakpoint);
                    if (lineBreakpoint != null) {
                        profilerLineBreakpointManager.detachProfilerLineBreakpoint(lineBreakpoint);
                    }
                }
            }

            @Override
            public void breakpointChanged(@NotNull XBreakpoint<?> xBreakpoint) {
                if (xBreakpoint.getType().getClass().equals(JavaLineBreakpointType.class)) {
                    // unhandled
                }
            }
        });
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "CodeProfilerProjectInitializer";
    }

    public ProfilerLineBreakpoint.ProfilerLineBreakpointCallback getProfilerLineBreakpointCallback() {
        return this.profilerLineBreakpointCallback;
    }

    public ProfilerProgramRunner.ProfileSessionListener getProfileSessionListener() {
        return this.profileSessionListener;
    }

    public TextWithImports getProfileLineBreakpointCondition() {
        return profileLineBreakpointCondition;
    }
}
