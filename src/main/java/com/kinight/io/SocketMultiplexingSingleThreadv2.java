package com.kinight.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class SocketMultiplexingSingleThreadv2 {

    private ServerSocketChannel server = null;
    private Selector selector = null;       // linux的多路复用器  (selector poll epoll)
    int port = 9091;

    public void initServer() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            // 在epoll模型下，open->epoll_create -> fd3
            selector = Selector.open();     // selector poll epoll，优先选择：epoll

            // server 就是listen状态的fd4
            /**
             * select, poll: jvm中开辟一个数组，fd放进去
             * epoll: epoll_ctl(fd3, ADD, fd4, EPOLLIN)
             */
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        initServer();
        System.out.println("服务器启动了...");
        try {
            while (true){
//                Set<SelectionKey> keys = selector.keys();
//                System.out.println(keys.size() + " size");

                // 1. 调多路复用器(select, poll, epoll)
                /**
                 * select, poll: 其实调用内核的select(fd4)
                 * epoll: 其实调用的是epoll_wait()
                 *
                 * timeout: 如果没有时间，就是阻塞，
                 *          有时间，就是超时时间
                 *
                 * selector.wakeup() 结果返回0
                 */
                while (selector.select(50) > 0) {
                    // 返回有状态的fd集合
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectionKeys.iterator();

                    // 不管啥多路复用器，我只能拿到状态，只能一个一个的去处理R/W
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();  // 由于是Set，不移除会重复处理
                        if (key.isAcceptable()) {
                            // 重点，如果要去接受一个新的连接
                            // 语义上，accept接受连接并且建立新连接的FD
                            // select, poll：由于内核中没有空间，在jvm中保存，和fd4保存在一起
                            // epoll：通过epoll_ctl把客户端fd注册到内核空间
                            acceptHandler(key);
                        } else if (key.isReadable()){
                            // 在多路复用器中把key拿出来，因此通过key.cancel，防止多次触发
                            key.cancel();
                            // 即是是抛出了线程去读取，但是在时差里，这个key的read事件会被重复触发
                            readHandler(key);   // 只处理了read并注册，关心这个key的write事件
                        } else if (key.isWritable()) {
                            key.cancel();
                            // 什么时候开始写操作
                            // 1. 准备好要写什么
                            // 2. send-queue是否有空间
                            // 3. read开始就要注册，write需要依赖以上关系，什么时候用什么时候注册
                            // 4. 如果一开始就注册write的事件，就会进入死循环，没有意义
                            writeHandler(key);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptHandler(SelectionKey key) {
        try {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel client = ssc.accept();    // accept接受客户端
            client.configureBlocking(false);

            ByteBuffer buffer = ByteBuffer.allocate(8192);
            // 将client注册到selector中
            client.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("-------------------------------------------");
            System.out.println("新客户端：" + client.getRemoteAddress());
            System.out.println("-------------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readHandler(SelectionKey key) {
        new Thread(() -> {
            System.out.println("read handler...");
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            buffer.clear();
            int read = 0;
            try {
                while (true) {
                    read = client.read(buffer);
                    System.out.println(Thread.currentThread().getName() + " " + read);
                    if (read > 0) {
                        // 关心 OP_WRITE，其实就是关心send-queue是不是有空间
                        client.register(key.selector(), SelectionKey.OP_WRITE, buffer);
                    } else if (read == 0) {
                        break;
                    } else {
                        client.close();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void writeHandler(SelectionKey key) {
        new Thread(() -> {
            System.out.println("write handler...");
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            buffer.flip();
            while (buffer.hasRemaining()) {
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            buffer.clear();
            key.cancel();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SocketMultiplexingSingleThreadv2 service = new SocketMultiplexingSingleThreadv2();
        service.start();
    }


}

