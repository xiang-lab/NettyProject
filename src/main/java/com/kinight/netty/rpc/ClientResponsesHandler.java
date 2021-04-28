package com.kinight.netty.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ClientResponsesHandler extends ChannelInboundHandlerAdapter {

    // consumer...
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("ClientResponses channelRead");

        ByteBuf buf = (ByteBuf) msg;
        if (buf.readableBytes() >= 100) {
            byte[] bytes = new byte[100];
            buf.readBytes(bytes);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();
            System.out.println("client response @ id: " + header.getRequestID());
            // TODO 处理requestID
            ResponseHandler.runCallback(header.getRequestID());

//            if (buf.readableBytes() >= header.getDataLen()) {
//                byte[] data = new byte[(int) header.getDataLen()];
//                buf.readBytes(data);
//                ByteArrayInputStream din = new ByteArrayInputStream(data);
//                ObjectInputStream doin = new ObjectInputStream(din);
//                MyContent content = (MyContent) doin.readObject();
//                System.out.println(content.getName());
//            }
        }
    }
}
