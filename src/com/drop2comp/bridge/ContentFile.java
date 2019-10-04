package com.drop2comp.bridge;

import java.io.BufferedInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentFile {

    public BufferedInputStream stream;
    public long size = 0;
    public String name = "";
    private ArrayList<String> headerLines;

    ContentFile(ArrayList<String> headerLines) {
        this.headerLines = headerLines;
        extractName();
        extractSize();
    }

    private void extractSize() {
        String curr = null;
        for (String next : headerLines) {
            if (curr != null) {
                if (curr.contains("filesize")) {
                    try {
                        size = Long.parseLong(next);
                    } catch (NumberFormatException e) {
                        size = 0;
                    }
                    break;
                }
            }
            curr = next;
        }
    }

    private void extractName() {
        for (String line: headerLines) {
            if (line.contains("filename")) {
                Pattern p = Pattern.compile("filename=\"(.+?)\"");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    name = new String(m.group(1).getBytes(StandardCharsets.ISO_8859_1));
                }
            }
        }
    }
}
