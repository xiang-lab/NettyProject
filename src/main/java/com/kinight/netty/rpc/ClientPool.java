package com.kinight.netty.rpc;

import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientPool {

    protected NioSocketChannel[] clients;
    protected Object[] lock;

    public ClientPool(int size) {
        clients = new NioSocketChannel[size];
        lock = new Object[size];

        for (int i = 0; i < size; i++) {
            lock[i] = new Object();
        }
    }

}
