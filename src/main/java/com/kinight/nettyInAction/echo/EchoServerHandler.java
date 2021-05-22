package com.kinight.nettyInAction.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable    // 表示ChannelHandler可以被多个Channel安全地共享
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 对与每个传入的消息都要调用
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Server recived: " + in.toString(CharsetUtil.UTF_8));
        ctx.write(in);  // 将接受到的消息写给发送者，不冲刷出站消息
    }

    /**
     * 通知ChannelInboundHandler最后一次对channelRead()的调用，是当前批量读取中的最后一条消息
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);     // 将未决消息冲刷到远程节点，并且关闭该Channel
    }

    /**
     * 在读取期间，异常抛出时的调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();    // 打印异常
        ctx.close();    // 关闭Channel
    }
}
