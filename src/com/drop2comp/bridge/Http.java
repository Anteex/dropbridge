package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Http {

    public static final int OK = 1;
    public static final int ERROR = 2;

    private String route = "";
    private String method = "";
    private BufferedInputStream inputStream;
    private PrintStream outputStream;
    private static int BUFFER_SIZE = 4 * 1024;

    Http(BufferedInputStream inputStream, PrintStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        readHeader();
    }

    public String getRoute() {
        return route;
    }

    public String getMethod() {
        return method;
    }

    public void response(int responceCode) {
        System.out.println("Response ...");
        switch (responceCode) {
            case OK :
                System.out.println("OK");
                outputStream.println("HTTP/1.0 200 OK");
                outputStream.println("Access-Control-Allow-Origin: *");
                outputStream.println("Access-Control-Allow-Headers: cache-control");
                outputStream.flush();
                break;
            case ERROR :
                System.out.println("ERROR");
                outputStream.println("HTTP/1.0 500 Internal Server Error");
                outputStream.flush();
                break;
        }
    }

    public void response(ContentFile contentFile) throws IOException {
        System.out.println("Response file ...");
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
            System.out.println("Error: " + e.getMessage());
        }
    }


    public ContentFile getFileInfo() {
        ArrayList<String> info = new ArrayList<String>();
        readExtraHeader(info);
        return new ContentFile(info);
    }

    private void readExtraHeader(ArrayList<String> info) {
        for (int i=0; i<2; i++) {
            String line;
            while (!(line = readLine(this.inputStream)).isEmpty()) {
                info.add(line);
            }
        }
    }

    private void readHeader() {
        String line;
        while (!(line = readLine(this.inputStream)).isEmpty()) {
//            System.out.println(line);
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
        String method = line.substring(start, end);
        return method;
    }

    private String readLine(BufferedInputStream in) {
        String res = "";
        int r;
        try {
            while (in.available() > 0 && (r = in.read()) != 13) {
//                System.out.println(in.available());
                res += (char) r;
            }
            System.out.println(res);
            in.read();
        }
        catch (IOException e) {
            return "";
        }
        return res;
    }

}
