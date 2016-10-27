package jdi;

import com.intellij.openapi.project.Project;
import com.sun.jdi.VirtualMachine;
import jdi.exceptions.AlreadyConnectedException;
import jdi.exceptions.ConnectorNotFoundException;
import jdi.exceptions.UnknownException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rishajai on 9/30/16.
 */
public class JDISessionFactory {

    private static Map<VMFactory.VMIdentifier, JDISession> sessionCache = new HashMap<>();
    private static final int CONNECTION_TIMEOUT_MILLIS = 10000;

    public synchronized static JDISession socketAttach (Project project, String host, int port)
            throws AlreadyConnectedException, ConnectorNotFoundException, UnknownException {
        VMFactory.VMIdentifier id = VMFactory.SocketAttachingConnector.getIdentifier(project, host, port, CONNECTION_TIMEOUT_MILLIS);
        JDISession session = null;
        session = sessionCache.get(id);
        if (session == null || !session.isSessionAlive()) {
            VirtualMachine vm = VMFactory.SocketAttachingConnector.attach(project, host, port, CONNECTION_TIMEOUT_MILLIS);
            if (vm == null) {
                throw new UnknownException("Failed to attach to the JVM");
            }
            session = new JDISession(project, vm);
            sessionCache.put(id, session);
            session.start();
            return session;
        }
        else
        {
            throw new AlreadyConnectedException("");
        }
    }
}
