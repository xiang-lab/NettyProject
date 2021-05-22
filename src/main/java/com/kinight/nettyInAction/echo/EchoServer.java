package com.kinight.nettyInAction.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();      // 1. 创建EventLoopGroup

        try {
            ServerBootstrap b = new ServerBootstrap();          // 2. 创建ServerBootStrap
            b.group(group)
                    .channel(NioServerSocketChannel.class)      // 3. 指定所使用的Nio传输Channel
                    .localAddress(new InetSocketAddress(port))  // 4. 使用指定的端口设置套接字地址
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 5. 添加EchoServerHandler到子Channel的ChannelPipeline
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // EchoServerHandler被标注为@Shareable，所以我们可以总是使用同样的实例
                            ch.pipeline().addLast(serverHandler);
                        }
                    });

            ChannelFuture f = b.bind().sync();  // 6. 异步绑定服务器，调用sync()方法阻塞等待，直到绑定完成
            f.channel().closeFuture().sync();   // 7. 获取Channel的ClostFuture，并且阻塞当前线程直到它完成
        } finally {
            group.shutdownGracefully().sync();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.out.println("Usage: " + EchoServer.class.getSimpleName() + " <Port>");
        }

        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();   // 调用服务器start()方法
    }

}
