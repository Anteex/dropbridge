package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

class Http {

    static final int OK = 1;
    static final int ERROR = 2;

    private String route = "";
    private String method = "";
    private BufferedInputStream inputStream;
    private PrintStream outputStream;
    private Log log;
    private static int BUFFER_SIZE = 4 * 1024;

    Http(BufferedInputStream inputStream, PrintStream outputStream) {
        log = new Log(WebServer.LOG_KIND);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        readHeader();
    }

    String getRoute() {
        return route;
    }

    String getMethod() {
        return method;
    }

    void response(int responceCode) {
        log.out(Log.MAIN,"Response ...");
        switch (responceCode) {
            case OK :
                log.out(Log.MAIN, "OK");
                outputStream.println("HTTP/1.0 200 OK");
                outputStream.println("Access-Control-Allow-Origin: *");
                outputStream.println("Access-Control-Allow-Headers: cache-control");
                outputStream.println();
                outputStream.flush();
                break;
            case ERROR :
                log.out(Log.MAIN, "ERROR");
                outputStream.println("HTTP/1.0 500 Internal Server Error");
                outputStream.println();
                outputStream.flush();
                break;
        }
    }

    void response(ContentFile contentFile) throws IOException {
        log.out(Log.MAIN, "Response file ...");
        outputStream.println("HTTP/1.0 200 OK");
        outputStream.println("Content-Description: File Transfer");
        outputStream.println("Content-Type: application/octet-stream");
        outputStream.println("Content-Disposition: attachment; filename=" + contentFile.name);
        outputStream.println("Content-Transfer-Encoding: binary");
        outputStream.println("Connection: Keep-Alive");
        outputStream.println("Expires: 0");
        outputStream.println("Cache-Control: must-revalidate, post-check=0, pre-check=0");
        outputStream.println("Pragma: public");
        outputStream.println("Content-Length: " + contentFile.size);
        outputStream.println();
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            long fileSize = contentFile.size;
            int readSize;
            while ((readSize = contentFile.stream.read(buffer)) != -1) {
                if (fileSize > readSize) {
                    fileSize -= readSize;
                    outputStream.write(buffer, 0, readSize);
                } else {
                    readSize = (int) fileSize;
                    outputStream.write(buffer, 0, readSize);
                    break;
                }
            }
            outputStream.flush();
        } catch (Exception e) {
            log.out(Log.ERRORS, "Error: " + e.getMessage());
        }
    }


    ContentFile getFileInfo() {
        ArrayList<String> info = new ArrayList<String>();
        readExtraHeader(info);
        return new ContentFile(info);
    }

    private void readExtraHeader(ArrayList<String> info) {
        for (int i=0; i<2; i++) {
            String line;
            boolean firstLine = true;
            while (!(line = readLine(this.inputStream)).isEmpty() || firstLine) {
                firstLine = false;
                if (!line.isEmpty()) {
                    info.add(line);
                    log.out(Log.HEADERS, line);
                }
            }
        }
    }

    private void readHeader() {
        String line;
        while (!(line = readLine(this.inputStream)).isEmpty()) {
            log.out(Log.HEADERS, line);
            if (line.startsWith("GET /") || line.startsWith("POST /") || line.startsWith("OPTIONS /")) {
                method = extractMethod(line);
                route = extractRoute(line);
            }
        }
    }

    private String extractRoute(String line) {
        int start = line.indexOf('/') + 1;
        int end = line.indexOf(' ', start);
        String route = line.substring(start, end);
        if (route.contains("?")) route = route.substring(0, route.indexOf("?"));
        return route;
    }

    private String extractMethod(String line) {
        int start = 0;
        int end = line.indexOf(' ', start);
        return line.substring(start, end);
    }

    private String readLine(BufferedInputStream in) {
        String res = "";
        int r;
        try {
            while ((r = in.read()) != 13) {
                res += (char) r;
            }
            in.read();
        }
        catch (IOException e) {
            return "";
        }
        return res;
    }

}
