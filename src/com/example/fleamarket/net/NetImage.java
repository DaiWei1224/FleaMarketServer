package com.example.fleamarket.net;

import java.io.Serializable;

public class NetImage implements Serializable {
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
