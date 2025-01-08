package com.tgy.rtls.data.algorithm.crosspoint;

/**
 * @Author: Zhangwenshun
 * @Description:
 * @Date: Created in 10:20 2020/9/10
 * @Modified by:
 */
public class CGGeometryLib {
    /**
     * 封装一下 Math 的 pow 、sqrt 方法，调用起来方便一些~
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double pow(double d1, double d2) {
        return Math.pow(d1, d2);
    }

    public static double sqrt(double d) {
        return Math.sqrt(d);
    }

    public static double sin(double theta) {
        return Math.sin(theta);
    }

    public static double cos(double theta) {
        return Math.cos(theta);
    }

    /**
     * 传入直线上的两个点以及圆心和半径
     * 返回直线过圆心与圆的两个交点
     *
     * @param p1     另外的点
     * @param p2     圆心
     * @param coc    圆心
     * @param radius 圆半径
     * @return 直线过圆心与圆的两个交点
     * @author zhangwenshun
     */
    public static double[] getLineCircleNode(CGPoint p1, CGPoint p2, CGPoint coc, double radius) {
        CGPoint[] target = new CGPoint[2];
        CGLine l1 = new CGLine(p1, p2);
        if (l1.iskExists()) {
            if (l1.k != 0) {
                // 经过数学运算，得出二元一次方程组的表达式
                double A = pow(l1.k, 2) + 1;
                double B = 2 * (l1.k * l1.b - l1.k * coc.y - coc.x);
                double C = pow(coc.x, 2) + pow((l1.b - coc.y), 2) - pow(radius, 2);
                double delta = pow(B, 2) - 4 * A * C;

                if (delta < 0) {
                    // 经实践检验有一定几率走入该分支，必须做特殊化处理~
                    // 2012。04。28。20。01，精度不够所致，换成double后无该情况出现~
                    target[0] = new CGPoint(coc.x, coc.y - radius);
                    target[1] = new CGPoint(coc.x, coc.y + radius);
                } else {
                    double x1 = (-B + sqrt(delta)) / (2 * A);
                    double y1 = l1.k * x1 + l1.b;
                    target[0] = new CGPoint(x1, y1);

                    double x2 = (-B - sqrt(delta)) / (2 * A);
                    double y2 = l1.k * x2 + l1.b;
                    target[1] = new CGPoint(x2, y2);
                }
            } else {
                target[0] = new CGPoint(coc.x - radius, coc.y);
                target[1] = new CGPoint(coc.x + radius, coc.y);
            }
        } else {
            target[0] = new CGPoint(coc.x, coc.y - radius);
            target[1] = new CGPoint(coc.x, coc.y + radius);
        }
        int dir=0;
        double[] res=null;
        double[] k={p1.x-coc.x,p1.y-coc.y};
        for (int i = 0; i < target.length; i++) {
           // System.out.println(target[i].x+"::"+target[i].y);
           double k1=(target[i].x-coc.x);
           double k2=(target[i].y-coc.y);
            double kk=k1*k[0]+k2*k[1];
            if(kk>0){
                 res=new double[2];
                 res[0]=target[i].x;
                 res[1]=target[i].y;
            }
        }
        return res;
    }

    /**
     * 测试实用性
     *
     * @param args
     */
    public static void main(String[] args) {
        CGPoint p1 = new CGPoint(-13, 0);
        CGPoint p2 = new CGPoint(-1, 0);
        CGPoint coc = new CGPoint(-1, 0);
        double[] whatIWanted = getLineCircleNode(p1, p2, coc, 10);
           if(whatIWanted!=null)
            System.out.println(whatIWanted[0]+"::"+whatIWanted[1]);


    }
}
