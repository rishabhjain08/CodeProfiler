package breakpoints;

//TODO: delete this file

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterDraggableObject;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.impl.breakpoints.*;
import com.sun.javadoc.Doc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.io.File;
import java.util.*;

/**
 * Created by rishajai on 10/2/16.
 */
public class XProfileLineBreakpointImpl extends XBreakpointBase {

//    private Document document;
//    private int offset;
//    private final XLineBreakpointType<P> myType;
    private XSourcePosition mySourcePosition;
//    private boolean myDisposed;

    public XProfileLineBreakpointImpl(XBreakpointType type, XBreakpointManagerImpl breakpointManager,
                                      XBreakpointProperties properties, BreakpointState state) {
        super(type, breakpointManager, properties, state);
//        this.document = document;
//        this.offset = offset;
    }

////    @Nullable
////    public Document getDocument() {
////        VirtualFile file = this.getFile();
////        return file == null?null: FileDocumentManager.getInstance().getDocument(file);
////    }
////
    @Nullable
    private VirtualFile getFile() {
        return VirtualFileManager.getInstance().findFileByUrl(this.getFileUrl());
    }
//
    public int getLine() {
        return ((LineBreakpointState)this.myState).getLine();
    }
//
    public String getFileUrl() {
//        System.out.println("state = " + myState + " file url = " + ((LineBreakpointState)this.myState).getFileUrl());
        return ((LineBreakpointState)this.myState).getFileUrl();
    }
//
    public String getPresentableFilePath() {
        String url = this.getFileUrl();
        return url != null && "file".equals(VirtualFileManager.extractProtocol(url))? FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(url)):(url != null?url:"");
    }
//
    public String getShortFilePath() {
        String path = this.getPresentableFilePath();
        return path.isEmpty()?"":(new File(path)).getName();
    }
//
//    @Nullable
//    public RangeHighlighter getHighlighter() {
//        return this.myHighlighter;
//    }

    @Override
    public XSourcePosition getSourcePosition() {
        if(this.mySourcePosition == null) {
            (new ReadAction() {
                protected void run(@NotNull Result result) {
                    XProfileLineBreakpointImpl.this.mySourcePosition = XDebuggerUtil.getInstance().createPosition(XProfileLineBreakpointImpl
                            .this.getFile(), XProfileLineBreakpointImpl.this.getLine());
//                    System.out.println(mySourcePosition + " || " + getFile() + " || " + getLine());
                }
            }).execute();
        }

        return this.mySourcePosition;
    }

//    public boolean isValid() {
//        return this.myHighlighter != null && this.myHighlighter.isValid();
//    }

//    public void dispose() {
//        this.removeHighlighter();
//        this.myDisposed = true;
//    }
//
//    private void removeHighlighter() {
//        if(this.myHighlighter != null) {
//            this.myHighlighter.dispose();
//            this.myHighlighter = null;
//        }
//
//    }
//
//    protected GutterDraggableObject createBreakpointDraggableObject() {
//        return new GutterDraggableObject() {
//            public boolean copy(int line, VirtualFile file) {
//                if(XLineBreakpointImpl.this.canMoveTo(line, file)) {
//                    XLineBreakpointImpl.this.setFileUrl(file.getUrl());
//                    XLineBreakpointImpl.this.setLine(line, true);
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//
//            public Cursor getCursor(int line) {
//                return XLineBreakpointImpl.this.canMoveTo(line, XLineBreakpointImpl.this.getFile())? DragSource.DefaultMoveDrop:DragSource.DefaultMoveNoDrop;
//            }
//        };
//    }
//
//    private boolean canMoveTo(int line, VirtualFile file) {
//        if(file != null && this.myType.canPutAt(file, line, this.getProject())) {
//            XLineBreakpoint existing = this.getBreakpointManager().findBreakpointAtLine(this.myType, file, line);
//            return existing == null || existing == this;
//        } else {
//            return false;
//        }
//    }
//
//    public void updatePosition() {
//        if(this.myHighlighter != null && this.myHighlighter.isValid()) {
//            this.setLine(this.myHighlighter.getDocument().getLineNumber(this.myHighlighter.getStartOffset()), false);
//        }
//
//    }
//
//    public void setFileUrl(String newUrl) {
//        if(!Comparing.equal(this.getFileUrl(), newUrl)) {
//            ((LineBreakpointState)this.myState).setFileUrl(newUrl);
//            this.mySourcePosition = null;
//            this.removeHighlighter();
//            this.fireBreakpointChanged();
//        }
//
//    }
//
//    private void setLine(int line, boolean removeHighlighter) {
//        if(this.getLine() != line) {
//            ((LineBreakpointState)this.myState).setLine(line);
//            this.mySourcePosition = null;
//            if(removeHighlighter) {
//                this.removeHighlighter();
//            }
//
//            this.fireBreakpointChanged();
//        }
//
//    }
//
//    public boolean isTemporary() {
//        return ((LineBreakpointState)this.myState).isTemporary();
//    }
//
//    public void setTemporary(boolean temporary) {
//        if(this.isTemporary() != temporary) {
//            ((LineBreakpointState)this.myState).setTemporary(temporary);
//            this.fireBreakpointChanged();
//        }
//
//    }
//
//    protected java.util.List<? extends AnAction> getAdditionalPopupMenuActions(XDebugSession session) {
//        return this.getType().getAdditionalPopupMenuActions(this, session);
//    }
//
//    protected void updateIcon() {
//        Icon icon = this.calculateSpecialIcon();
//        if(icon != null) {
//            this.setIcon(icon);
//        } else {
//            this.setIcon(this.isTemporary()?this.myType.getTemporaryIcon():this.myType.getEnabledIcon());
//        }
//    }

    public String toString() {
        return "XProfileLineBreakpointImpl(" + " at " + this.getShortFilePath() + ":" + this.getLine() + ")";
    }
}
