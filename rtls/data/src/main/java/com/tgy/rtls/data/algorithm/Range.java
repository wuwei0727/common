package com.tgy.rtls.data.algorithm;

import java.math.BigDecimal;

public class Range {
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

    public static double getDis(BigDecimal timeStamp[], double anneDelayBS0, double anneDelayTag) {



         //   double coef = 0.00469176397861579;
         // double coef=  0.00469035686786358173076923076923;
         //空气光速 299552816  0.0046880015704    299.55
         // 真空光速 299792458 0.0046917519677  299.79
         //  63897.76357827476
             double coef=0.00465845245436762;



        double dT[] = new double[2];
        double dis1;
        dT[0] = getdiff(timeStamp[3], timeStamp[0]);
        dT[1] = getdiff(timeStamp[2], timeStamp[1]);
        dis1 = (dT[0] - dT[1]) * coef / 2 - anneDelayBS0 - anneDelayTag;
        //dis1 = dis1 + 0.0089*dis1 + 0.1125;

        return dis1;

    }
}
