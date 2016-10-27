package breakpoints;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.ui.breakpoints.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashMap;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.*;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;
import com.intellij.xdebugger.impl.breakpoints.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaLineBreakpointProperties;
import profile.ProjectInitializer;

import javax.sound.sampled.Line;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rishajai on 10/2/16.
 */
public class ProfilerLineBreakpointManager {

    private static Map<Project, ProfilerLineBreakpointManager> managerMap = new HashMap<>();
    private static long ProfileLineBreakpointCounter = 0;
    private Project project;
    private final LineBreakpointManager lineBreakpointManager;
    //private Map<XBreakpoint, LineBreakpoint>
    private ProfilerLineBreakpointManager(Project project) {
        this.project = project;
        lineBreakpointManager = new LineBreakpointManager();
    }

    public synchronized static ProfilerLineBreakpointManager getProfilerLineBreakpointManager(Project project) {
        ProfilerLineBreakpointManager manager = null;
        manager = managerMap.get(project);
        if (manager != null) {
            return manager;
        }
        manager = new ProfilerLineBreakpointManager(project);
        managerMap.put(project, manager);
        return manager;
    }

    public void cleanupAllZombieProfileLineBreakpoints() {
        BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager();
        List<Breakpoint> allBreakpoints = breakpointManager.getBreakpoints();
        Set<Breakpoint> breakpointsSet = new HashSet<>(allBreakpoints);
        boolean isLineBreakpointPresent;
        for (Breakpoint breakpoint : allBreakpoints) {
            if (breakpoint instanceof ProfilerLineBreakpoint) {
                isLineBreakpointPresent = breakpointsSet.contains(((ProfilerLineBreakpoint) breakpoint).getMatchingLineBreakpoint());
                if (!isLineBreakpointPresent) {
                    breakpointManager.removeBreakpoint(breakpoint);
                }
            }
        }
    }

    public void profileAllLineBreakpoints() {
        cleanupAllZombieProfileLineBreakpoints();
        BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager();
        XDependentBreakpointManager dependentBreakpointManager = ((XBreakpointManagerImpl) XDebuggerManager.getInstance
                (project).getBreakpointManager()).getDependentBreakpointManager();
        List<Breakpoint> allBreakpoints = breakpointManager.getBreakpoints();
        ProfilerLineBreakpoint profilerLineBreakpoint = null;
        boolean profileBreakpointFound;
        //System.out.println("_____________________________________");
        for (Breakpoint breakpoint : allBreakpoints) {
            if (breakpoint instanceof LineBreakpoint && !(breakpoint instanceof ProfilerLineBreakpoint)) {
                profileBreakpointFound = findProfilerLineBreakpoint((LineBreakpoint) breakpoint) != null;
//                List<XBreakpoint<?>> slaveBreakpoints = dependentBreakpointManager.getSlaveBreakpoints(breakpoint.getXBreakpoint());
//                for (XBreakpoint slaveBreakpoint : slaveBreakpoints) {
//                    System.out.println("slave breakpoints " + slaveBreakpoint.getType());
//                    if (slaveBreakpoint.getType().equals(JavaProfilerLineBreakpointType.class)) {
//                        profileBreakpointFound = true;
//                        break;
//                    }
//                }
                if (!profileBreakpointFound) {
                    profilerLineBreakpoint = attachProfilerLineBreakpoint((LineBreakpoint)breakpoint);
                }
            }
        }
    }

    public void deProfileAllLineBreakpoints() {
        BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager();
        XDependentBreakpointManager dependentBreakpointManager = ((XBreakpointManagerImpl) XDebuggerManager.getInstance
                (project).getBreakpointManager()).getDependentBreakpointManager();
        List<Breakpoint> allBreakpoints = breakpointManager.getBreakpoints();
        ProfilerLineBreakpoint profilerLineBreakpoint = null;
        boolean profileBreakpointFound;
        //System.out.println("_____________________________________");
        for (Breakpoint breakpoint : allBreakpoints) {
            if (breakpoint instanceof LineBreakpoint && !(breakpoint instanceof ProfilerLineBreakpoint)) {
                profileBreakpointFound = findProfilerLineBreakpoint((LineBreakpoint) breakpoint) != null;
//                List<XBreakpoint<?>> slaveBreakpoints = dependentBreakpointManager.getSlaveBreakpoints(breakpoint.getXBreakpoint());
//                for (XBreakpoint slaveBreakpoint : slaveBreakpoints) {
//                    System.out.println("slave breakpoints " + slaveBreakpoint.getType());
//                    if (slaveBreakpoint.getType().equals(JavaProfilerLineBreakpointType.class)) {
//                        profileBreakpointFound = true;
//                        break;
//                    }
//                }
                if (profileBreakpointFound) {
                    detachProfilerLineBreakpoint((LineBreakpoint)breakpoint);
                }
            }
        }
    }

//    private boolean isCorrespondingProfileLineBreakpointPresent(LineBreakpoint lineBreakpoint) {
//        List<Breakpoint> breakpoints = this.findHighlightedBreakpoints(lineBreakpoint.getDocument(), lineBreakpoint.getLineIndex(), lineBreakpoint.getCategory());
//        System.out.println("vvvvvvvvv for breakpoint under " + lineBreakpoint.getLineIndex());
//        for (Breakpoint breakpoint : breakpoints) {
//            System.out.println(breakpoint.getClassName() + " || " + breakpoint.getCategory() + " || " + breakpoint.getDisplayName());
//        }
//        System.out.println("^^^^^^^^for breakpoint under " + lineBreakpoint.getLineIndex());
//            for (Breakpoint breakpoint : breakpoints) {
//            if (breakpoint instanceof ProfilerLineBreakpoint) {
//                return true;
//            }
//        }
//        return false;
//    }

    public void detachProfilerLineBreakpoint(LineBreakpoint lineBreakpoint) {
//        XDependentBreakpointManager dependentBreakpointManager = ((XBreakpointManagerImpl) XDebuggerManager.getInstance
//                (project).getBreakpointManager()).getDependentBreakpointManager();
        BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager();
        ProfilerLineBreakpoint profilerLineBreakpoint = findProfilerLineBreakpoint(lineBreakpoint);
        if (profilerLineBreakpoint != null) {
//            DebuggerManagerEx.getInstanceEx(project).getContext().getDebuggerSession().getXDebugSession()
//                    .getDebugProcess().get
            (new WriteAction() {
                protected void run(@NotNull Result result) {
                    breakpointManager.removeBreakpoint(profilerLineBreakpoint);
                }
            }).execute();
            lineBreakpointManager.restoreState(lineBreakpoint);
        }
    }

    public ProfilerLineBreakpoint attachProfilerLineBreakpoint(LineBreakpoint lineBreakpoint) {
//        System.out.println("ATTACHING to " + lineBreakpoint);
//        XDependentBreakpointManager dependentBreakpointManager = ((XBreakpointManagerImpl) XDebuggerManager.getInstance
//                (project).getBreakpointManager()).getDependentBreakpointManager();
        ProfilerLineBreakpoint profilerLineBreakpoint = addProfilerLineBreakpoint(lineBreakpoint.getDocument(),
                lineBreakpoint.getSourcePosition());
//        System.out.println("RESULTANT BREAKP -  " + profilerLineBreakpoint);
        if (profilerLineBreakpoint != null) {
//            profilerLineBreakpoint.setVisible(false);
//            XLineBreakpointManager xLineBreakpointManager = ((XBreakpointManagerImpl) XDebuggerManager
//                    .getInstance(project).getBreakpointManager()).getLineBreakpointManager();
//            xLineBreakpointManager.unregisterBreakpoint((XLineBreakpointImpl) profilerLineBreakpoint.getXBreakpoint());
//            xLineBreakpointManager.updateBreakpointsUI();
//            System.out.println("on init === " +
//                    profilerLineBreakpoint.init());
            ProfilerLineBreakpoint.ProfilerLineBreakpointCallback profilerLineBreakpointCallback =
                    project.getComponent(ProjectInitializer.class).getProfilerLineBreakpointCallback();
            profilerLineBreakpoint.setMatchingLineBreakpoint(lineBreakpoint);
            TextWithImports condition = project.getComponent(ProjectInitializer.class).getProfileLineBreakpointCondition();
            if (condition != null) {
                profilerLineBreakpoint.setCondition(project.getComponent(ProjectInitializer.class)
                        .getProfileLineBreakpointCondition());
            }
            profilerLineBreakpoint.addProfilerLineBreakpointCallback(profilerLineBreakpointCallback);
            lineBreakpointManager.saveState(lineBreakpoint);
            lineBreakpoint.setSuspendPolicy("SuspendNone");
        }
//        dependentBreakpointManager.setMasterBreakpoint(profilerLineBreakpoint.getXBreakpoint(), lineBreakpoint.getXBreakpoint(), false);
        return profilerLineBreakpoint;
    }

    private ProfilerLineBreakpoint addProfilerLineBreakpoint(Document document, SourcePosition sourcePosition) {
        //lineIndex++;
        ApplicationManager.getApplication().assertIsDispatchThread();
//        if(!LineBreakpoint.canAddLineBreakpoint(project, document, lineIndex)) {
//            System.out.println("duplicate breakpoint");
//        }
        XBreakpointBase xBreakpoint = addXBreakpoint(JavaProfilerLineBreakpointType.class, project,
                document, sourcePosition.getLine());
//        if (xLineBreakpoint != null) {
//            final XBreakpointManager xBreakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
//            xBreakpointManager.updateBreakpointPresentation(xLineBreakpoint, null, " ");
//        }
//        System.out.println("xbreakpoint is " + xBreakpoint);
        Breakpoint breakpoint = BreakpointManager.getJavaBreakpoint(xBreakpoint);
//        System.out.println("java breakpoint is " + breakpoint + " is instance correct " + (breakpoint instanceof
//                ProfilerLineBreakpoint) );
        if(breakpoint instanceof ProfilerLineBreakpoint) {
            BreakpointManager.addBreakpoint(breakpoint);
            return (ProfilerLineBreakpoint)breakpoint;
        } else {
            return null;
        }
    }

//    private <B extends XBreakpoint<?>> XLineBreakpoint addXLineBreakpoint(Class<? extends XBreakpointType<B, ?>>
//                                                                                  typeCls, Project project, Document document, int lineIndex) {
//        XBreakpointType type = XDebuggerUtil.getInstance().findBreakpointType(typeCls);
//        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
//        XLineBreakpoint xLineBreakpoint = (XLineBreakpoint)ApplicationManager.getApplication().<XLineBreakpoint>runWriteAction(() -> {
//            return XDebuggerManager.getInstance(project).getBreakpointManager().addLineBreakpoint((XLineBreakpointType)
//                    type, file.getUrl(), lineIndex, ((XLineBreakpointType)type).createBreakpointProperties(file, lineIndex), false);
//        });
//        XBreakpointManagerImpl xBreakpointManager = (XBreakpointManagerImpl) XDebuggerManager.getInstance(project).getBreakpointManager();
//        xBreakpointManager.getLineBreakpointManager().unregisterBreakpoint((XLineBreakpointImpl) xLineBreakpoint);
//        return xLineBreakpoint;
//    }

    private <B extends XBreakpoint<?>> XBreakpointBase addXBreakpoint(Class<? extends XBreakpointType<B, ?>> typeCls,
                                                                      Project project, Document document, int lineIndex) {
        XBreakpointManagerImpl xBreakpointManager = (XBreakpointManagerImpl) XDebuggerManager.getInstance(project).getBreakpointManager();
        XBreakpointType type = XDebuggerUtil.getInstance().findBreakpointType(typeCls);
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        return (XBreakpointBase)ApplicationManager.getApplication().<XBreakpointBase>runWriteAction(() -> {
            XBreakpointBase xBreakpointBase = (XBreakpointBase) (xBreakpointManager.addBreakpoint(type, new JavaProfilerLineBreakpointProperties()));
            LineBreakpointState state = new LineBreakpointState(true, type.getId(), file.getUrl(), lineIndex, false,
                    ProfileLineBreakpointCounter++);
            return xBreakpointBase;
            //return new XProfileLineBreakpointImpl(type, xBreakpointManager, new JavaProfilerLineBreakpointProperties
              //      (), state);
        });
        //        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
//        XBreakpoint xBreakpoint = (XBreakpoint) ApplicationManager.getApplication().<XBreakpoint>runWriteAction(() -> {
//            return xBreakpointManager.addBreakpoint(type, null);
//        });
//        return xBreakpoint;
    }

//    @NotNull
//    public <T extends XBreakpointProperties> XLineBreakpoint<T> addLineBreakpoint(XLineBreakpointType<T> type, @NotNull String fileUrl, int line, @Nullable T properties, boolean temporary) {
//        BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager();
//        XBreakpointManagerImpl xBreakpointManager = (XBreakpointManagerImpl) XDebuggerManager.getInstance(project).getBreakpointManager();
//        ApplicationManager.getApplication().assertWriteAccessAllowed();
//        LineBreakpointState state = new LineBreakpointState(true, type.getId(), fileUrl, line, temporary, ++ProfileLineBreakpointCounter);
//        XLineBreakpointImpl breakpoint = new XLineBreakpointImpl(type, xBreakpointManager, properties, state);
//        xBreakpointManager.addBreakpoint(breakpoint, false, true);
//        return breakpoint;
//    }

    public ProfilerLineBreakpoint findProfilerLineBreakpoint(LineBreakpoint lineBreakpoint) {
        List<Breakpoint> allBreakpoints = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager().getBreakpoints();
        ProfilerLineBreakpoint profilerLineBreakpoint = null;
        for (Breakpoint breakPoint : allBreakpoints) {
            if (!(breakPoint instanceof ProfilerLineBreakpoint)) {
                continue;
            }
            if (((ProfilerLineBreakpoint) breakPoint).getMatchingLineBreakpoint() == lineBreakpoint) {
                profilerLineBreakpoint = (ProfilerLineBreakpoint) breakPoint;
                break;
            }
//            profilerLineBreakpoint = (ProfilerLineBreakpoint) breakPoint;
//            if (!profilerLineBreakpoint.isAt(lineBreakpoint.getDocument(),
//                    lineBreakpoint.getXBreakpoint().getSourcePosition().getOffset())) {
//                profilerLineBreakpoint = null;
//                continue;
//            }
//            break;
        }
        return profilerLineBreakpoint;
    }

    public class LineBreakpointManager {

        private final Map<LineBreakpoint, LineBreakpointState> savedStates;

        private LineBreakpointManager() {
            savedStates = new ConcurrentHashMap<>();
        }

        private void saveState(LineBreakpoint lineBreakpoint) {
            LineBreakpointState state = new LineBreakpointState();
            state.suspendPolicy = lineBreakpoint.getSuspendPolicy();
            savedStates.put(lineBreakpoint, state);
            System.out.println("save state for " + lineBreakpoint + " " + state.suspendPolicy);
        }

        private void restoreState(LineBreakpoint lineBreakpoint) {
            (new ReadAction() {
                protected void run(@NotNull Result result) {
                    LineBreakpointState state = savedStates.get(lineBreakpoint);
                    System.out.println("saved state for " + lineBreakpoint + " is " + (state == null ? "NULL" : state.suspendPolicy));
                    if (state != null) {
                        lineBreakpoint.setSuspendPolicy(state.suspendPolicy);
                    }
                    savedStates.remove(lineBreakpoint);
                }
            }).execute();
        }

        private void saveState() {
            BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager();
            List<Breakpoint> breakpoints = breakpointManager.getBreakpoints();
            for (Breakpoint breakpoint : breakpoints) {
                if (breakpoint.getXBreakpoint().getType().equals(JavaLineBreakpointType.class)) {
                    saveState((LineBreakpoint) breakpoint);
                }
            }
        }

        private void restoreState() {
            BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(project).getBreakpointManager();
            List<Breakpoint> breakpoints = breakpointManager.getBreakpoints();
            for (Breakpoint breakpoint : breakpoints) {
                if (breakpoint.getXBreakpoint().getType().equals(JavaLineBreakpointType.class)) {
                    restoreState((LineBreakpoint) breakpoint);
                }
            }
        }

        private class LineBreakpointState {
            String suspendPolicy;
        }
    }
}
