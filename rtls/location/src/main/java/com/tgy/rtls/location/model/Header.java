package com.tgy.rtls.location.model;


import lombok.Data;

@Data
public class Header {



    private byte[] tag;// 同步头(2 bytes)
    private byte[] length;// 数据段长度(2 bytes)
    private byte[] reserved;// (0xffffffff4 bytes)
    private byte[] head;// 0x1717171717(4 bytes)
    private byte[] src;// bsid(4 bytes)
    private byte[] dst;// 帧dst(4 bytes)
    private byte[] cmd;// 帧cmd(4 bytes)



    public Header(byte[] tag, byte[] length, byte[] reserved, byte[] head, byte[] src, byte[] dst,byte[] cmd) {
        this.tag = tag;
        this.length = length;
        this.reserved = reserved;
        this.head = head;
        this.src = src;
        this.dst = dst;
        this.cmd=cmd;
    }



}
