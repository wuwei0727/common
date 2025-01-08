package com.tgy.rtls.data.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author 许强
 * @Package com.example.util
 * @date 2019/6/13
 */
public class NullUtils {

    /*
    * 非空判断
    * */
    public static boolean isEmpty(Object obj){
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }
        if (obj instanceof Object[]) {
            Object[] object = (Object[]) obj;
            if (object.length == 0) {
                return true;
            }
            boolean empty = true;
            for (int i = 0; i < object.length; i++) {
                if (!isEmpty(object[i])) {
                    empty = false;
                    break;
                }
            }
            return empty;
        }
        return false;
    }

    /**
     * 获取时间差，单位为秒  T2>T1
     * @param formatTime1
     * @param formatTime2
     * @return
     */
    public static long getTimeDifference(Timestamp formatTime1, Timestamp formatTime2) {
        SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.SSS");
        long t1 = 0L;
        long t2 = 0L;
        try {
            t1 = timeformat.parse(getTimeStampNumberFormat(formatTime1)).getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            t2 = timeformat.parse(getTimeStampNumberFormat(formatTime2)).getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //鍥犱负t1-t2寰楀埌鐨勬槸姣绾�,鎵�浠ヨ鍒�3600000寰楀嚭灏忔椂.绠楀ぉ鏁版垨绉掑悓鐞�
        //  int hours=(int) ((t1 - t2)/3600000);
        //  int minutes=(int) (((t1 - t2)/1000-hours*3600)/60);
        //  int second=(int) ((t1 - t2)/1000-hours*3600-minutes*60);
        return (t2-t1)/1000;
    }

    public static String getTimeStampNumberFormat(Timestamp formatTime) {
        SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.SSS", new Locale("zh", "cn"));
        return m_format.format(formatTime);
    }

    /**
     * 从完整标签编号中获取后两位id
     * @param sid1 识别码第一个字节
     * @param sid2 识别码第二个字节
     * @param fullTagid  完成标签编号
     * @return  去除识别码以后的标签编号
     */
    public static Integer getTagidFromFull(byte sid1, byte sid2, int fullTagid){
        if(checkTagid(sid1,sid2,fullTagid)) {
            return fullTagid-sid1<<24-sid2<<16;
        } else {
            return null;
        }
    }

    /**
     * 获取标签识别码
     * @param fullTagid  完整标签编号
     * @return  返回值为标签识别码数组，其中第一个字节为识别码第一个字节，第二个为识别码第二个数组
     */
    public static byte[] getSidFromFull( int fullTagid){
        byte[] bytes=ByteUtils.intToBytes(fullTagid);
        byte[] sids={bytes[3],bytes[2]};
        return sids;
    }

    /**
     * 判断完整标签编号是否是某个识别码
     * @param sid1 识别码第一个字节
     * @param sid2  识别码第二个字节
     * @param fullTagid
     * @return
     */
    public static Boolean checkTagid(byte sid1,byte sid2,int fullTagid){
        byte[] bytes=ByteUtils.intToBytes(fullTagid);
        if(bytes[3]==sid1&&bytes[2]==sid2){
            return true;
        }else {
            return false;
        }
    }

   public static Long StringToDate(long year,long month,long day,long hour,long minute,long second,long milisecond){

        String date_string=year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second+"."+milisecond;
       Date currentTime = new Date();
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
       try {
           Date date = formatter.parse(date_string);
           return (date.getTime());
       }catch (Exception e){
           return null;
       }

    }

    public static void main(String[] args) {
        StringToDate((short)2020,(short)12,(short)1,(short)12,(short)12,(short)12,(short)121);
    }
}
