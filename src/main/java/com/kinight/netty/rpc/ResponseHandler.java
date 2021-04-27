package com.kinight.netty.rpc;

import java.util.concurrent.ConcurrentHashMap;

public class ResponseHandler {

    private static ConcurrentHashMap<Long, Runnable> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long requestID, Runnable cb) {
        mapping.putIfAbsent(requestID, cb);
    }

    public static void runCallback(long requestID) {
        Runnable runnable = mapping.get(requestID);
        runnable.run();
        removeCallBack(requestID);
    }

    private static void removeCallBack(long requestID) {
        mapping.remove(requestID);
    }

}
