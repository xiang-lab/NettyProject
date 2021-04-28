package com.kinight.netty.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseMappingCallback {

    private static ConcurrentHashMap<Long, CompletableFuture> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long requestID, CompletableFuture cb) {
        mapping.putIfAbsent(requestID, cb);
    }

    public static void runCallback(Packmsg msg) {
        CompletableFuture cf = mapping.get(msg.getHeader().getRequestID());
//        runnable.run();
        cf.complete(msg.getContent().getRes());
        removeCallBack(msg.getHeader().getRequestID());
    }

    private static void removeCallBack(long requestID) {
        mapping.remove(requestID);
    }

}
