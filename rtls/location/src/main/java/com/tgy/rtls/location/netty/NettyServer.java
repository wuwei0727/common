package com.tgy.rtls.location.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.util.concurrent.TimeUnit;
@Component
public class NettyServer {
    @Autowired
    DataProcess dataProcess;
public  NettyServer(){
   startUdpServer(38778);
    new Thread(new Runnable() {
    @Override
    public void run() {
        try {
            startTcpServer(38777);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}).start();

}


    public void startTcpServer(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
           /* Message msg = new Message();
            msg.frameid=1;
            msg.pid=1;
            msg.uid=1;*/

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                           // ch.pipeline().addLast(new SelfDecode(ByteOrder.LITTLE_ENDIAN,1024*1024, 0, 4, 0, 4,false));
                            // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段
                            // ch.pipeline().addLast(new LengthFieldPrepender(4));
                            // 对经过粘包和拆包处理之后的数据进行json反序列化，从而得到User对象
                           // ch.pipeline().addLast(new JsonDecoder());
                            // 对响应数据进行编码，主要是将User对象序列化为json
                            //ch.pipeline().addLast(new JsonEncoder());
                            // 处理客户端的请求的数据，并且进行响应
                           // ch.pipeline().addLast(new EchoServerHandler());
                            ch.pipeline().addLast("idleStateCheck", new IdleStateHandler(0, 0, 5, TimeUnit.MINUTES));
                            ch.pipeline().addLast("decoder", new MessageDecoder());
                            ch.pipeline().addLast("encoder", new MessageEncoder());
                            ch.pipeline().addLast("handler", new TcpServerHandler());

                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    private void startUdpServer(int udpport) {

        Thread udpthread=new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap();//udp不能使用ServerBootstrap
                    b.group(workerGroup).channel(NioDatagramChannel.class)//设置UDP通道
                            .handler(new UdpServerHandler(dataProcess))//初始化处理器
                            .option(ChannelOption.SO_BROADCAST, true)// 支持广播
                            .option(ChannelOption.SO_RCVBUF, 1024 *2)// 设置UDP读缓冲区为1M
                            .option(ChannelOption.SO_SNDBUF, 1024 *2)// 设置UDP写缓冲区为1M
                            .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));

                    System.out.println("UDP start");

                    // 绑定端口，开始接收进来的连接
                    ChannelFuture f = b.bind(udpport).sync();

                    Channel channel = f.channel();

                    // 等待服务器 socket 关闭 。
                    // 这不会发生，可以优雅地关闭服务器。
                    f.channel().closeFuture().await();

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    workerGroup.shutdownGracefully();

                    System.out.println("UDP stop");
                }

            }
        });
        udpthread.start();

    }

    public static void startUdpClient(String ip,Integer tagStart,Integer tagEnd, Integer bsStart, Integer bsEnd){
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        NetworkInterface ni = NetUtil.LOOPBACK_IF;
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
               .option(ChannelOption.SO_BROADCAST, true)//广播配置
            //    .option(ChannelOption.IP_MULTICAST_IF,ni) 组播配置
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch)throws Exception {
                       // ChannelPipeline pipeline = ch.pipeline();

                     /*   ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
                        ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());*/
                     //   ch.pipeline().addLast(new MessageEncoder());
                        ch.pipeline().addLast(new UdpClientHandler( tagStart, tagEnd,  bsStart,  bsEnd) );
                    }
                });
        try {
            bootstrap.connect(ip,39778).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ;
    }

   static void  fn(int n){
        if(n > 0)
            fn(n-1);
        System.out.println(n-1);
    }

    public static void main(String[] args) {
          //   fn(5);
    UdpClientHandler.FLAG=true;
         //  for(int i=0;i<10;i++) {
               Thread th=new Thread(new Runnable() {
                   @Override
                   public void run() {
                       startUdpClient("192.168.1.189", 1, 2, 9, 10);
                   }
               });
               th.start();

          // }
    }
}

