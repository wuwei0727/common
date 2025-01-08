package com.tgy.rtls.data.algorithm;

public class BleRssiDis {

    /**
     *
     * @param rssi 信号强度值
     * @param rxpower  1米处信号强度
     * @return
     */
    public  static volatile float index=2.2f;
    public static   double  calcDistByRSSI(float rssi,float rxpower){
/*        //abs是求绝对值的函数
        float iRssi = Math.abs(rssi);
        float rxpower1=Math.abs(rxpower);
      *//*  int iRssi;
        if(rssi>=0){
            iRssi=rssi;
        }else {
            iRssi=-rssi;
        }*//*
        *//*  float power =  ((float) iRssi-65)/ 2;*//*
        float diff=(iRssi-rxpower1);
        double power = diff/(10* 2.1);
        //10的power次方
        return (float) (Math.pow(10, power)+rxpower1*0.001/diff);*/
        float iRssi = Math.abs(rssi);
        float power = (iRssi-Math.abs(rxpower))/(10*index);
        return Math.pow(10, power);
    }



    public static   double  calcRssi(float dis,float rxpower){
        double dasd = Math.log10(dis);
        double rssi = 0-(dasd * (10 * index) + Math.abs(rxpower));
        return rssi;
/*        //abs是求绝对值的函数
        float iRssi = Math.abs(rssi);
        float rxpower1=Math.abs(rxpower);
      *//*  int iRssi;
        if(rssi>=0){
            iRssi=rssi;
        }else {
            iRssi=-rssi;
        }*//*
         *//*  float power =  ((float) iRssi-65)/ 2;*//*
        float diff=(iRssi-rxpower1);
        double power = diff/(10* 2.1);
        //10的power次方
        return (float) (Math.pow(10, power)+rxpower1*0.001/diff);*/
       /* float iRssi = Math.abs(rssi);
        float power = (iRssi-Math.abs(rxpower))/(10*index);
        return Math.pow(10, power);*/
    }

    public static  double motilpleCalcult(float rssi){
      //  -0.0002x4 - 0.071x3 - 8.0088x2 - 400.51x - 7494.9
      //  0.0002x5 + 0.0702x4 + 10.381x3 + 764.83x2 + 28095x + 411596
        //2E-05x4 + 0.005x3 + 0.4497x2 + 18.046x + 270.22
        // y = 0.000021 x4 + 0.004985 x3 + 0.449683 x2 + 18.045781 x + 270.217317
      double dis = 000021*rssi*rssi*rssi*rssi* +0.004985*rssi*rssi*rssi* +0.449683 *rssi*rssi +18.045781*rssi +270.22 ;
      //  double dis = -0.0004*rssi*rssi*rssi* -0.0678*rssi*rssi -3.6128*rssi -64.539 ;

       // double dis = 0.0002*rssi*rssi*rssi*rssi*rssi+0.0702*rssi*rssi*rssi*rssi+10.381*rssi*rssi*rssi* +764.83*rssi*rssi +28095*rssi + 411596;
        return dis;
    }
    public static void main(String[] args) {

        double dis = calcDistByRSSI(-77, -55);
        System.out.println(dis);
            double rss=          calcRssi((float) 12,-55);
        System.out.println(rss);
            /*    float[] rssi={-56,-63, -67,-68,-65, -73, -75, -65, -74,-77,-80,-70,-80,-81, -80};
          float[] index={1.7f,1.8f,1.9f,2.0f,2.1f,2.2f,2.3f,2.4f};
        for(int k=0;k<index.length;k++) {
            System.out.println(index[k]);
            BleRssiDis.index=index[k];
            for (int i = 0; i < rssi.length; i++) {
                double dis = calcDistByRSSI(rssi[i], -56);
                double dis_motiple = motilpleCalcult(rssi[i]);
                // System.out.println("rel dis:"+(i+1));
                System.out.println(dis);
                // System.out.println("mul dis:"+dis_motiple );
                // System.out.println();
            }
        }*/

    }

}
