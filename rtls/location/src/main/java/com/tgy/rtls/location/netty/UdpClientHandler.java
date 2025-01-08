package com.tgy.rtls.location.netty;

import com.tgy.rtls.location.test.TestData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

/**
 * udp发送端，支持单波，广播和组播。根据输入ip以及配置不同
 */
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    public static volatile boolean FLAG=false;
    Integer tagStart;
    Integer tagEnd;
    Integer bsStart;
    Integer bsEnd;
//	private static Channel channel = LogPushUdpClient.getInstance().getChannel();


    UdpClientHandler(  Integer tagStart,Integer tagEnd, Integer bsStart, Integer bsEnd){
        this.tagStart=tagStart;
        this.tagEnd=tagEnd;
        this.bsStart=bsStart;
        this.bsEnd=bsEnd;

    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //当channel就绪后。
        logger.info("client channel is ready!");
   new Thread(new Runnable() {
       @Override
       public void run() {
          while (FLAG)
          // int sd=0;
       //  while (true)
           {
               int k=0;

               for(int i=tagStart;i<=tagEnd;i++){
                   if(bsEnd!=null) {
                       TestData.addRangeDate(bsStart+k, i, bsStart+k, ctx,0);
                     //  System.out.println(this+":"+sd);
                       k++;
                   }else{
                       TestData.addRangeDate(bsStart, i, bsStart, ctx,0);
                       //System.out.println(Thread.currentThread().getId()+":"+sd);
                   }
                  try {
                       Thread.sleep(5000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }

       /*       if(FLAG) {
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }*/
           }
       }
   }).start();

      //  ctx.writeAndFlush("started");//阻塞直到发送完毕  这一块可以去掉的
   /*  while (true) {
            try {
                logger.info("client active");
                System.out.println("channelActive=========");
                ByteBuf tmsg = Unpooled.buffer(10);
                String xx = "004301180000";
                // String zh=ConvertUtil.toStringHex(xx);

                    for (int i = 0; i < xx.getBytes().length; i++) {
                        tmsg.writeByte(xx.getBytes()[i]);
                    }
                    ctx.writeAndFlush("dsadd");
                } catch(Exception e){
                    e.printStackTrace();
                }
            Thread.sleep(5000);
            }*/

//       NettyUdpClientHandler.sendMessage("你好UdpServer", new InetSocketAddress("127.0.0.1",8888));
//       sendMessageWithInetAddressList(message);
//       logger.info("client send message is: 你好UdpServer");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception {
        // TODO 不确定服务端是否有response 所以暂时先不用处理
        final ByteBuf buf = packet.content();
        int readableBytes = buf.readableBytes();
        byte[] content = new byte[readableBytes];
        buf.readBytes(content);
        String serverMessage = new String(content);
        logger.info("reserveServerResponse is: "+serverMessage);
    }



    /**
     * 向服务器发送消息
     * @param msg 按规则拼接的消息串
     * @param inetSocketAddress 目标服务器地址
     */
    public static void sendMessage(final String msg,final InetSocketAddress inetSocketAddress){
        if(msg == null){
            throw new NullPointerException("msg is null");
        }
        // TODO 这一块的msg需要做处理 字符集转换和Bytebuf缓冲区
        senderInternal(datagramPacket(msg, inetSocketAddress));
    }

    /**
     * 发送数据包并监听结果
     * @param datagramPacket
     */
    public static void senderInternal(final DatagramPacket datagramPacket, List<Channel> channelList) {

    }

    /**
     * 组装数据包
     * @param msg 消息串
     * @param inetSocketAddress 服务器地址
     * @return DatagramPacket
     */
    private static DatagramPacket datagramPacket(String msg, InetSocketAddress inetSocketAddress){
        ByteBuf dataBuf = Unpooled.copiedBuffer(msg, Charset.forName("UTF-8"));
        DatagramPacket datagramPacket = new DatagramPacket(dataBuf, inetSocketAddress);
        return datagramPacket;
    }

    /**
     * 发送数据包服务器无返回结果
     * @param datagramPacket
     */
    private static void senderInternal(final DatagramPacket datagramPacket) {


    }



}

