package com.example.fleamarket.net;

import java.io.Serializable;

public class NetMessage implements Serializable {

    private MessageType type;
    private String id;
    private String pw;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }
}
