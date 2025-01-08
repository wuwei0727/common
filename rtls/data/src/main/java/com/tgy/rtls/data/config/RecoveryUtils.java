package com.tgy.rtls.data.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 许强
 * @Package com.example.util
 * @date 2019/11/13
 */
public class RecoveryUtils {

        public static long bytes2long(byte[] b) {
        long temp = 0;
        long res = 0;
        if (b.length == 8) {
            for (int i = 7; i >= 0; i--) {
                res <<= 8;
                temp = b[i] & 0xff;
                res |= temp;
            }
        } else {
            for (int i = 3; i >= 0; i--) {
                res <<= 8;
                temp = b[i] & 0xff;
                res |= temp;
            }


        }
        return res;
    }

        public static float bytes2float(byte[] b) {
            int l;
            int index = 0;
            l = b[index + 0];
            l &= 0xff;
            l |= ((long) b[index + 1] << 8);
            l &= 0xffff;
            l |= ((long) b[index + 2] << 16);
            l &= 0xffffff;
            l |= ((long) b[index + 3] << 24);
            return Float.intBitsToFloat(l);
        }

        public static long byte2short(byte[] res) {
            long targets;

            {
                targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00);
            }
            return targets;
        }

        public static Date getNowDate(long year, int month, int day, int hour, int minute, int second) {

            String strDate = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(strDate, pos);
            return strtodate;
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

        public static double getDis(BigDecimal timeStamp[], double anneDelayBS0, double anneDelayTag) {
            //double coef = 0.00469176397861579;
            double coef= 0.00469035686786358173076923076923;
            double dT[] = new double[2];
            double dis1;
            dT[0] = getdiff(timeStamp[3], timeStamp[0]);
            dT[1] = getdiff(timeStamp[2], timeStamp[1]);
            dis1 = (dT[0] - dT[1]) * coef / 2 - anneDelayBS0 - anneDelayTag;
           // dis1 = dis1 + 0.0089 * dis1 + 0.1125;

            return dis1;

        }

        public static double getdiff(BigDecimal x2, BigDecimal x1) {
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

        public static BigDecimal readUnsignedLong(long value) throws IOException {
            if (value >= 0)
                return new BigDecimal(value);
            long lowValue = value & 0x7fffffffffffffffL;
            return BigDecimal.valueOf(lowValue).add(BigDecimal.valueOf(Long.MAX_VALUE)).add(BigDecimal.valueOf(1));
        }

}
