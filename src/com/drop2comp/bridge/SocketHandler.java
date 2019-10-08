package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class SocketHandler implements Runnable {

    private WebServer webServer;
    private Socket socket;

    SocketHandler(WebServer webServer, Socket socket) {
        this.webServer = webServer;
        this.socket = socket;
        new Thread(this).start();
    }

    public void run() {
        try {
            handle();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handle() throws IOException {
        BufferedInputStream reader = null;
        PrintStream output = null;

        System.out.println("Starting handle on port " + webServer.getPort());
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

            if (http.getMethod().equals("POST") && !http.getRoute().isEmpty()) {
                ContentFile contentFile = http.getFileInfo();
                System.out.println("Receiving file");
                System.out.println("Name: " + contentFile.name);
                System.out.println("Size: " + contentFile.size);
                contentFile.stream = reader;
                contentFile.id = http.getRoute();
                webServer.contentFiles.put(http.getRoute(), contentFile);
                while (webServer.contentFiles.containsKey(http.getRoute())) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                http.response(Http.OK);
                System.out.println("Stop loop. Port " + webServer.getPort());
            }

            if (http.getMethod().equals("GET")) {
                if (!http.getRoute().isEmpty() && webServer.contentFiles.containsKey(http.getRoute())) {
                    http.response(webServer.contentFiles.get(http.getRoute()));
                    webServer.contentFiles.remove(http.getRoute());
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
            System.out.println("End handle on port " + webServer.getPort());
            if (socket != null) {
                socket.close();
            }
        }
    }

}
