package com.kinight.netty.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

// 解码器
public class ServerDecode extends ByteToMessageDecoder {

    // 父类里一定有channelRead { 前面的拼buf; decode(); 剩余留存; 对out遍历} -> bytebuf
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        System.out.println("ServerDecode.decode channel start: " + buf.readableBytes());

        while (buf.readableBytes() >= 100) {
            byte[] bytes = new byte[100];
            buf.getBytes(buf.readerIndex(), bytes);     // （从哪里读取，读多少），读取后readindex不变
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();
            System.out.println("server response @ dataLen: " + header.getDataLen());
            System.out.println("server response @ id: " + header.getRequestID());

            if (buf.readableBytes() >= header.getDataLen()) {
                // 处理指针
                buf.readBytes(100); // 将指正移动到body开始的位置
                byte[] data = new byte[(int) header.getDataLen()];
                buf.readBytes(data);
                ByteArrayInputStream din = new ByteArrayInputStream(data);
                ObjectInputStream doin = new ObjectInputStream(din);
                MyContent content = (MyContent) doin.readObject();
                System.out.println(content.getName());

                out.add(new Packmsg(header, content));
            } else {
                break;
            }
        }

    }
}
