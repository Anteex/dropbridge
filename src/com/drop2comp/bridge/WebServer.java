package com.drop2comp.bridge;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class WebServer implements Runnable {

    HashMap<String, ContentFile> contentFiles;
    static final byte LOG_KIND = Log.HEADERS | Log.MAIN | Log.ERRORS;
    private boolean isRunning;
    private ServerSocket serverSocket;
    private final int port;
    private Log log;

    WebServer(int port) {
        this.port = port;
        contentFiles = new HashMap<>();
        log = new Log(WebServer.LOG_KIND);
    }

    void start() {
        isRunning = true;
        new Thread(this).start();
        log.out(Log.MAIN,"Start service on port " + getPort());
    }

    int getPort() {
        return port;
    }

    private void stop() {
        try {
            isRunning = false;
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            log.out(Log.ERRORS, "Error closing the server socket: " + e.getMessage());
        }
        serverSocket = null;
        log.out(Log.MAIN, "Stop service on port " + getPort());
    }

    @Override
    public void run() {
        log.out(Log.MAIN, "Running ...");
        try {
            serverSocket = new ServerSocket(port);
            while (isRunning) {
                Socket socket = serverSocket.accept();
                new SocketHandler(this, socket);
            }
            stop();
        } catch (SocketException e) {
            log.out(Log.ERRORS, "Socket service error:" + e.getMessage());
        } catch (IOException e) {
            log.out(Log.ERRORS, "IO service error:" + e.getMessage());
        }
    }

}

