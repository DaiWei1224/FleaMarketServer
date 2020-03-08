package com.example.myapplication;

import java.io.Serializable;

public class User implements Serializable {
    String account;
    String password;
    public User(String act, String pwd){
        account = act;
        password = pwd;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}