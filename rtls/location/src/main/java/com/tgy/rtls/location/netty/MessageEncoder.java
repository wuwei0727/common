package com.tgy.rtls.location.netty;

import com.tgy.rtls.location.model.Header;
import com.tgy.rtls.location.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        Header header = msg.getHeader();
       byte[] head=header.getTag();
        //System.out.println("MessageEncoder"+head[0]);
        if(head[0]==(byte)0x7e&&head[1]==(byte)0xaa){
            //System.out.println("bsidasdasd");
            out.writeBytes(MessageDecoder.PACKAGE_TAG);
            out.writeBytes(header.getLength());
            out.writeBytes(header.getReserved());
            out.writeBytes(header.getHead());
            out.writeBytes(header.getSrc());
            out.writeBytes(header.getDst());
            out.writeBytes(header.getCmd());
            out.writeBytes(msg.getData());
        }else  if(head[0]==(byte)0xc9&&head[1]==(byte)0xc9){
            logger.info("anjubao");;
            out.writeBytes(msg.getData());
        }else{
           // System.out.println("head[0]"+head[0]);
        }

    }

}
