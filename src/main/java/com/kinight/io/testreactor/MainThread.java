package com.kinight.io.testreactor;

public class MainThread {

    public static void main(String[] args) {
        // 这里不做相关IO 和 业务的事情

        // 1. 创建 IO Thread(一个或者多个)
        SelectorThreadGroup stg = new SelectorThreadGroup(3);
        // 混杂模式，只有一个线程负责accept，每个都会被分配client，进行R/W
        // SelectorThreadGroup stg = new SelectorThreadGroup(3);


        // 2. 应该把监听的server注册到某一个selector上
        stg.bind(9999);

    }
}
