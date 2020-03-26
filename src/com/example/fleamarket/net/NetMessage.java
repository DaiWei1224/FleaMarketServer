package com.example.fleamarket.net;

import java.io.Serializable;
import java.util.List;

public class NetMessage implements Serializable {

    private MessageType type;
    private String id;
    private String pw;
    private String nickname;
    private NetImage avatar;
    private Commodity commodity;
    private int commodityNum;
    private List<Commodity> commodityList;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public NetImage getAvatar() {
        return avatar;
    }

    public void setAvatar(NetImage avatar) {
        this.avatar = avatar;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public void setCommodity(Commodity commodity) {
        this.commodity = commodity;
    }

    public int getCommodityNum() {
        return commodityNum;
    }

    public void setCommodityNum(int commodityNum) {
        this.commodityNum = commodityNum;
    }

    public List<Commodity> getCommodityList() {
        return commodityList;
    }

    public void setCommodityList(List<Commodity> commodityList) {
        this.commodityList = commodityList;
    }

}
