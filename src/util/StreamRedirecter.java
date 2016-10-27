package util;

import java.io.*;

/**
 * Created by rishajai on 9/30/16.
 */
public class StreamRedirecter extends Thread {

    private String description;
    private InputStreamReader reader;
    private OutputStreamWriter writer;

    private static final int BUFFER_SIZE = 2048;

    public StreamRedirecter(String description, InputStream from, OutputStream to) {
        this.description = description;
        reader = new InputStreamReader(from);
        writer = new OutputStreamWriter(to);
    }

    @Override
    public void run() {
        int count = 0;
        char[] charBuffer = new char[BUFFER_SIZE];
        try {
            while ((count = reader.read(charBuffer, 0, BUFFER_SIZE)) >= 0) {
                writer.write(charBuffer, 0, count);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
