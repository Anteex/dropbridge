package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class SocketHandler implements Runnable {

    private Socket socket;
    private boolean isWaiting;
    private ContentFile contentFile;
    private WebServer transmitServer;
    final private int port;

    SocketHandler(Socket socket, int port, ContentFile contentFile) {
        this.socket = socket;
        this.port = port;
        this.contentFile = contentFile;
        new Thread(this).start();
    }

    public void run() {
        try {
            isWaiting = false;
            handle();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handle() throws IOException {
        BufferedInputStream reader = null;
        PrintStream output = null;

        System.out.println("Starting handle on port " + port);
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
                        System.out.println("Stop loop. Port " + port);
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
                    //stop();
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
            System.out.println("End handle on port " + port);
            socket.close();
        }
    }

    private void startTransmition(ContentFile transmitHeader) {
        transmitServer = new WebServer(8055);
        transmitServer.start(transmitHeader);
    }



}
