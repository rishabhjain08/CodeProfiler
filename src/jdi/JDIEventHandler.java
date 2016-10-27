package jdi;

import com.intellij.openapi.project.Project;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by rishajai on 9/30/16.
 */
public class JDIEventHandler extends Thread {

    private final JDISession session;
    private Set<JDISession.JDIEventCallback> eventCallbacks;
    private volatile boolean connected;

    public JDIEventHandler(JDISession session) {
        this.session = session;
        this.eventCallbacks = eventCallbacks;
        this.connected = true;
    }

    @Override
    public void run() {
        EventQueue queue = session.getVM().eventQueue();
        EventSet eventSet = null;
        while (connected) {
            try {
                eventSet = queue.remove();
                for (Event event : eventSet) {
                    handleEvent(event);
                }
                eventSet.resume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (VMDisconnectedException e) {
                handleVMDisconnectedException();
                break;
            }
        }
    }

    private void handleVMDisconnectedException() {
        EventQueue queue = session.getVM().eventQueue();
        EventSet eventSet = null;
        while (connected) {
            try {
                eventSet = queue.remove();
                for (Event event : eventSet) {
                    if (event instanceof VMDeathEvent) {
                        vmDeathEvent((VMDeathEvent) event);
                    }
                    else if (event instanceof VMDisconnectEvent) {
                        vmDisconnectEvent((VMDisconnectEvent) event);
                    }
                    handleEvent(event);
                }
                eventSet.resume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void vmDeathEvent(VMDeathEvent event) {
        connected = false;
    }

    private void vmDisconnectEvent(VMDisconnectEvent event) {
        connected = false;
    }

    private void handleEvent(Event event) {
        Iterator<JDISession.JDIEventCallback> itr = eventCallbacks.iterator();
        JDISession.JDIEventCallback cb = null;
        while (itr.hasNext()) {
            cb = itr.next();
            cb.handleEvent(event);
        }
    }

    public boolean isVMConnected() {
        return this.connected;
    }
}
