package jdi;

import com.intellij.openapi.project.Project;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import jdi.exceptions.ConnectorNotFoundException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rishajai on 9/30/16.
 */
public class VMFactory {

    //private static Map<VMIdentifier, VirtualMachine> vmMap = new ConcurrentHashMap<VMIdentifier, VirtualMachine>();

    public enum ConnectionType {
        SOCKET_ATTACH("com.sun.jdi.SocketAttach");

        String className;
        ConnectionType(String className) {
            this.className = className;
        }

        public String getClassName() {
            return this.className;
        }

        public static ConnectionType fromConnectionName(String connectionName) {
            for (ConnectionType connectionType : ConnectionType.values()) {
                if (connectionType.getClassName().equals(connectionName)) {
                    return connectionType;
                }
            }
            return null;
        }
    }

    static class VMIdentifier {
        Project project;
        ConnectionType connectionName;
        Map<String, Connector.Argument> arguments;

        VMIdentifier (Project project, Map<String, Connector.Argument> arguments, ConnectionType connectionType) {
            this.project = project;
            this.arguments = arguments;
            this.connectionName = connectionType;
        }

        @Override
        public boolean equals (Object ob) {
            boolean result = false;
            if (!(ob instanceof VMIdentifier)) {
                result = false;
            }
            VMIdentifier vmIdentifier = (VMIdentifier) ob;
            // match the connection type
            if (!this.connectionName.equals(vmIdentifier.connectionName)) {
                return false;
            }
            // match the project
            if (!this.project.equals(vmIdentifier.project)) {
                return false;
            }
            // match the connection arguments
            if (this.arguments.size() != vmIdentifier.arguments.size()) {
                return false;
            }
            HashSet tempSet = new HashSet<String>(this.arguments.keySet());
            tempSet.removeAll(vmIdentifier.arguments.keySet());
            if (!tempSet.isEmpty()) {
                return false;
            }
            for (String key : this.arguments.keySet()) {
                if (!this.arguments.get(key).equals(vmIdentifier.arguments.get(key))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + project.getLocationHash().hashCode();
            hash = 17 * hash + connectionName.getClassName().hashCode();
            return hash;
        }
    }

    public static class SocketAttachingConnector {

        private static ConnectionType connectionType = ConnectionType.SOCKET_ATTACH;

        public static VirtualMachine attach(Project project, String hostname, int port, int timeoutMillis) throws ConnectorNotFoundException {
            AttachingConnector connector = getSocketAttachingConnector();
            if (connector == null) {
                throw new ConnectorNotFoundException("Couldnt find a socket attach connector");
            }

            VirtualMachine vm = null;
            try {
                // override connection parameters
                Map<String, Connector.Argument> arguments = prepareConnectionArguments(connector, hostname, port,
                        timeoutMillis);

                // lookup cache
                VMIdentifier identifier = new VMIdentifier(project, arguments, connectionType);
                //vm = getVMFromCache(identifier);

                // connect and cache
                vm = connector.attach(arguments);
                //putVMInCache(identifier, vm);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalConnectorArgumentsException e) {
                e.printStackTrace();
            }
            return vm;
        }

        public static Map<String, Connector.Argument> getDefaultArguments() {
            return getSocketAttachingConnector().defaultArguments();
        }

        private static AttachingConnector getSocketAttachingConnector() {
            List<AttachingConnector> connectors = Bootstrap.virtualMachineManager().attachingConnectors();
            AttachingConnector connector = null;
            for (AttachingConnector ac : connectors) {
                if (ac.name().equals(connectionType.getClassName())) {
                    connector = ac;
                    break;
                }
            }
            return connector;
        }

        private static Map<String, Connector.Argument> prepareConnectionArguments(AttachingConnector connector, String
                hostname, int port, int timeoutMillis) {
            Map<String, Connector.Argument> arguments = connector.defaultArguments();
            arguments.get("hostname").setValue(hostname);
            arguments.get("port").setValue(String.valueOf(port));
            if (timeoutMillis >= 0) {
                arguments.get("timeout").setValue(String.valueOf(timeoutMillis));
            }
            return arguments;
        }

        public static VMIdentifier getIdentifier(Project project, String hostname, int port, int timeoutMillis) {
            Map<String, Connector.Argument> arguments = prepareConnectionArguments(getSocketAttachingConnector(),
                    hostname, port, timeoutMillis);
            VMIdentifier identifier = new VMIdentifier(project, arguments, connectionType);
            return identifier;
        }

        /*
        private static VirtualMachine getVMFromCache(Project project, String hostname, int port, int timeoutMillis) {
            Map<String, Connector.Argument> arguments = prepareConnectionArguments(getSocketAttachingConnector(),
                    hostname, port, timeoutMillis);
            VMIdentifier identifier = new VMIdentifier(project, arguments, connectionType);
            return VMFactory.getVMFromCache(identifier);
        }
        */
    }

    /*
    private static VirtualMachine putVMInCache(VMIdentifier myIdentifier, VirtualMachine vm) {
        return vmMap.put(myIdentifier, vm);
    }

    private static VirtualMachine getVMFromCache(VMIdentifier myIdentifier) {
        return vmMap.get(myIdentifier);
    }

    private static VirtualMachine getVMFromCache(Project project, Map<String, Connector.Argument> arguments, String
            connectionName) {
        VMIdentifier myIdentifier = new VMIdentifier(project, arguments, ConnectionType.fromConnectionName(connectionName));
        return getVMFromCache(myIdentifier);
    }
    */

    public static void main (String[] args) {
        //VMFactory.SocketAttachingConnector.attach(null, "" , 4, 4);
    }
}
