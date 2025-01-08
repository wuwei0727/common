package com.tgy.rtls.location.test;

import com.tgy.rtls.location.model.Cmd;
import com.tgy.rtls.location.model.Header;
import com.tgy.rtls.location.model.Message;
import com.tgy.rtls.location.netty.MessageDecoder;
import com.tgy.rtls.location.netty.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多连接客户端
 */
public class MutilClient {

    /**服务类*/
    private Bootstrap bootstrap = new Bootstrap();

    /**会话集合*/
    //private List<Channel>  channels = new ArrayList<Channel>();

    private ConcurrentHashMap<Integer, Channel> channels=new ConcurrentHashMap<>();

   // private String ip="192.168.1.80";

    private int port=39777;
    public static volatile boolean FLAG=true;

    /**引用计数*/
    private final AtomicInteger index = new AtomicInteger();

    /**初始化*/
    public void    init(String ip,int start ,int end){
        //worker
        EventLoopGroup worker = new NioEventLoopGroup();

        //设置工作线程
        this.bootstrap.group(worker);

        //初始化channel
        bootstrap.channel(NioSocketChannel.class);

        //设置handler管道
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                //  channel.pipeline().addLast(new StringDecoder());
                // channel.pipeline().addLast(new StringEncoder());
                channel.pipeline().addLast(new MessageDecoder());
                channel.pipeline().addLast(new MessageEncoder());
                channel.pipeline().addLast(new ClientHandler());
            }
        });

        //根据连接数建立连接

        for(int i=start;i <= end;i++){
            ChannelFuture channelFuture = bootstrap.connect(ip,port);
            channels.put(i,channelFuture.channel());
        }




    }

    /**重连*/
    private void reconect(String ip,int deviceid,Channel channel) {
        //此处可改为原子操作
        // synchronized(channel){
        if(!channel.isActive()) {
            Channel newChannel = bootstrap.connect(ip, port).channel();
            channels.replace(deviceid, newChannel);
            //}
        }
    }



    public static void main(String[] args) {
        mutilClient("192.168.1.189",1,2);




    }

    public static void mutilClient(String ip,Integer start,Integer end){


        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                MutilClient client = new MutilClient();
                client.init(ip,start,end);

                    int idd=0;
                    while(MutilClient.FLAG) {
                        idd++;
                        for(java.util.Map.Entry<Integer, Channel> entry:client.channels.entrySet())
                        {
                            int deviceid=(int)entry.getKey();
                            Channel ch=(Channel) entry.getValue();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if(!MutilClient.FLAG)
                                 break;
                            if(ch.isActive()) {
                                int lngi=113;
                                int lati=23;
                             //  System.out.println("device id :"+deviceid);
                                Random random=new Random();
                                double rand=random.nextDouble();
                                TestData.addHeartDate(deviceid,4.1f,ch);
                            }else {
                                System.out.println(System.currentTimeMillis()+"断开重新连接");
                                client.reconect(ip,deviceid, ch);
                            }


                        }
                        //   System.out.println(new Timestamp(new Date().getTime()).toString()+"total connect:"+client.channels.size());

                        try {
                            if(MutilClient.FLAG)
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

            }
        }).start();

    }





    }


class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
     //   System.out.println("client receive msg:");
        Message message = (Message) msg;
     /*   ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);*/
      //  ctx.writeAndFlush(msg);

        //  System.out.println("client receive msg:");
      //  new MessageReadTask(message,ctx.channel());
        if (message == null)
            return;
        Header header = message.getHeader();
        if (header == null)
            return;
        long bsid = message.getBsId();
        byte[] cmd=header.getCmd();
        String cmd_type= new Cmd(cmd).getCmd();
        byte[] data = message.getData();
        if (data == null)
            return;
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
       // System.out.println("CMD"+cmd_type);
        switch (cmd_type){
            case "00002027":
                TestData.setRandomKey(1l,ctx,data);
                break;
        }
    }



}


