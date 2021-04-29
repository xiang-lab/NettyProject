package com.kinight.netty.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1. 先假设一个需求，写一个RPC
 * 2. 来回通信，连接数量，拆包
 * 3. 动态代理，序列化与反序列化，协议封装
 * 4. 连接池
 */
public class MyRPCTest {

    /**
     * 客户端
     */
    @Test
    public void get() {

        new Thread(() -> {
            startServer();
        }).start();

        System.out.println("server started......");

        AtomicInteger num = new AtomicInteger(0);

        int size = 50;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car = proxyGet(Car.class);   // 动态代理
                String arg = "hello" + num.incrementAndGet();
                String res = car.ooxx(arg);
                System.out.println("client over msg: " + res + " src arg: " + arg);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 服务端
     */
    public void startServer() {


        MyCar car = new MyCar();
        MyFly fly = new MyFly();

        Dispatcher dis = new Dispatcher();
        dis.register(Car.class.getName(), car);
        dis.register(Fly.class.getName(), fly);


        NioEventLoopGroup boss = new NioEventLoopGroup(20);
        NioEventLoopGroup worker = boss;

        ServerBootstrap sbs = new ServerBootstrap();
        ChannelFuture bind = sbs.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        System.out.println("server accept client port: " + ch.remoteAddress().getPort());
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new ServerDecode());  // 先将解码器置入
                        p.addLast(new ServerRequestHandler(dis));
                    }
                })
                .bind(new InetSocketAddress("localhost", 9091));

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> T proxyGet(Class<T> interfaceInfo) {
        // 实现各个版本的动态代理

        ClassLoader loader = interfaceInfo.getClassLoader();
        Class<?>[] methodInfo = {interfaceInfo};

        return (T) Proxy.newProxyInstance(loader, methodInfo, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                // 如何设计我们的consumer对于provider的调用过程

                // 1. 调用服务，方法，参数 ---> 封装成message [content]
                // 准备消息体
                String name = interfaceInfo.getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                MyContent content = new MyContent();
                content.setName(name);
                content.setMethodName(methodName);
                content.setParameterTypes(parameterTypes);
                content.setArgs(args);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(out);
                oout.writeObject(content);
                // TODO: 解决数据decode问题
                // TODO: Server: dispatcher Executor
                byte[] msgBody = out.toByteArray();

                // 2. requestID + message, 本地要缓存
                // 协议：[header<>][msgBody]
                // 准备消息头
                MyHeader header = createHeader(msgBody);

                out.reset();
                oout = new ObjectOutputStream(out);
                oout.writeObject(header);
                byte[] msgHeader = out.toByteArray();

                // 3. 连接池：取得连接
                ClientFactory factory = ClientFactory.getFactory();
                NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9091));
                // 获取连接过程中：开始-创建，过程-直接取

                // 4. 发送 ---> 走IO ---> 走netty
                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);

                long id = header.getRequestID();
//                CountDownLatch countDownLatch = new CountDownLatch(1);
//                ResponseMappingCallback.addCallBack(id, new Runnable() {
//                    @Override
//                    public void run() {
//                        countDownLatch.countDown();
//                    }
//                });
                CompletableFuture<String> res = new CompletableFuture<>();
                ResponseMappingCallback.addCallBack(id, res);

                byteBuf.writeBytes(msgHeader);
                byteBuf.writeBytes(msgBody);
                ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
                channelFuture.sync();   // IO是双向的，看似有个sync，仅仅代表out

//                countDownLatch.await();

                // 5. IO未来回来了，怎么将代码执行到这里

                // (睡眠/回调，如何让线程停下来)

                return res.get();   // 阻塞
            }
        });
    }

    public static MyHeader createHeader(byte[] msg) {
        MyHeader header = new MyHeader();
        int size = msg.length;
        int f = 0x14141414; // 0x14  0001 0100
        long requestID = Math.abs(UUID.randomUUID().getLeastSignificantBits());

        header.setFlag(f);
        header.setDataLen(size);
        header.setRequestID(requestID);

        return header;
    }


}
