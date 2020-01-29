package com.drop2comp.bridge;

public class Log {

    static final byte NONE    = 0b00000000;
    static final byte MAIN    = 0b00000001;
    static final byte HEADERS = 0b00000010;
    static final byte ERRORS  = 0b00000100;

    private byte mask;

    Log(byte mask) {
        this.mask = mask;
    }

    void out(byte kind, String message) {
        if ((mask & kind) != 0) {
            System.out.println(message);
        }
    }
}
