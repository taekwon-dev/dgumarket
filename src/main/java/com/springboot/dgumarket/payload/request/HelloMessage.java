package com.springboot.dgumarket.payload.request;

/**
 * Created by TK YOUN (2020-11-17 오후 8:17)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public class HelloMessage {

    private String name;

    public HelloMessage() {
    }

    public HelloMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}