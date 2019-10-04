package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class WebServer implements Runnable {

    private boolean isRunning, isWaiting;
    private ServerSocket serverSocket;
    private WebServer transmitServer;
    private ContentFile contentFile;
    private OnStopListener onStopListener;
    private final int port;

    public WebServer(int port) {
        this.port = port;
    }

    public void start(ContentFile contentFile) {
        isRunning = true;
        isWaiting = false;
        this.contentFile = contentFile;
        new Thread(this).start();
        System.out.println("Start service on port " + getPort());
    }

    public void stop() {
        try {
            isRunning = false;
            isWaiting = false;
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
                handle(socket);
                socket.close();
            }
        } catch (SocketException e) {
            System.out.println("Service error:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Service error:" + e.getMessage());
        }
    }

    public void setOnStopListener(OnStopListener onStopListener) {
        this.onStopListener = onStopListener;
    }

    private void handle(Socket socket) throws IOException {
        BufferedInputStream reader = null;
        PrintStream output = null;

        System.out.println("Starting handle on port " + getPort());
        try {
            reader = new BufferedInputStream(socket.getInputStream());
            output = new PrintStream(socket.getOutputStream());
            final Http http = new Http(reader, output);

            System.out.println("Method: " + http.getMethod());
            System.out.println("Route: " + http.getRoute());

            if (http.getMethod().equals("OPTIONS")) {
                http.response(http.OK);
                return;
            }

            if (http.getMethod().equals("POST")) {
                ContentFile contentFile = http.getFileInfo();
                System.out.println("Receiving file");
                System.out.println("Name: " + contentFile.name);
                System.out.println("Size: " + contentFile.size);
                contentFile.stream = reader;
                startTransmition(contentFile);
                transmitServer.setOnStopListener(new OnStopListener() {
                    @Override
                    public void onStop() {
                        http.response(Http.OK);
                        isWaiting = false;
                        System.out.println("Stop loop. Port " + getPort());
                    }
                });
                isWaiting = true;
                int a = 0;
                while (isWaiting) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            if (http.getMethod().equals("GET")) {
                if (contentFile != null) {
                    http.response(contentFile);
                    stop();
                } else {
                    http.response(Http.OK);
                }
            }

        } finally {
            if (output != null) {
                output.close();
            }
            if (reader != null) {
                reader.close();
            }
            System.out.println("End handle on port " + getPort());
        }
    }

    private void startTransmition(ContentFile transmitHeader) {
        transmitServer = new WebServer(8055);
        transmitServer.start(transmitHeader);
    }

}

