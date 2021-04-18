package com.kinight.io;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class OSFileIO {

    static byte[] data = "123456789".getBytes();
    static String path = "/home/kinight/workspace/nettyProject/out.txt";


    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "0":
                testBasicFileIO();
                break;
            case "1":
                testBufferFileIO();
                break;
            case "2":
                testRandomAccessFileWrite();
                break;
            case "3":
            default:
        }
    }

    /**
     * 基本的file写
     * @throws Exception
     */
    public static void testBasicFileIO() throws Exception {
        File file = new File(path);
        FileOutputStream out = new FileOutputStream(file);
        while (true) {
            Thread.sleep(10);
            out.write(data);
        }
    }

    /**
     * 测试buffer文件io
     * @throws Exception
     * jvm将保存8KB后才开始调用syscall write(8KB byte[])
     */
    public static void testBufferFileIO() throws Exception {
        File file = new File(path);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        while (true) {
            Thread.sleep(10);
            out.write(data);
        }
    }

    /**
     * 测试文件NIO
     * @throws Exception
     */
    public static void testRandomAccessFileWrite() throws Exception {

        RandomAccessFile raf = new RandomAccessFile(path, "rw");

        // 普通写入-------------------------------------------
        raf.write("hello mashibing\n".getBytes());
        raf.write("hello seanzhou\n".getBytes());
        System.out.println("write---------------");
        System.in.read();

        // 随机写入-------------------------------------------
        raf.seek(4);
        raf.write("ooxx".getBytes());

        System.out.println("seek----------------");
        System.in.read();

        // 堆外映射写入-------------------------------------------
        FileChannel rafchannel = raf.getChannel();
        // 只有文件的通道才有map
        // mmap 得到一个堆外和且和文件映射的
        MappedByteBuffer map = rafchannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);

        // 曾经需要out.write()这样额系统调用，才能让程序的data进入内核的pagecache
        // 曾经必须有用户态内核态的切换
        // mmap的内存映射，依然是内核的pagecache体系所约束的
        // 换言之，丢数据
        // 可以去github上找一些C的jni扩展程序，使用linux内核的Direct IO
        // Direct IO是忽略linux的pagecache
        // 是把pagecache交给了程序自己开辟一个字节数组当做pagecache，动用代码逻辑来维护一致性/dirty

        // 不是系统调用，但是数据会到达内核的pagecache
        map.put("@@@".getBytes());
        System.out.println("map--put------");
        System.in.read();


        // 读取-------------------------------------------
        // seek到文件开头
        raf.seek(0);

        ByteBuffer buffer = ByteBuffer.allocate(8192);
//        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);

        int read = rafchannel.read(buffer); // 将rafchannel内容读取，写入到buffer中
        System.out.println(buffer);
        buffer.flip();  // buffer翻转之后才能读取
        System.out.println(buffer);

        for (int i = 0; i < buffer.limit(); i++) {
            Thread.sleep(200);
            System.out.println((char) buffer.get(i));
        }

    }

    /**
     * Buffer基本介绍
     */
    @Test
    public void whatByteBuffer(){
        // 分配堆外内存
//        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        // 分配内存
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        System.out.println("postition: " + buffer.position());
        System.out.println("limit: " +  buffer.limit());
        System.out.println("capacity: " + buffer.capacity());
        System.out.println("mark: " + buffer);

        // 写入操作
        buffer.put("123".getBytes());

        System.out.println("-------------put:123......");
        System.out.println("mark: " + buffer);

        //读写交替
        buffer.flip();

        System.out.println("-------------flip......");
        System.out.println("mark: " + buffer);

        // 获取
        buffer.get();

        System.out.println("-------------get......");
        System.out.println("mark: " + buffer);

        buffer.compact();

        System.out.println("-------------compact......");
        System.out.println("mark: " + buffer);

        buffer.clear();

        System.out.println("-------------clear......");
        System.out.println("mark: " + buffer);

    }


}
