package com.springboot.dgumarket.payload.request;

/**
 * Created by TK YOUN (2020-11-17 오후 8:16)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

public class Greeting {

    private String content;

    public Greeting() {
    }

    public Greeting(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}