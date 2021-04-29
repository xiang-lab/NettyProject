package com.kinight.netty.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    private Dispatcher dis;

    public ServerRequestHandler(Dispatcher dis) {
        this.dis = dis;
    }

    /**
     * provider
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packmsg requestPkg = (Packmsg) msg;

//        System.out.println("ServersRequestHandler.channelRead server handler" + requestPkg.getContent().getArgs()[0]);

        // 如果假设处理完了，要个客户端返回
        // 需要注意哪些环节

        // byteBuf
        // 因为是个RPC，需要有requestID
        // 在client一侧也要解决解码问题

        // 关注rpc通信协议 来的时候flag 0x14141414

        // 有新的header+content
        String ioThreadName = Thread.currentThread().getName();
        // 方法1. 直接在当前方法处理IO和业务和返回的事
        // 方法2. 使用netty自己的eventLoop来处理业务及返回
        //        ctx.executor().execute(new Runnable() {
        // 方法3. 自己创建线程池
        ctx.executor().parent().next().execute(new Runnable() {
            @Override
            public void run() {

                String serviceName = requestPkg.getContent().getName();
                String method = requestPkg.getContent().getMethodName();
                Object c = dis.get(serviceName);
                Class<?> clazz = c.getClass();
                Object res = null;
                try {
                    Method m = clazz.getMethod(method, requestPkg.getContent().getParameterTypes());
                    res = m.invoke(c, requestPkg.getContent().getArgs());
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


//                String execThreadName = Thread.currentThread().getName();

                MyContent content = new MyContent();
//                String s = "io thread: " + ioThreadName +
//                        " exec thread: " + execThreadName +
//                        " from args: " + requestPkg.getContent().getArgs()[0];
//                System.out.println(s);
                content.setRes((String) res);
                byte[] contentByte = SerDerUtil.serialize(content);

                MyHeader responseHeader = new MyHeader();
                responseHeader.setRequestID(requestPkg.getHeader().getRequestID());
                responseHeader.setFlag(0x14141424);
                responseHeader.setDataLen(contentByte.length);
                byte[] responseHeaderByte = SerDerUtil.serialize(responseHeader);

                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(responseHeaderByte.length + contentByte.length);
                byteBuf.writeBytes(responseHeaderByte);
                byteBuf.writeBytes(contentByte);

                ctx.writeAndFlush(byteBuf);
            }
        });



    }


}
