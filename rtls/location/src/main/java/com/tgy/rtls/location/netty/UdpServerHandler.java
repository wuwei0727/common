package com.tgy.rtls.location.netty;

import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.model.Header;
import com.tgy.rtls.location.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private Logger logger= LoggerFactory.getLogger(UdpServerHandler.class);
    public   byte[] PACKAGE_TAG = {0x7e, (byte) 0xAA};

    DataProcess  dataprocess;
    public UdpServerHandler(DataProcess  dataprocess) {
        // TODO Auto-generated constructor stub
        this.dataprocess=dataprocess;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // TODO Auto-generated method stub
   /*  String req = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println(msg);
        System.out.println(req);*/


        byte[] tag = new byte[2];//同步头
        ByteBuf buffer = msg.content();
        if (buffer.readableBytes() < 24) {
            throw new CorruptedFrameException("包长度问题");
        }
        buffer.readBytes(tag);
        if (tag[0] != PACKAGE_TAG[0] || tag[1] != PACKAGE_TAG[1]
        ) {
            // buffer.readBytes(buffer.readableBytes());
            throw new CorruptedFrameException("标志错误"+tag[0] +":"+tag[1] );
        }

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
        try {
            buffer.readBytes(data);
            Message message = new Message(head, data);
       //   Executor executor = SpringContextHolder.getBean("threadPool1");;
        //  executor.execute(new MessageReadTask(message,null));
            MessageReadTask.MessageReadTask1(message,null);
        }catch (Exception e){
            logger.error(e.getMessage(), e.getStackTrace());
        }






    }


}

