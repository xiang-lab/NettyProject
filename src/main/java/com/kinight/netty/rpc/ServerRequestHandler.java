package com.kinight.netty.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    /**
     * provider
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packmsg buf = (Packmsg) msg;

        System.out.println("ServerRequestHandler.channelRead server handler" + buf.getContent().getMethod());

    }


}
