package com.kinight.netty.rpc;

import java.io.Serializable;

public class Packmsg implements Serializable {

    private MyHeader header;
    private MyContent content;

    public Packmsg(MyHeader header, MyContent content) {
        this.header = header;
        this.content = content;
    }

    public MyHeader getHeader() {
        return header;
    }

    public void setHeader(MyHeader header) {
        this.header = header;
    }

    public MyContent getContent() {
        return content;
    }

    public void setContent(MyContent content) {
        this.content = content;
    }
}
