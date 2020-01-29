package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class SocketHandler implements Runnable {

    private WebServer webServer;
    private Socket socket;
    private Log log;

    SocketHandler(WebServer webServer, Socket socket) {
        this.webServer = webServer;
        this.socket = socket;
        new Thread(this).start();
        log = new Log(WebServer.LOG_KIND);
    }

    public void run() {
        try {
            handle();
        } catch (IOException e) {
            log.out(Log.ERRORS, "Error: " + e.getMessage());
        }
    }

    private void handle() throws IOException {
        BufferedInputStream reader = null;
        PrintStream output = null;

        log.out(Log.MAIN, "Starting handle on port " + webServer.getPort());
        try {
            reader = new BufferedInputStream(socket.getInputStream());
            output = new PrintStream(socket.getOutputStream());
            final Http http = new Http(reader, output);

            log.out(Log.MAIN, "Method: " + http.getMethod());
            log.out(Log.MAIN, "Route: " + http.getRoute());

            if (http.getMethod().equals("OPTIONS")) {
                http.response(http.OK);
                return;
            }

            if (http.getMethod().equals("POST") && !http.getRoute().isEmpty()) {
                ContentFile contentFile = http.getFileInfo();
                log.out(Log.MAIN, "Receiving file");
                log.out(Log.MAIN, "Name: " + contentFile.name);
                log.out(Log.MAIN, "Size: " + contentFile.size);
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
                log.out(Log.MAIN, "Stop loop. Port " + webServer.getPort());
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
            log.out(Log.MAIN, "End handle on port " + webServer.getPort());
            if (socket != null) {
                socket.close();
            }
        }
    }

}
