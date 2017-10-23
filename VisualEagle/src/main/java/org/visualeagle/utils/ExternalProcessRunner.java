package org.visualeagle.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExternalProcessRunner {
    private Process process;
    private AtomicBoolean printThreadFlag;
    private StringBuffer sb = new StringBuffer();

    public static ExternalProcessRunner startProgram( String[] args) throws IOException, InterruptedException {
     /*   Process process = new ProcessBuilder().command(args);
        final InputStream is = ps.process.getInputStream();
        final AtomicBoolean printThreadRunFlag = new AtomicBoolean(true);
        ps.printThreadFlag = printThreadRunFlag;
        Thread printThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    print(is, printThreadRunFlag, ps);
                } catch (IOException e) {
                    logger.warn("", e);
                }
            }
        });

        printThread.start();
        return ps;*/
     return null;
    }
/*

    private static void print(InputStream inputStream, AtomicBoolean runFlag, ExternalProcess process) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = in.readLine()) != null && runFlag.get()) {
            logger.info(line);
            process.sb.append(line).append("\n");
        }
        inputStream.close();
    }

    public int getExitValue() {
        return process != null ? process.exitValue() : 0;
    }

    public void waitFor() {
        if (process != null) {
            try {
                final int exitCode = process.waitFor();
                logger.info("application exit status: " + exitCode);
            } catch (InterruptedException e) {
                logger.warn("", e);
            }
            printThreadFlag.set(false);
        }
    }

    public String getOutput() {
        return sb.toString();
    }

    public void kill() {
        process.destroy();
    }

    public boolean isRunning() {
        if (process == null) {
            return false;
        }

        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public boolean logContains(String s) {
        return getOutput().contains(s);
    }*/
}