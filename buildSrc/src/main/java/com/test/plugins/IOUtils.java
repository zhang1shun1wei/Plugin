package com.test.plugins;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {
    public static byte[] read(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        int len;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
    }
}
