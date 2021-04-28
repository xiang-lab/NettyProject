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

        while (buf.readableBytes() >= 100) {
            byte[] bytes = new byte[100];
            buf.getBytes(buf.readerIndex(), bytes);     // （从哪里读取，读多少），读取后readindex不变
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();

            if (buf.readableBytes() >= header.getDataLen() + 100) {
                // 处理指针
                buf.readBytes(100); // 将指正移动到body开始的位置
                byte[] data = new byte[(int) header.getDataLen()];
                buf.readBytes(data);
                ByteArrayInputStream din = new ByteArrayInputStream(data);
                ObjectInputStream doin = new ObjectInputStream(din);

                // Decode 在两个方向上都使用
                // 通信的协议
                if (header.getFlag() == 0x14141414) {
                    // 服务端
                    MyContent content = (MyContent) doin.readObject();
                    out.add(new Packmsg(header, content));
                } else if (header.getFlag() == 0x14141424) {
                    // 客户端返回处理
                    MyContent content = (MyContent) doin.readObject();
                    out.add(new Packmsg(header, content));
                }
            } else {
                break;
            }
        }

    }
}
