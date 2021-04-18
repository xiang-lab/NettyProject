package com.kinight.old.nio;

import java.nio.IntBuffer;

public class BasicBuffer {

    // 简单说明
    public static void main(String[] args) {
        // 1.创建一个Buffer, 大小为5, 即可以存放5个int
        IntBuffer intBuffer = IntBuffer.allocate(5);

        // 2.向buffer存放数据
        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i*2);
        }

        // 3.如何从buffer读取数据
        // 4.将buffer转换, 读写切换
        intBuffer.flip();

        while (intBuffer.hasRemaining()) {
            System.out.println(intBuffer.get());
        }
    }

}
