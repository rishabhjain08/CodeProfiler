package jdb;

import util.ProcessManager;

import java.util.Iterator;

/**
 * Created by rishajai on 9/25/16.
 */
public class JDBSession {

    private String cmd;
    private ProcessManager processManager;
    private static final long sessionStartTimeoutMillis = 10000; //10 seconds

    protected JDBSession() {

    }

    private static ProcessManager.ReceiverCheck jdbStartedCheck = new ProcessManager.ReceiverCheck() {

        @Override
        public ProcessManager.ReceiverCheck.Status checkStatus(ProcessManager.Receiver receiver) {
            ProcessManager.ProcessOutputLine output = null;
            Status status = Status.IDLE;
            Iterator<ProcessManager.ProcessOutputLine> itr = receiver.iterator();
            while (itr.hasNext()) {
                output = itr.next();
                if (output.getMessage().indexOf("Initializing jdb …") != -1) {
                    status = Status.SUCCESS;
                    receiver.clearUntil(output);
                    break;
                }
            }
            if (receiver.isClosed() && status != Status.SUCCESS) {
                status = Status.FAILED;
            }
            return status;
        }
    };

    public static JDBSession attachToJVM(String pathToJDB, String host, int port) {
        JDBSession session = new JDBSession();
        String cmd = pathToJDB + " -attach " + host + ":" + port;
        session.cmd = cmd;
        session.processManager = new ProcessManager(cmd);
        session.processManager.start(sessionStartTimeoutMillis, jdbStartedCheck);
        return session;
    }





}
