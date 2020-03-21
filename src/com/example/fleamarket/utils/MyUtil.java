package com.example.fleamarket.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyUtil {

    public static byte[] loadImageFromFile(File image) {
        try {
            InputStream is = new FileInputStream(image);
            byte[] data = new byte[(int)image.length()];
            is.read(data);
            is.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
