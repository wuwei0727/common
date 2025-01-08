package com.tgy.rtls.data.algorithm;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 计算多边形外围轮廓
 */
public class ConvexReg {
    public static List isInPolygon(Double[][] points){
        int len=points.length;
        List<ResSort> list_one=new ArrayList();
        List<ResSort> list_two=new ArrayList();
        List<ResSort> list_three=new ArrayList();
        List<ResSort> list_four=new ArrayList();
        List<ResSort> resList=new ArrayList();
        for (int i=1;i<len;i++){
            double dis=Math.sqrt((points[i][0] - points[0][0]) * (points[i][0] - points[0][0]) + (points[i][1] - points[0][1]) * (points[i][1] - points[0][1]));
            float sin = (float) ((points[i][1] - points[0][1]) / dis);
            float cos = (float) ((points[i][0] - points[0][0]) / dis);
            if(sin>=0&&cos>=0)
                list_one.add( new ResSort(points[i][0],points[i][1],0d,null,sin,0-dis));
            else if(sin>0&&cos<0)
                list_two.add( new ResSort(points[i][0],points[i][1],0d,null,sin,0-dis));
            else if(sin<=0&&cos<=0)
                list_three.add( new ResSort(points[i][0],points[i][1],0d,null,sin,0-dis));
            else if(sin<0&&cos>0)
                list_four.add( new ResSort(points[i][0],points[i][1],0d,null,sin,0-dis));
        }
        Collections.sort(list_one);
        Collections.sort(list_two);
        Collections.sort(list_three);
        Collections.sort(list_four);


         List<List<ResSort>> total=new ArrayList<>();
        total.add(list_one);
        total.add(list_two);
        total.add(list_three);
        total.add(list_four);

        for (List<ResSort> eachList: total
             ) {
            List<ResSort> deleteList=new ArrayList();
            ResSort cache=null;
            for (ResSort res:eachList
            ) {
                if(cache==null){
                    cache= res;
                }else {
                    if (cache.getIner().floatValue() == res.getIner().floatValue()) {
                        deleteList.add(res);
                    } else {
                        cache = res;
                    }
                }

            }
            for (ResSort delete:deleteList) {
                eachList.remove(delete);
            }

        }

        Collections.reverse(list_two);
        Collections.reverse(list_three);
        resList.addAll(list_four);
        resList.addAll(list_one);
        resList.addAll(list_two);
        resList.addAll(list_three);
        resList.add(new ResSort(points[0][0],points[0][1],points[0][2],null,0f,0d));

        List<Point2D.Double> finalRes=new ArrayList();
        for (ResSort res1:resList
             ) {
            finalRes.add(new Point2D.Double(res1.x,res1.y));
        }
        return finalRes;

    }



    public static void main(String[] args) {
      /*  List<Point2D.Double> ss=new ArrayList<>();
        Point2D.Double p1 = new Point2D.Double();
        p1.setLocation( 1.08,2.54);
        Point2D.Double p2 = new Point2D.Double();
        p2.setLocation(0.7,7.07);
        Point2D.Double p3 = new Point2D.Double();
        p3.setLocation( 5.54,2.54);
        Point2D.Double p4 = new Point2D.Double();
        p4.setLocation(3.31,11.6);
        ss.add(p1);
        ss.add(p2);
        ss.add(p4);
        ss.add(p3);
        Point2D.Double test = new Point2D.Double();
    *//*    1.08:2.54
        0.7:7.07
        5.54:2.54
        3.31:11.6
        1.6526360674348135:3.6314574009286726*//*
        test.setLocation(1.6526360674348135,3.6314574009286726);

     boolean res=   isInPolygon(test,ss);
        System.out.println(res);*/
        Double[][] bsPos={
                {3.31d,11.62d,0d},
                {3.34d,2.51d,0d},
                {0.7d,7d,0d},
                {5.54d,7.1d,0d},
                {1.3d,11.6d,0d},
                {5.5d,11.6d,0d},
                {5.5d,2.54d,0d},
                {1.08d,2.54d,0d},
                };
        List res = isInPolygon(bsPos);
        System.out.println();

    }


}
