package breakpoints;

import com.intellij.debugger.DebuggerBundle;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaBreakpointHandler;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.debugger.ui.breakpoints.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.SmartList;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.*;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.intellij.xdebugger.impl.breakpoints.BreakpointState;
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaBreakpointProperties;
import org.jetbrains.java.debugger.breakpoints.properties.JavaLineBreakpointProperties;

import javax.swing.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rishajai on 10/1/16.
 */
//public class XBreakpointBase<Self extends XBreakpoint<P>, P extends XBreakpointProperties, S extends BreakpointState> extends UserDataHolderBase implements XBreakpoint<P>, Comparable<Self> {

//public class XLineBreakpointImpl<P extends XBreakpointProperties> extends com.intellij.xdebugger.impl.breakpoints.XBreakpointBase<XLineBreakpoint<P>, P, LineBreakpointState<P>> implements XLineBreakpoint<P> {

public class JavaProfilerLineBreakpointType extends JavaBreakpointTypeBase<JavaProfilerLineBreakpointProperties>
        implements JavaBreakpointType<JavaProfilerLineBreakpointProperties> {

    public JavaProfilerLineBreakpointType() {
        super("java-profiler-line", "Profiler Line Breakpoints");
    }

    protected JavaProfilerLineBreakpointType(@NonNls @NotNull String id, @Nls @NotNull String title) {
        super(id, title);
    }

    @Override
    public String getDisplayText(XBreakpoint xBreakpoint) {
        return "some text...";
    }

    protected String getHelpID() {
        return "debugging.profilerLineBreakpoint";
    }

    public String getDisplayName() {
        return "Profiler line breakpoints...";
    }

    @NotNull
    public Breakpoint<JavaProfilerLineBreakpointProperties> createJavaBreakpoint(Project project, XBreakpoint breakpoint) {
        return new ProfilerLineBreakpoint(project, breakpoint);
        //super.getSour
    }

    @Nullable
    public PsiElement getContainingMethod(@NotNull ProfilerLineBreakpoint breakpoint) {
        SourcePosition position = breakpoint.getSourcePosition();
        if(position == null) {
            return null;
        } else {
            JavaBreakpointProperties properties = breakpoint.getXBreakpoint().getProperties();
            Integer ordinal = ((JavaLineBreakpointProperties)properties).getLambdaOrdinal();
            if(ordinal.intValue() > -1) {
                List lambdas = DebuggerUtilsEx.collectLambdas(position, true);
                if(ordinal.intValue() < lambdas.size()) {
                    return (PsiElement)lambdas.get(ordinal.intValue());
                }
            }
            return DebuggerUtilsEx.getContainingMethod(position);
        }
    }

    public boolean matchesPosition(@NotNull ProfilerLineBreakpoint breakpoint, @NotNull SourcePosition position) {
        JavaProfilerLineBreakpointProperties properties = breakpoint.getXBreakpoint().getProperties();
        if(((JavaProfilerLineBreakpointProperties)properties).getLambdaOrdinal() == null) {
            return true;
        } else {
            PsiElement containingMethod = this.getContainingMethod(breakpoint);
            return containingMethod == null?false:DebuggerUtilsEx.inTheMethod(position, containingMethod);
        }
    }

    @Nullable
    public XSourcePosition getSourcePosition(@NotNull XBreakpoint<JavaProfilerLineBreakpointProperties> xBreakpoint) {
        ProfilerLineBreakpoint profilerLineBreakpoint = (ProfilerLineBreakpoint) BreakpointManager.getJavaBreakpoint(xBreakpoint);
        LineBreakpoint lineBreakpoint = profilerLineBreakpoint.getMatchingLineBreakpoint();
        //System.out.println("getting source position for " + profilerLineBreakpoint);
        if (lineBreakpoint != null) {
            //System.out.println("you are depending on " +((XLineBreakpointImpl)(lineBreakpoint.getXBreakpoint()))
              //      .getSourcePosition() + " man...");
            return ((XLineBreakpointImpl)(lineBreakpoint.getXBreakpoint())).getSourcePosition();
        }
        return null;
    }

//    @Override
//    public XSourcePosition getSourcePosition(@NotNull XBreakpoint<JavaLineBreakpointProperties> xBreakpoint) {
//        ProfilerLineBreakpoint lineBreakpoint = (ProfilerLineBreakpoint) BreakpointManager.getJavaBreakpoint
//                (xBreakpoint);
//        XSourcePosition xSourcePosition = lineBreakpoint.getXSourcePosition();
//        if (xSourcePosition != null) {
//            return xSourcePosition;
//        }
//        (new ReadAction() {
//            protected void run(@NotNull Result result) {
//                XSourcePosition xSourcePosition = XDebuggerUtil.getInstance().createPosition(XLineBreakpointImpl.this.getFile(),
//                        XLineBreakpointImpl.this.getLine());
//            }
//        }).execute();
//    }
}
