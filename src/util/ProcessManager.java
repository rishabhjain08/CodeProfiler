package util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by rishajai on 9/25/16.
 */
public class ProcessManager {

    private String cmd;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean stopReadThread;
    private Deque<ProcessOutputLine> readBuffer;
    private Receiver receiver;
    private long startTimeoutInMillis;
    private Set<ReceiverEventsCallback> receiverEventsCallbacks;

    public ProcessManager (String cmd)
    {
        this.cmd = cmd;
        this.stopReadThread = false;
        this.readBuffer = new ConcurrentLinkedDeque<>();
        this.receiver = new Receiver();
        this.receiverEventsCallbacks = new HashSet<>();
    }

    private Thread readThread = new Thread() {
        @Override
        public void run() {
            String line = null;
            long counter = 1L;
            ProcessOutputLine processOutputLine;
            try {
                while (!stopReadThread && (line = reader.readLine()) != null) {
                    processOutputLine = new ProcessOutputLine(line, counter);
                    readBuffer.offerLast(processOutputLine);
                    for (ReceiverEventsCallback cb : receiverEventsCallbacks) {
                        cb.outputAvailable(processOutputLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public class ProcessOutputLine {
        private String message;
        private long counter;
        public ProcessOutputLine(String message, long counter) {
            this.message = message;
            this.counter = counter;
        }
        public String getMessage() {
            return message;
        }
    }

    public boolean start(long startTimeoutInMillis, ReceiverCheck processStartedCheck) {
        long startedAtMillis = System.currentTimeMillis();
        this.startTimeoutInMillis = startTimeoutInMillis;
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);
        try {
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        reader = new BufferedReader (new InputStreamReader(process.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        readThread.start();
        long currTime = System.currentTimeMillis();
        while (processStartedCheck.checkStatus(receiver) == ReceiverCheck.Status.IDLE
                && (currTime - startedAtMillis) < startTimeoutInMillis) {
            try {
                Thread.sleep(200);
                currTime += 200;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (processStartedCheck.checkStatus(receiver) == ReceiverCheck.Status.FAILED) {
            this.stop();
            return false;
        }
        return true;
    }

    public void stop() {
        stopReadThread = true;
        process.destroyForcibly();
        try {
            readThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean send(String str) {
        boolean success = false;
        try {
            writer.write(str);
            success =  true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public class Receiver {
        public synchronized ProcessOutputLine peek() {
            return readBuffer.peekFirst();
        }

        public synchronized ProcessOutputLine poll() {
            return readBuffer.pollFirst();
        }

        public synchronized void clear() {
            readBuffer.clear();
        }

        public synchronized void clearUntil (ProcessOutputLine line) {
            while (!readBuffer.isEmpty()
                    && readBuffer.peekFirst().counter <= line.counter) {
                readBuffer.pollFirst();
            }
        }

        public synchronized int size() {
            return readBuffer.size();
        }

        public Iterator<ProcessOutputLine> iterator() {
            // wrapping like this so that we dont expose remove()
            return new Iterator<ProcessOutputLine>() {
                Iterator<ProcessOutputLine> itr = readBuffer.iterator();
                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public ProcessOutputLine next() {
                    try {
                        return itr.next();
                    } catch (NoSuchElementException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }

        public boolean isClosed() {
            return process.isAlive();
        }

        public void subscribeToReceiverEvents(ReceiverEventsCallback cb) {
            receiverEventsCallbacks.add(cb);
        }
    }

    public interface ReceiverCheck {
        public enum Status {
            SUCCESS,
            IDLE,
            FAILED;
        }
        public Status checkStatus(Receiver receiver);
    }

    public interface ReceiverEventsCallback {
        /**
         * Should not take long.
         * @param line is the message received
         */
        public void outputAvailable(ProcessOutputLine line);
    }

    public Receiver getReceiver() {
        return this.receiver;
    }

}
