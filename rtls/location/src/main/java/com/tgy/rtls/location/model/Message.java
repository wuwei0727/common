package com.tgy.rtls.location.model;



import com.tgy.rtls.location.Utils.ByteUtils;

import java.io.ByteArrayOutputStream;

public class Message {

    private Header header;
    //    private String data;
    private byte[] data;




    public Message(Header header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public long getBsId() {
        long bsid = ByteUtils.bytes2long(header.getSrc()) ;

        return bsid;
    }


    public Header getHeader() {
        return header;
    }

    public byte[] getData() {
        return data;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] toByte() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        return out.toByteArray();
    }



}