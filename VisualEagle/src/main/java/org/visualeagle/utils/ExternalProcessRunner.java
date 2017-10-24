package org.visualeagle.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.IOUtils;

public class ExternalProcessRunner {

    public boolean run(String[] args, File workingFolder, LineCallback lineCallback, boolean printToStdout) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(args).directory(workingFolder);
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();
        AtomicInteger streamCounter = new AtomicInteger();
        streamCopy(inputStream, lineCallback, printToStdout, streamCounter);
        streamCopy(errorStream, lineCallback, printToStdout, streamCounter);
        try {
            int result = process.waitFor();
            while (streamCounter.get() != 0) {
                Thread.sleep(10);
            }
            return result == 0;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private void streamCopy(InputStream stream, LineCallback lineCallback, boolean printToStdOut, AtomicInteger streamCounter) {
        streamCounter.incrementAndGet();
        Thread thread = new Thread("StreamCopier") {
            @Override
            public void run() {
                Scanner scanner = new Scanner(IOUtils.buffer(stream));
                boolean firstLine = true;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (printToStdOut) {
                        System.out.println(line);
                    }

                    if (lineCallback != null) {
                        lineCallback.onNextLine(line, firstLine);
                    }

                    firstLine = false;
                }
                streamCounter.decrementAndGet();
            }
        };

        thread.setDaemon(true);
        thread.start();
    }
}
