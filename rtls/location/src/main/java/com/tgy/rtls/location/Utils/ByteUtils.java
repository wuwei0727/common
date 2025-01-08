package com.tgy.rtls.location.Utils;

import com.tgy.rtls.location.model.Cmd;
import com.tgy.rtls.location.struct.BsPower;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ByteUtils {

    public static void main(String[] args) throws IOException {

        String url="http://192.168.1.80:39621/audio/11.wav";
       // toByteArray3(url);
        openFile(url);

        int i = 123456;
        byte[] bs = intToBytes(i);
        System.out.println(bs[0]);
        System.out.println(bs[1]);
        System.out.println(bs[2]);
        System.out.println(bs[3]);

        byte[] b2 = {(byte)0x00, 0x00,0x00,(byte)0x01,
                      0x00,
                      0x00,0x00,0x00,0x00,
                      0x00,0x00,0x00,0x00
        };


            BsPower ss=new BsPower();

        ByteBuffer buffer= ByteBuffer.allocate(10+4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)12);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)4);//messageid
        buffer.put((byte)0x05);//配置指令
        buffer.put((byte)0x06);//功率配置
        buffer.put((byte)0x00);
        buffer.put((byte)0x01);
        System.out.println(printHexString(buffer.array()));
        String sdas="/doe/193aa67e-9f71-4b5b-980e-4a8bb8f43d91.png";
        String[] sdasarray=sdas.split("/");
            //打印输出，这里输出byte字符串

     /*   List arr=new ArrayList();
        arr.add(si);
        arr.add(s2);
        arr.add(s3);
        Collections.sort(arr);*/


            //如果想打印每一个项，直接
     /*   ss.battery_volt.set(12);
        ss.charge_state.set((short)1);
        ss.pkgId.set(112);
        ss.charge_volt.set(122);
        ss.setByteBufferPosition(0);
       ByteBuffer buf = ByteBuffer.wrap(ss.);*/
    /*   buf.order(ByteOrder.LITTLE_ENDIAN);
           ss.setByteBuffer(buf, 0);*/
      //  System.out.println("before:"+printHexString(ss.getByteBuffer().array()));
      //  ss.getByteBuffer().put((byte)0x00);
      //  System.out.println("after:"+ss.getByteBuffer().array().length);
      //  BsPower ssd=new BsPower(1,(byte)0x00,1,1);
         // ss.pkgId.set(2);
         //   System.out.println("ssd:"+ssd);
          //  System.out.println("pkgid"+ss);
          /*  byte[] das=DataProcess.objectToBytes(ss);*/
         //   ByteUtils.printHexString(das);
        //  BsPower sss=(BsPower) DataProcess.bytesToObject(das);



        System.out.println("long:"+new Cmd(b2).getCmd());
    /*    timestamp:909427734016
        timestamp:196685938135
        timestamp:196759021056
        timestamp:909500883872*/
        //dis158.48409168965392


      /*  ByteBuffer buffer= ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(10);
        buffer.putInt(20);
        System.out.println(printHexString(buffer.array()));;*/
 /*      BsError sss=new BsError();
        BsError1 ssss=new BsError1();

        sss.setByteBuffer(bu4f,0);
        ssss.setByteBuffer(bu4f,8);*/




       // System.out.println("long:"+bu4f.array().length);



    }

    static double getdiff(BigDecimal x2, BigDecimal x1) {
        double diff;
        if (x2.doubleValue() < 1 || x1.doubleValue() < 1 || x2.doubleValue() > 1099511627775d || x1.doubleValue() > 1099511627775d)
            return 0;
        else {
            if (x2.subtract(x1).doubleValue() < 0) {
                x2 = x2.add(new BigDecimal("1099511627775"));
            }
            diff = x2.subtract(x1).doubleValue();
            return diff;
        }

    }

    static double getDis(BigDecimal timeStamp[], double anneDelayBS0, double anneDelayTag) {
        //����2����վ�����й̶��߶ȵ�ƽ�涨λ
        //timeStamp[T0��1����վ����ĵ�һ��ʱ���  T1 ��ǩ�յ��źŵ�ʱ��� T2 ��ǩ�����ʱ��� T3 ��1����վ�յ��źŵ�ʱ���]
        //anneDelay ������ʱ  ��վ�ͱ�ǩ��ͬ 77.38


        //   double coef = 0.00469176397861579;
        double coef=  0.00469035686786358173076923076923;

        double dT[] = new double[2];
        double dis1;
        dT[0] = getdiff(timeStamp[3], timeStamp[0]);
        dT[1] = getdiff(timeStamp[2], timeStamp[1]);
        dis1 = (dT[0] - dT[1]) * coef / 2 - anneDelayBS0 - anneDelayTag;
        dis1 = dis1 + 0.0089*dis1 + 0.1125;

        return dis1;

    }
    public static   long byte2short(byte[] res) {
        // 涓�涓猙yte鏁版嵁宸︾Щ24浣嶅彉鎴�0x??000000锛屽啀鍙崇Щ8浣嶅彉鎴�0x00??0000
        long targets;

        {
            targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00);
        }
        return targets;
    }
    //ip鍦板潃杞寲涓篵yte
    public static byte[] hostip (){


        String literals[];
        byte[] ip=null;
        try {
            literals = InetAddress.getLocalHost().getHostAddress().split("\\.",4);
            ip=new byte[4];
            int c=3;
            for(String d:literals) {
                ip[c--]=(byte) Short.parseShort(d);
                //System.out.println("杞崲鍚庡瓧鑺�:"+Arrays.toString(ip));
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return ip ;

    }
    public static byte[] bytesReverseOrder(byte[] b) {
        int length = b.length;
        byte[] result = new byte[length];
        for(int i=0; i<length; i++) {
            result[length-i-1] = b[i];
        }
        return result;
    }
    public static  String printHexString( byte[] b) {
        String res="";
        for (int i = 0,len=b.length; i < len; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            res=res+hex;
        }
        return res;
    }
    //已经小端处理
    public static  byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 灏嗘渶浣庝綅淇濆瓨鍦ㄦ渶浣庝綅
            temp = temp >> 8; // 鍚戝彸绉�8浣�
        }
        return b;
    }
    //byte 鏁扮粍涓� long 鐨勭浉浜掕浆鎹�
    public static  byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }
    public static Integer getBitByByte(byte b, int index) {
        if(index >= 8) { return null; }
        Integer val = null;
        String binStr = byteToBin(b);
        val = Integer.parseInt(String.valueOf(binStr.charAt(index)));
        return val;
    }
    /**
     * 把单个字节转换成二进制字符串
     */
    public static String byteToBin(byte b) {
        String zero = "00000000";
        String binStr = Integer.toBinaryString(b & 0xFF);
        if(binStr.length() < 8) {
            binStr = zero.substring(0, 8 -binStr.length()) + binStr;
        }
        //System.out.println(binStr);
        return binStr;
    }

    // 杩炴帴byte鏁扮粍

    public static   byte[] connecttwobyte( byte[] data1,byte[] data2 )
    {
        byte[] data3 = new byte[data1.length+data2.length];
        System.arraycopy(data1,0,data3,0,data1.length);
        System.arraycopy(data2,0,data3,data1.length,data2.length);
        return data3;
    }





    //宸茬粡灏忕澶勭悊
    public static  long bytes2long(byte[] b) {
        long temp = 0;
        long res = 0;
        if(b.length==8){
            for (int i=7;i>=0;i--) {
                res <<= 8;
                temp = b[i] & 0xff;
                res |= temp;
            }
        }
        else{
            for (int i=3;i>=0;i--) {
                res <<= 8;
                temp = b[i] & 0xff;
                res |= temp;
            }



        }
        return res;
    }
    public static byte[] intToBytes(long num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >>> (24 - i * 8));
        }
        b=bytesReverseOrder(b);
        return b;
    }
    public static float bytes2float(byte[] b) {
        int l;
        int index=0;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }
    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    public static  String bytetoString( byte[] b) {

        String a="";
        for (int i = 0,len=b.length; i < len; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            a=a+hex;

        }
        return a;
    }
    public static float byte2float(byte[] b) {
        int l,index=0;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static final BigDecimal readUnsignedLong(long value) throws IOException {
        if (value >= 0)
            return new BigDecimal(value);
        long lowValue = value & 0x7fffffffffffffffL;
        return BigDecimal.valueOf(lowValue).add(BigDecimal.valueOf(Long.MAX_VALUE)).add(BigDecimal.valueOf(1));
    }


    public static byte[] toByteArray3(String filename) throws IOException {

        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(filename, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                // System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static byte[] openFile(String filePath) {
        int HttpResult; // 服务器返回的状态
        byte[] ee = null;
        try
        {
            URL url =new URL(filePath); // 创建URL
            URLConnection urlconn = url.openConnection(); // 试图连接并取得返回状态码
            urlconn.connect();
            HttpURLConnection httpconn =(HttpURLConnection)urlconn;
            HttpResult = httpconn.getResponseCode();
            if(HttpResult != HttpURLConnection.HTTP_OK) {
                System.out.print("无法连接到");
            } else {
                int filesize = urlconn.getContentLength(); // 取数据长度
                InputStream inStream = urlconn.getInputStream();
                byte[] buffer = new byte[1024];
                //每次读取的字符串长度，如果为-1，代表全部读取完毕
                int len = 0;
                //使用一个输入流从buffer里把数据读取出来
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                while ((len = inStream.read(buffer)) != -1) {
                    //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                    outStream.write(buffer, 0, len);
                }
             byte[] res=   outStream.toByteArray();

                return res;

            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return  ee;
    }

    public static	int BKDRHash(byte[] dat, int len)
    {
        long seed =131; /* 31 131 1313 13131 131313 etc.. */
        long hash =0;
        int i =0;

        for (i =0; i < len; i++)
        {

            hash = (long)((hash * seed + (dat[i]&0x0ff))%4294967296L);

        }
        return (int)hash;
    }



}
