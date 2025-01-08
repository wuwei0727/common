package com.tgy.rtls.location.netty;


import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.model.Header;
import com.tgy.rtls.location.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    private Logger logger= LoggerFactory.getLogger(MessageDecoder.class);

    /** 包长度志头 **/
    public static final int HEAD_LENGHT = 24;
    /**标志头**/
    public static final byte[] PACKAGE_TAG = {0x7e, (byte) 0xAA};

    /**标志头**/
    public static final byte[] SRC = {0x00, 0x00,0x00,(byte) 0x80};
    public static final byte[] RESERVED = {(byte)0xff, (byte)0xff,(byte)0xff,(byte) 0xff};
    public static final byte[] HEADER = {(byte)0x17, (byte)0x17,(byte)0x17,(byte) 0x17};


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {

   // System.out.println("TCP read length"+buffer.readableBytes());
        int totallen= buffer.readableBytes();


    /*   byte[] sds=new byte[totallen];
        buffer.readBytes(sds);
        ByteUtils.printHexString(sds);
        System.out.println(ByteUtils.printHexString(sds));*/


       if (buffer.readableBytes() < HEAD_LENGHT) {
            throw new CorruptedFrameException("包长度问题");
        }
        byte[] tag = new byte[2];//同步头
        for(int i=0;i< totallen;i++) {

            byte head0=buffer.readByte();
            byte head1=buffer.readByte();
            int readerIndex = buffer.readerIndex();
            if (head0== PACKAGE_TAG[0] && head1 == PACKAGE_TAG[1]) {
                buffer.readerIndex(readerIndex-2);
                buffer.markReaderIndex();
                buffer.readBytes(tag);
             //   System.out.println("find head");
                break;
              //  throw new CorruptedFrameException("标志错误" + tag[0] + ":" + tag[1]);
            }else{
                buffer.readerIndex(readerIndex-1);
            }
        }
        try {
        byte[] len = new byte[2];//数据段长度
        buffer.readBytes(len);
    //    logger.error( ByteUtils.printHexString(len));
        byte[] reserved = new byte[4];//reserved
        buffer.readBytes(reserved);
     //   logger.error( ByteUtils.printHexString(reserved));
        byte[] header = new byte[4];//header
        buffer.readBytes(header);
    //    logger.error( ByteUtils.printHexString(header));
        byte[] src = new byte[4];//src
        buffer.readBytes(src);
       // logger.error( ByteUtils.printHexString(src));
        byte[] dst = new byte[4];//dest
        buffer.readBytes(dst);


        byte[] cmd = new byte[4];//cmd
        buffer.readBytes(cmd);
    //   logger.error( ByteUtils.printHexString(cmd));




        Header head = new Header(tag, len, reserved, header, src, dst,cmd);


        byte[] data=new byte[(int)(ByteUtils.byte2short(len)-16)];
        buffer.readBytes(data);
        Message message = new Message(head, data);
        out.add(message);
        }catch (Exception e){
            buffer.resetReaderIndex();
        }
    }

}
