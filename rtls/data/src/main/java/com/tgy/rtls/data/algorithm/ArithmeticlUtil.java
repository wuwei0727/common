package com.tgy.rtls.data.algorithm;
import com.tgy.rtls.data.entity.common.Point2d;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 许强
 * @Package com.example.util
 * @date 2019/6/21
 * 计算点是否在指定区域
 */
public class ArithmeticlUtil {
    public static boolean isInPolygon(Point2d point, List<Point2d> pts){
 /*       for (Point2D.Double pp:pts
             ) {
            System.out.println(pp.x+":"+pp.y);
        }
        System.out.println(point.x+":"+point.y);*/
        int N=pts.size();
        boolean boundOrVertex=true;//如果点在区域内，返回true
        int intersectCount=0;
        double precision = 2e-10; //浮点类型计算时候与0比较时候的容差
        Point2d p1, p2;//neighbour bound vertices
        Point2d p = point; //当前点00

        p1 = pts.get(0);//left vertex
        for(int i = 1; i <= N; ++i){//check all rays
            if(p.equals(p1)){
                return boundOrVertex;//p is an vertex
            }

            p2 = pts.get(i % N);//right vertex
            if(p.x < Math.min(p1.x, p2.x) || p.x > Math.max(p1.x, p2.x)){//ray is outside of our interests
                p1 = p2;
                continue;//next ray left point
            }

            if(p.x > Math.min(p1.x, p2.x) && p.x < Math.max(p1.x, p2.x)){//ray is crossing over by the algorithm (common part of)
                if(p.y <= Math.max(p1.y, p2.y)){//x is before of ray
                    if(p1.x == p2.x && p.y >= Math.min(p1.y, p2.y)){//overlies on a horizontal ray
                        return boundOrVertex;
                    }

                    if(p1.y == p2.y){//ray is vertical
                        if(p1.y == p.y){//overlies on a vertical ray
                            return boundOrVertex;
                       }else{//before ray
                            ++intersectCount;
                        }
                    }else{//cross point on the left side
                        double xinters = (p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y;//cross point of y
                        if(Math.abs(p.y - xinters) < precision){//overlies on a ray
                            return boundOrVertex;
                        }

                        if(p.y < xinters){//before ray
                            ++intersectCount;
                        }
                    }
                }
            }else{//special case when ray is crossing through the vertex
                if(p.x == p2.x && p.y <= p2.y){//p crossing over p2
                    Point2d p3 = pts.get((i+1) % N); //next vertex
                    if(p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(p1.x, p3.x)){//p.x lies between p1.x & p3.x
                        ++intersectCount;
                    }else{
                        intersectCount += 2;
                    }
                }
            }
            p1 = p2;//next ray left point
        }

        if(intersectCount % 2 == 0){//偶数在多边形外
            return false;
        } else { //奇数在多边形内
            return true;
        }
    }

    /*
     * p为待测点  list为多边形边界
     */
    public static String rayCasting(Point2d p, List<Point2d> list) {
        double px = p.getX(), py = p.getY();
        boolean flag = false;
        //
        for (int i = 0, l = list.size(), j = l - 1; i < l; j = i, i++) {
            //取出边界的相邻两个点
            double sx = list.get(i).getX(),
                    sy = list.get(i).getY(),
                    tx = list.get(j).getX(),
                    ty = list.get(j).getY();
            // 点与多边形顶点重合
            if ((sx == px && sy == py) || (tx == px && ty == py)) {
                return "on";
            }
            // 判断线段两端点是否在射线两侧
            //思路:作p点平行于y轴的射线 作s,t的平行线直线  如果射线穿过线段，则py的值在sy和ty之间
            if ((sy < py && ty >= py) || (sy >= py && ty < py)) {
                // 线段上与射线 Y 坐标相同的点的 X 坐标 ,即求射线与线段的交点
                double x = sx + (py - sy) * (tx - sx) / (ty - sy);
                // 点在多边形的边上
                if (x == px) {
                    return "on";
                }
                // 射线穿过多边形的边界
                if (x > px) {
                    flag = !flag;
                }
            }
        }
        // 射线穿过多边形边界的次数为奇数时点在多边形内
        return flag ? "in" : "out";
    }

    public static void main(String[] args) {
       /* dis range::5002.0:5003.0:5004.0:5007.0:in area0.0sqre6.2482756463488975:x:3.61714480571972:y:9.684573151189326
        5.55:11.5
        1.3:11.65
        0.7:7.07
        3.31:11.6
        3.633203098317578:10.163153270467046
        dis range::5002.0:5003.0:5005.0:5007.0:in area0.0sqre6.328553231469179:x:3.633203098317578:y:10.163153270467046
        5.54:2.54
        5.55:11.5
        1.3:11.65
        3.31:11.6
        3.6354571572329384:10.206618937354648
        dis range::5001.0:5002.0:5003.0:5007.0:in area0.0sqre4.675059118454556:x:3.6354571572329384:y:10.206618937354648*/

        List<Point2D.Double> ss=new ArrayList<>();
        Point2D.Double p1 = new Point2D.Double();
        p1.setLocation( 3.31,11.62);
        Point2D.Double p2 = new Point2D.Double();
        p2.setLocation(3.34,2.51);
        Point2D.Double p3 = new Point2D.Double();
        p3.setLocation( 0.7,7);
        Point2D.Double p4 = new Point2D.Double();
        p4.setLocation(5.54,7.1);
        Point2D.Double p5 = new Point2D.Double();
        p5.setLocation(1.3,11.6);
        Point2D.Double p6 = new Point2D.Double();
        p6.setLocation( 5.5,11.6);
        Point2D.Double p7 = new Point2D.Double();
        p7.setLocation(5.5,2.54);

        Point2D.Double p8 = new Point2D.Double();
        p8.setLocation(1.08,2.54);

        ss.add(p1);
        ss.add(p2);
        ss.add(p3);
        ss.add(p4);
        ss.add(p5);
        ss.add(p6);
        ss.add(p7);
        ss.add(p8);
        Point2D.Double test = new Point2D.Double();
    /*    1.08:2.54
        0.7:7.07
        5.54:2.54
        3.31:11.6
        1.6526360674348135:3.6314574009286726*/
        test.setLocation(5.7,7);
/*
     boolean res=   isInPolygon(test,ss);
        System.out.println(res);*/

    }


}
