package com.kinight.io;

import java.io.*;
import java.net.Socket;

public class SocketClient {

    public static void main(String[] args) {

        try {
            Socket client = new Socket("192.168.162.133",9090);

            client.setSendBufferSize(20);       // 发送缓冲区
            client.setTcpNoDelay(false);        // false 默认开启优化，尽量将数据拼接成一个数据包进行发送
                                                // true 不开启优化，有数据就发送
            client.setOOBInline(false);         // 未知功能
            OutputStream out = client.getOutputStream();

            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while(true){
                String line = reader.readLine();
                if(line != null ){
                    byte[] bb = line.getBytes();
                    for (byte b : bb) {
                        out.write(b);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
