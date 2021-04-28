package com.kinight.netty.rpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientResponsesHandler extends ChannelInboundHandlerAdapter {

    // consumer...
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("ClientResponses channelRead");

        Packmsg responsePkg = (Packmsg) msg;

        // 曾经没有考虑返回的事
        ResponseMappingCallback.runCallback(responsePkg);

    }
}
