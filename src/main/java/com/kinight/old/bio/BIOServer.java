package com.kinight.old.bio;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOServer {

    /**
     * 思路
     * 1. 创建一个线程池
     * 2. 如果有客户端连接, 就创建一个线程与之通信(单独写一个方法)
     */
    public static void main(String[] args) throws Exception {
        // 线程池
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

        // 创建一个ServerSocket
        ServerSocket serverSocket = new ServerSocket(6666);

        System.out.println("服务器启动了");
        while (true) {
            // 监听, 等待客户端连接(阻塞)
            System.out.println("等待连接");
            final Socket socket = serverSocket.accept();
            System.out.println("连接到一个客户端");

            // 就创建一个线程与之通信(单独写一个方法)
            newCachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    // 可以和客户端通信
                    handler(socket);
                }
            });

        }

    }

    // 编写一个handler方法, 和客户端通讯
    public static void handler(Socket socket) {
        try {
            byte[] bytes = new byte[1024];
            // 通过socket获取输入流
            InputStream inputStream = socket.getInputStream();

            // 循环读取客户端发送的数据
            while (true) {
                // 读取数据(阻塞)
                System.out.println("read......");
                int read = inputStream.read(bytes);
                if (read != -1) {
                    System.out.println(new String(bytes, 0, read));   // 输出客户端发送的数据
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("关闭和client的连接");
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}












