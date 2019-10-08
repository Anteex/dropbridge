package com.drop2comp.bridge;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class WebServer implements Runnable {

    HashMap<String, ContentFile> contentFiles;
    private boolean isRunning;
    private ServerSocket serverSocket;
    private final int port;

    WebServer(int port) {
        this.port = port;
        contentFiles = new HashMap<>();
    }

    void start() {
        isRunning = true;
        new Thread(this).start();
        System.out.println("Start service on port " + getPort());
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
            System.out.println("Error closing the server socket: " + e.getMessage());
        }
        serverSocket = null;
        System.out.println("Stop service on port " + getPort());
    }

    @Override
    public void run() {
        System.out.println("Running ...");
        try {
            serverSocket = new ServerSocket(port);
            while (isRunning) {
                Socket socket = serverSocket.accept();
                new SocketHandler(this, socket);
            }
            stop();
        } catch (SocketException e) {
            System.out.println("Socket service error:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO service error:" + e.getMessage());
        }
    }

}

