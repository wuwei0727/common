package com.tgy.rtls.location.check;

import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfigImp;
import com.tgy.rtls.location.model.BsCheck;
import com.tgy.rtls.location.netty.MapContainer;

import java.util.Random;

public class Task extends Thread {

 Long bsid;
 Long interval;
 Long count;
    private MapContainer mapContainer = SpringContextHolder.getBean("mapContainer");
    private BsParaConfig bsParaConfig = SpringContextHolder.getBean(BsParaConfigImp.class);

   public Task(Long bsid,Long interval,Long count){
        this.bsid=bsid;
        this.interval=interval;
        this.count=count;

    }
    @Override
    public void run() {
        BsCheck bs = mapContainer.bsCheck.get(bsid);
        bs.random.clear();
        Random random=new Random();
        while (bs.flag){

            /**
             * 生成 [m,n] 的数字
             * int i1 = random.nextInt() * (n-m+1)+m;
             * */
            for(int i=0;i<count;i++) {
                long i1 = random.nextInt(90000000)  + 10000000;
                while (bs.random.containsKey((i1))) {
                    i1 = random.nextInt(90000000)  + 10000000;
                }
                bs.random.put(Long.valueOf(i1), false);
                //bsParaConfig.setRandomKey(bsid, (int)i1,0);
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!bs.flag){
                    break;
                }
            }
            System.out.println("发送结束random");
            bs.flag=false;

        }

    }

    public static void main(String[] args) {
    byte[] ss=float2byte(3.2f);
    String gg=ByteUtils.printHexString(ss);
        System.out.println(gg);
    }
    public static byte[] float2byte(float f) {

        // 鎶奻loat杞崲涓篵yte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 缈昏浆鏁扮粍
        int len = b.length;
        // 寤虹珛涓�涓笌婧愭暟缁勫厓绱犵被鍨嬬浉鍚岀殑鏁扮粍
        byte[] dest = new byte[len];
        // 涓轰簡闃叉淇敼婧愭暟缁勶紝灏嗘簮鏁扮粍鎷疯礉涓�浠藉壇鏈�
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 灏嗛『浣嶇i涓笌鍊掓暟绗琲涓氦鎹�
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;

    }
}
