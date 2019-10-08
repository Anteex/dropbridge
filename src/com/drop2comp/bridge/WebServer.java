package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class WebServer implements Runnable {

    private boolean isRunning;
    private ServerSocket serverSocket;
    private ContentFile contentFile;
    private OnStopListener onStopListener;
    private final int port;

    public WebServer(int port) {
        this.port = port;
    }

    public void start(ContentFile contentFile) {
        isRunning = true;
        this.contentFile = contentFile;
        new Thread(this).start();
        System.out.println("Start service on port " + getPort());
    }

    public void stop() {
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
        onStopListener.onStop();
        System.out.println("Stop service on port " + getPort());
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        System.out.println("Running ...");
        try {
            serverSocket = new ServerSocket(port);
            while (isRunning) {
                Socket socket = serverSocket.accept();
                SocketHandler socketHandler = new SocketHandler(socket, getPort(), contentFile);
            }
            stop();
        } catch (SocketException e) {
            System.out.println("Service error:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Service error:" + e.getMessage());
        }
    }

    public void setOnStopListener(OnStopListener onStopListener) {
        this.onStopListener = onStopListener;
    }

}

