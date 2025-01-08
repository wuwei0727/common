package com.tgy.rtls.location.test;

import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.model.Header;
import com.tgy.rtls.location.model.Message;
import com.tgy.rtls.location.netty.MessageDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.tgy.rtls.location.netty.MessageDecoder.*;

public class TestData {
public static volatile  int rangeid=0;
    public static   int rangeid1=0;
    public static void addHeartDate(int bsid,float volt, Channel ch) {
        int reserved = 16;
        ByteBuffer byteBuffer = ByteBuffer.allocate(13);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(0);
        byteBuffer.put((byte) 0x01);
        byteBuffer.putFloat(volt);
        byteBuffer.putFloat(volt);


        byte[] data = byteBuffer.array();
        byte[] CMD = {0x12, 0x20, 0x00, 0x00};
        Header head = new Header(PACKAGE_TAG, ByteUtils.shortToByte((short) (data.length + reserved)), RESERVED, HEADER, ByteUtils.intToBytes(bsid), SRC, CMD);

        Message message = new Message(head, data);
        System.out.println("heartdata");
        System.out.print(ByteUtils.printHexString(head.getTag()));
        System.out.print(ByteUtils.printHexString(head.getLength()));
        System.out.print(ByteUtils.printHexString(head.getReserved()));
        System.out.print(ByteUtils.printHexString(head.getHead()));
        System.out.print(ByteUtils.printHexString(head.getSrc()));
        System.out.print(ByteUtils.printHexString(head.getCmd()));
        System.out.print(ByteUtils.printHexString(head.getDst()));
        System.out.print(ByteUtils.printHexString(head.getCmd()));
        System.out.print(ByteUtils.printHexString(data));
        System.out.println("heartdata");
        ch.writeAndFlush(message);
    }

    public static void addRangeDate(int bsid,int tagid,int rangeBsid, ChannelHandlerContext ch,int rangeid1) {
        int reserved = 16;
        ByteBuffer byteBuffer = ByteBuffer.allocate(111);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(tagid);
      //  byteBuffer.putInt(rangeid++);  //rangeid
        byteBuffer.putInt(rangeid1++);  //rangeid
        byteBuffer.putLong(577869615308l);// rx3
        byteBuffer.putInt(rangeBsid);// bs0
        byteBuffer.put((byte)0x00);//0: 主模块	1：从模块
        byteBuffer.putLong(483387392);//ft1
        byteBuffer.putLong(496462378982l);//ft2
        byteBuffer.putLong(496591887872l);//ft3
        byteBuffer.putLong(612963507);//ft4
        byteBuffer.putInt(rangeBsid);//bs1
        byteBuffer.put((byte)0x01);//0: 主模块	1：从模块
        byteBuffer.putLong(483387392);//ft1
        byteBuffer.putLong(496462377982l);//ft2
        byteBuffer.putLong(496591887872l);//ft3
        byteBuffer.putLong(612963507);//ft4


        byteBuffer.putFloat(-90);//rssifp
        byteBuffer.putFloat(-90);// rssi
        byteBuffer.putFloat(0.9f);// cl




        /**
         * 时间
         */
        Calendar nowtime = new GregorianCalendar();
        byteBuffer.putShort((short) nowtime.get(Calendar.YEAR));
        byteBuffer.put((byte) nowtime.get(Calendar.MONTH));
        byteBuffer.put((byte) nowtime.get(Calendar.DATE));
        byteBuffer.put((byte) nowtime.get(Calendar.HOUR));
        byteBuffer.put((byte) nowtime.get(Calendar.MINUTE));
        byteBuffer.put((byte) nowtime.get(Calendar.SECOND));
        byteBuffer.putShort((short) nowtime.get(Calendar.MILLISECOND));



        byte[] data = byteBuffer.array();
        byte[] CMD = {0x22, 0x20, 0x00, 0x00};
        Header head = new Header(PACKAGE_TAG, ByteUtils.shortToByte((short) (data.length + reserved)), RESERVED, HEADER, ByteUtils.intToBytes(bsid), SRC, CMD);

        Message message = new Message(head, data);
        writeMessage(message,ch);
       // System.out.println("rangeid"+rangeid);
    }

    public  static  void setRandomKey(Long bsid ,ChannelHandlerContext ch, byte[] data){
        int reserved = 16;
        byte[] CMD = {0x28, 0x20, 0x00, 0x00};
        Header head = new Header(PACKAGE_TAG, ByteUtils.shortToByte((short) (data.length + reserved)), RESERVED, HEADER, ByteUtils.intToBytes(bsid), SRC, CMD);
        Message message = new Message(head, data);
        ch.writeAndFlush(message);
    }

    public  static void writeMessage(Message msg,ChannelHandlerContext out){
        Header header = msg.getHeader();
        ByteBuf tmsg = Unpooled.buffer(10);
        tmsg.writeBytes(MessageDecoder.PACKAGE_TAG);
        tmsg.writeBytes((header.getLength()));
        tmsg.writeBytes((header.getReserved()));
        tmsg.writeBytes((header.getHead()));
        tmsg.writeBytes((header.getSrc()));
        tmsg.writeBytes((header.getDst()));
        tmsg.writeBytes((header.getCmd()));
        tmsg.writeBytes((msg.getData()));
        out.writeAndFlush(tmsg);
    }
}
