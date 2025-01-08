package com.tgy.rtls.location.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 组播server
 */
public class ClientMulticastHandler  extends SimpleChannelInboundHandler<DatagramPacket> {
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        //当channel就绪后。
        logger.info("client channel is ready!");
        ctx.writeAndFlush("started");//阻塞直到发送完毕  这一块可以去掉的
        while (true) {
            try {
                logger.info("client active");
                System.out.println("channelActive=========");
                ByteBuf tmsg = Unpooled.buffer(1024);
                String xx = "004301180000";
                // String zh=ConvertUtil.toStringHex(xx);

                for (int i = 0; i < xx.getBytes().length; i++) {
                    tmsg.writeByte(xx.getBytes()[i]);
                }
                ctx.writeAndFlush(tmsg);
            } catch(Exception e){
                e.printStackTrace();
            }
            Thread.sleep(5000);
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}





