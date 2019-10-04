package com.drop2comp.bridge;

public class Main {

    private static WebServer webServer;

    public static void main(String[] args) {
	    webServer = new WebServer(8033);
	    webServer.start(null);
    }
}
