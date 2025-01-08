package com.tgy.rtls.gateway.fft;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadTxt {
    /**传入txt路径读取txt文件
     * @param txtPath
     * @return 返回读取到的内容
     */
    public static List readTxt(String txtPath) {
        File file = new File(txtPath);
        List res=new ArrayList();
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuffer sb = new StringBuffer();
                String text = null;
                while((text = bufferedReader.readLine()) != null){
                    sb.append(text);
                    res.add(Double.valueOf(text));
                }
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**使用FileOutputStream来写入txt文件
     * @param txtPath txt文件路径
     * @param content 需要写入的文本
     */
    public static void writeTxt(String txtPath,String content){
        FileOutputStream fileOutputStream = null;
        File file = new File(txtPath);
        try {
            if(file.exists()){
                //判断文件是否存在，如果不存在就新建一个txt
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPeakNum3(double[] data){
        int peak=0;
        int trough=0;

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
            if (((PeakAndTrough[i]>0&&data[i]<ave)||(PeakAndTrough[i]<0&&data[i]>ave)/*&&(Math.abs(data[i]-ave)<1.6)*/)){
                PeakAndTrough[i]=0;
            }
        }

        //统计波峰数量
        int index=0;


       for (int i=0;i<PeakAndTrough.length;){
            while (i<PeakAndTrough.length&&PeakAndTrough[i]<=0){
                i++;

            }

            if (i>=PeakAndTrough.length){
                break;
            }

            peak++;
            index=i;
           System.out.println("波峰计数"+peak+"位置"+index);

            while (i<PeakAndTrough.length&&PeakAndTrough[i]>=0){
                i++;


            }

        }


        for (int i=0;i<PeakAndTrough.length;){
            while (i<PeakAndTrough.length&&PeakAndTrough[i]>=0){
                i++;
            }

            if (i>=PeakAndTrough.length){
                break;
            }

            trough++;
            index=i;
            System.out.println("波谷计数"+trough+"位置"+index);
            while (i<PeakAndTrough.length&&PeakAndTrough[i]<=0){
                i++;
            }

        }
        //System.out.println("final index:"+index);
        System.out.println("波峰计数"+peak+"波谷计数"+trough);
        return peak;
    }
}