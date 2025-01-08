package com.tgy.rtls.gateway.fft.count;




import java.util.LinkedList;
import java.util.Queue;

public class CountStep {

    volatile  int State;
    //加速度数组
    int len=24;
    //运动状态判断数组
    private float[] stateValue=new float[20];
    Queue<Double> queue = new LinkedList<Double>();
    int statecount;

    int count=0;
    volatile static int index;





    public void refreshAcc(float[] values,long timestamp,float angle) {
        double acc= values[0];
        queue.add(acc);
        //检查运动状态
        stateValue[statecount%20]=(float) acc;
        statecount++;


        checkState();
        if(State==1&&queue.size()>16)
        {
           System.out.println( "检测到运动开始计算波峰");
            Object[] accdata = queue.toArray();
            double[] array=new double[accdata.length];
            for(int i=0;i<array.length;i++){
                array[i]=(double)accdata[i];
            }
            int cou=getPeakNum3(array);
          System.out.println("检测到"+cou+"个波峰");
            if(cou>0){
                for(int i=0;i<index;i++)
                    queue.poll();
            }

        }
    }
    //运动状态判断
    private void checkState(){
        float ave=0;
        float var=0;

        //求均值
        for (int i=0;i<stateValue.length;++i){
            ave+=stateValue[i];
        }
        ave/=stateValue.length;

        //求方差
        for (int i=0;i<stateValue.length;++i){
            var+=(stateValue[i]-ave)*(stateValue[i]-ave);
        }
        var/=stateValue.length;


        //状态判决
        State=var> 0.4?1:0;
        //  MainActivity.appendDebuginf("动静判断方差："+var);
    }
    public static int getPeakNum3(double[] data){
        int peak=0;

        double[] PeakAndTrough=new double[data.length];

        //需要三个不同的值进行比较，取lo,mid，hi分别为三值
        for (int lo=0,mid=1,hi=2;hi<data.length;hi++){
            //先令data[lo]不等于data[mid]
            while (mid<data.length&&data[mid]==data[lo]){
                mid++;
            }

            hi=mid+1;

            //令data[hi]不等于data[mid]
            while (hi<data.length&&data[hi]==data[mid]){
                hi++;
            }

            if (hi>=data.length){
                break;
            }

            //检测是否为峰值
            if (data[mid]>data[lo]&&data[mid]>data[hi]){
                PeakAndTrough[mid]=1;       //1代表波峰
            }else if(data[mid]<data[lo]&&data[mid]<data[hi]){
                PeakAndTrough[mid]=-1;      //-1代表波谷
            }

            lo=mid;
            mid=hi;
        }

        //计算均值
        float ave=0;
        for (int i=0;i<data.length;i++){
            ave+=data[i];
        }
        ave/=data.length;

        //排除大于均值的波谷和小于均值的波峰
        for (int i=0;i<PeakAndTrough.length;i++){
            if (((PeakAndTrough[i]>0&&data[i]<ave)||(PeakAndTrough[i]<0&&data[i]>ave)&&(Math.abs(data[i]-ave)<1.6))){
                PeakAndTrough[i]=0;
            }
        }

        //统计波峰数量

        for (int i=0;i<PeakAndTrough.length;){
            while (i<PeakAndTrough.length&&PeakAndTrough[i]<=0){
                i++;
            }

            if (i>=PeakAndTrough.length){
                break;
            }

            peak++;
            index=i;

            while (i<PeakAndTrough.length&&PeakAndTrough[i]>=0){
                i++;
            }

        }
        System.out.println("final index:"+index);

        return peak;
    }
}

