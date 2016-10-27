package jdi;

import com.intellij.openapi.project.Project;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import util.StreamRedirecter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rishajai on 9/30/16.
 */
public class JDISession extends Thread {

    private final Project project;
    private final VirtualMachine vm;
    private JDIEventHandler eventHandler;
    private StreamRedirecter errRedirecter;
    private StreamRedirecter outRedirecter;
    private final Set<JDIEventCallback> eventCallbacks;

    public interface JDIEventCallback {
        public void handleEvent (Event event);
    }

    protected JDISession(Project project, VirtualMachine vm) {
        this.project = project;
        this.vm = vm;
        eventCallbacks = new HashSet<>();
    }

    public void addEventCallback(JDIEventCallback cb) {
        this.eventCallbacks.add(cb);
    }

    @Override
    public void run() {
        eventHandler = new JDIEventHandler(this);
        eventHandler.start();
        Process process = vm.process();
        errRedirecter = new StreamRedirecter("err redirection", process.getErrorStream(), System.err);
        errRedirecter.start();
        outRedirecter = new StreamRedirecter("out redirection", process.getInputStream(), System.out);
        outRedirecter.start();
        try {
            eventHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            errRedirecter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            outRedirecter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public VirtualMachine getVM() {
        return vm;
    }

    public Project getProject() {
        return project;
    }

    public boolean isSessionAlive() {
        return eventHandler.isVMConnected();
    }
}
