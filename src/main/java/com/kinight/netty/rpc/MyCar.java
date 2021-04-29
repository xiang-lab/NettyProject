package com.kinight.netty.rpc;

public class MyCar implements Car {

    @Override
    public String ooxx(String msg) {

        System.out.println("server, get client arg: " + msg);

        return "server res " + msg;
    }
}
