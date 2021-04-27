package com.kinight.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyNetty {

    /**
     * Netty对channel, buteBuffer, selector进行了封装
     * channel
     * byteBuffer
     * selector
     */

    @Test
    public void myBytebuf() {

        // ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8, 20);
        // 非池化
        // ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        // 池化
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);

        print(buf);


        buf.writeBytes(new byte[]{1,2,3,4});
        print(buf);

        buf.writeBytes(new byte[]{1,2,3,4});
        print(buf);

        buf.writeBytes(new byte[]{1,2,3,4});
        print(buf);

        buf.writeBytes(new byte[]{1,2,3,4});
        print(buf);

        buf.writeBytes(new byte[]{1,2,3,4});
        print(buf);
    }

    public static void print(ByteBuf buf) {
        System.out.println("buf.isReadable: " + buf.isReadable());
        System.out.println("buf.readerIndex: " + buf.readerIndex());
        System.out.println("buf.readableBytes: " + buf.readableBytes());
        System.out.println("buf.isWritable: " + buf.isWritable());
        System.out.println("buf.writerIndex: " + buf.writerIndex());
        System.out.println("buf.writableBytes: " + buf.writableBytes());
        System.out.println("buf.capacity: " + buf.capacity());
        System.out.println("buf.maxCapacity: " + buf.maxCapacity());
        System.out.println("buf.isDirect: " + buf.isDirect());
        System.out.println("--------------------------------");
    }

    /**
     * 客户端
     * 1. 主动发送数据
     * 2. 别人什么时候给我发送? selector
     */
    @Test
    public void loopExecutor() throws Exception {

        // Group就是一个线程池
        NioEventLoopGroup selector = new NioEventLoopGroup(2);
        selector.execute(() -> {
            try {
                while (true) {
                    System.out.println("hello world");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.in.read();
    }

    @Test
    public void clientMode() throws InterruptedException {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);

        // 客户端模式
        NioSocketChannel client = new NioSocketChannel();
        thread.register(client);    // epoll_ctl(5, ADD, 3)

        // 响应式：
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());

        // reactor 异步的特征
        ChannelFuture connect = client.connect(new InetSocketAddress("192.168.162.133", 9090));
        ChannelFuture sync = connect.sync();

        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        ChannelFuture send = client.writeAndFlush(buf);
        send.sync();



        sync.channel().closeFuture().sync();

        System.out.println("client over....");
    }





    @Test
    public void serverMode() throws InterruptedException {

        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();

        thread.register(server);

        // 指不定什么来客户端
        ChannelPipeline p = server.pipeline();
        p.addLast(new MyAcceptHandler(thread, new ChannelInit()));   // accept客户端，并且注册到selector

        ChannelFuture bind = server.bind(new InetSocketAddress("192.168.162.1", 9090));
        bind.sync().channel().closeFuture().sync();
    }


    /**
     * 官方代码
     */
    @Test
    public void nettyClient() throws InterruptedException {

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(group)
                .channel(NioSocketChannel.class)
//                .handler(new ChannelInit())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MyInHandler());
                    }
                })
                .connect(new InetSocketAddress("192.168.162.133", 9090));

        Channel client = connect.sync().channel();

        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        ChannelFuture send = client.writeAndFlush(buf);
        send.sync();

        client.closeFuture().sync();

    }

    /**
     * 官方代码
     */
    @Test
    public void nettyServer() throws InterruptedException {

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap bs = new ServerBootstrap();
        ChannelFuture bind = bs.group(group, group)
                .channel(NioServerSocketChannel.class)
//                .childHandler(new ChannelInit())
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MyInHandler());
                    }
                })
                .bind(new InetSocketAddress("192.168.162.1", 9090));

        bind.sync().channel().closeFuture().sync();

    }


}

class MyAcceptHandler extends ChannelInboundHandlerAdapter {

    private final EventLoopGroup selector;
    private final ChannelHandler handler;

    public MyAcceptHandler(EventLoopGroup thread, ChannelHandler myInHandler) {
        this.selector = thread;
        this.handler = myInHandler;     // ChannelInit
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server registed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel client = (SocketChannel) msg;     // accept

        // 1. 响应式的 handler
        ChannelPipeline p = client.pipeline();
        p.addLast(handler);     // 1. client::pipeline[ChannelInit]

        // 2. 注册
        selector.register(client);

    }
}

@ChannelHandler.Sharable
class ChannelInit extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());   // 2. client::pipeline[ChannelInit, MyInHandler]
        ctx.pipeline().remove(this);
    }
}

/**
 * 读写的客户端，用户自己实现的
 * 用户需要进行属性的操作
 * ChannelHandler.Sharable 不应该强加给coder
 */
// @ChannelHandler.Sharable
class MyInHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client registed....");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client active");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        // read指针会动
        // CharSequence str = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);
        // get指针不会动
        CharSequence str = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
        System.out.println(str);

        ctx.writeAndFlush(buf);
    }
}
