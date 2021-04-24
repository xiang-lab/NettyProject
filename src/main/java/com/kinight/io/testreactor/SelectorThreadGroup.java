package com.kinight.io.testreactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {

    private SelectorThread[] sts;
    private ServerSocketChannel server;
    AtomicInteger xid = new AtomicInteger(0);

    public SelectorThreadGroup(int num) {
        // num 线程数
        sts = new SelectorThread[num];
        for (int i = 0; i < num; i++) {
            sts[i] = new SelectorThread(this);

            new Thread(sts[i]).start();
        }
    }

    //
    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            // 注册到哪个selector上呢
            nextSelector(server);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 无论server是serversocke还是socket，都复用这个方法
    public void nextSelector(Channel c) {
        // 重点: c有可能是server有可能是client
        SelectorThread st = next(); // 在main线程中，取到堆里的selectorThread对象

        // 1. 通过队列传递数据
        st.lbq.add(c);
        // 2. 通过打断阻塞，让对应线程去自己在打断后，完成注册selector
        st.selector.wakeup();



//        // 呼应上，int nums = selector.select();
//        ServerSocketChannel s = (ServerSocketChannel) c;
//        try {
//            st.selector.wakeup();   // 功能是让selector的select()方法，立刻返回不阻塞
//            s.register(st.selector, SelectionKey.OP_ACCEPT);    // 该方法会被阻塞
//        } catch (ClosedChannelException e) {
//            e.printStackTrace();
//        }

    }

    private SelectorThread next() {
        int index = xid.incrementAndGet() % sts.length;
        return sts[index];
    }
}
