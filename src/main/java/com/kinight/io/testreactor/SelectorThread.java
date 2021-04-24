package com.kinight.io.testreactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectorThread implements Runnable {

    // 每线程对应一个selector，多线程情况下，该程序的并发客户端被分配到多个selector上
    // 注：每个客户端，只绑定到其中一个selector上
    // 其实不会有交互问题

    Selector selector = null;

    // 队列：线程的栈是独立的，堆是共享的
    LinkedBlockingQueue<Channel> lbq = new LinkedBlockingQueue<>();

    SelectorThreadGroup stg;

    public SelectorThread(SelectorThreadGroup stg) {
        try {
            this.stg = stg;
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        // Loop
        while (true) {
            try {
                // 1. select()
                // System.out.println(Thread.currentThread().getName() + " : before select " + selector.keys().size());
                int nums = selector.select();   // 可能会阻塞，需要wakeup()
                // System.out.println(Thread.currentThread().getName() + " : after select " + selector.keys().size());
                // 2. 处理selectkeys
                if (nums > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {    // 线性处理
                        SelectionKey key = iter.next();
                        iter.remove();

                        if (key.isAcceptable()) {   // 接受客户端的过程相对复杂（接受之后需要注册，但在多线程下，新的客户端需要注册到哪个selector）
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }
                }

                // 3. 处理一些task
                if (!lbq.isEmpty()) {
                    Channel c = lbq.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + " register listen");
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                        System.out.println(Thread.currentThread().getName() + " register client: " + client.getRemoteAddress());
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "  acceptHandler......");

        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);    // 设置为非阻塞

            // choose a selector and register
            // 客户端的注册
            stg.nextSelector(client);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "  readHandler......");

        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        while (true) {
            try {
                int num = client.read(buffer);
                if (num > 0) {
                    buffer.flip();  // 将读到的内容进行翻转，然后直接写出
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (num == 0) {
                    break;
                } else {
                    // num < 0 客户端断开
                    System.out.println("client: " + client.getRemoteAddress() + " closed....");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
