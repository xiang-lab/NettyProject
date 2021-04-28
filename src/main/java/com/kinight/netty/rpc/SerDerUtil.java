package com.kinight.netty.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 序列化工具类
 */
public class SerDerUtil {

    private static ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * 序列化
     */
    public synchronized static byte[] serialize(Object msg) {
        out.reset();
        ObjectOutputStream oout = null;
        byte[] msgBody = null;

        try {
            oout = new ObjectOutputStream(out);
            oout.writeObject(msg);
            msgBody = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msgBody;
    }

}
