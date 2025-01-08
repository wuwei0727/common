package com.tgy.rtls.location.check;

import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class BsUpdateCheck implements Delayed {

    public String bsid;
    public int  fileType;
    private long excuteTime;// 延迟时长，这个是必须的属性因为要按照这个判断延时时长。

    public BsUpdateCheck(String bsid, int type, long excuteTime) {
        this.bsid = bsid;
        this.fileType=type;
        this.excuteTime = excuteTime;
    }





    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(excuteTime, TimeUnit.NANOSECONDS) - unit.convert(System.currentTimeMillis(),TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        BsUpdateCheck msg = (BsUpdateCheck) o;
        return (this.excuteTime) > (msg.excuteTime) ? 1
                : (this.excuteTime) < (msg.excuteTime) ? -1 : 0;
    }

    public static void main(String[] args) {
        BsUpdateCheck oi = new BsUpdateCheck("1", 1,new Date().getTime() + 3000); // 加3000，表示延时3秒
        BsUpdateCheck oi2 = new BsUpdateCheck("2" ,1,new Date().getTime() + 3000);
        BsUpdateCheck oi3 = new BsUpdateCheck("3" ,1,new Date().getTime() + 3000);
        BsUpdateCheck oi4 = new BsUpdateCheck("4" ,1,new Date().getTime() + 3000);
        BsUpdateCheck oi5 = new BsUpdateCheck("5" ,1,new Date().getTime() + 3000);
        System.out.println(new Date().toString() + "启动，并加入了两条消息"); // 模拟接收到了两个订单
         DelayQueue<BsUpdateCheck> messageQueue = new DelayQueue<BsUpdateCheck>(); //消息队列
        messageQueue.offer(oi); // 当接收到订单后，往队列里放一条消息
        messageQueue.offer(oi2);
        messageQueue.offer(oi3);
        messageQueue.offer(oi4);
        messageQueue.offer(oi5);

        while (messageQueue.size()!=0) {
            try {
                BsUpdateCheck message = messageQueue.take(); // take方法，有消息（延时时间<0的消息）取消息，没有，阻塞住。
                // 这里查一遍订单.. 符合需求发消息，不符合不发
                System.out.println(new Date().toString() + " ..... 发送消息id " + message.bsid);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("延时队列结束");


    }

}
