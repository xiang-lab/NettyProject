package com.kinight.rpc.proxy;

import com.kinight.netty.rpc.ClientFactory;
import com.kinight.netty.rpc.MyContent;
import com.kinight.netty.rpc.MyHeader;
import com.kinight.netty.rpc.ResponseMappingCallback;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class MyProxy {

}
