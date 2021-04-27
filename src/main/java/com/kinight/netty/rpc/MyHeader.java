package com.kinight.netty.rpc;

import java.io.Serializable;

public class MyHeader implements Serializable {

    /**
     * 通信上的协议
     * 1. ooxx值
     * 2. UUID: requestID
     * 3. DATA_LEN
     */

    private int flag;   // 32位可以设置很多的信息
    private long requestID;
    private long dataLen;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        this.requestID = requestID;
    }

    public long getDataLen() {
        return dataLen;
    }

    public void setDataLen(long dataLen) {
        this.dataLen = dataLen;
    }
}
