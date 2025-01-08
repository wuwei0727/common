package com.tgy.rtls.data.gather;


import com.tgy.rtls.data.entity.common.Point2d;

import java.sql.Timestamp;
import java.util.*;

public class Cal {


  public   double inf=1e-6;
   public  double dist(Point2d p1, Point2d p2) {
        return Math.sqrt((p1.getX()-p2.getX())*(p1.getX()-p2.getX())+(p1.getY()-p2.getY())*(p1.getY()-p2.getY()));
    }


    public Point2d getCycle(Point2d p1, Point2d p2, double r){
        Point2d mid = new Point2d((p1.getX()+p2.getX())/2,(p1.getY()+p2.getY())/2);
        double angle = Math.atan2(p1.getX()-p2.getX(),p2.getY()-p1.getY());
        double d = Math.sqrt(r*r-Math.pow(dist(p1,mid),2));
        mid=new Point2d(mid.getX()+d*Math.cos(angle),mid.getY()+d*Math.sin(angle));
        return mid;
    }
public Gather getMax(double r, int count, List<Point2d> p ){
       Gather gatherRes=new Gather();
    int num = p.size();
    double a,b;
    double eps=1e-8;
    int i,j;
    int ans=0;

    for(i=0; i<num; i++)
    {
        for(j=i+1; j<num; j++)
        {
            if(dist(p.get(i),p.get(j)) > 2.0*r)
                continue;
            Point2d center = getCycle(p.get(i),p.get(j),r);
            double x=0,y=0;
            List<String> name_queue = new LinkedList<String>();
            List<String> tagid_queue = new LinkedList<String>();
            int cnt = 0;
            for(int k=0; k<num; k++)
                if(dist(center,p.get(k)) < r+eps)
                {
                    Point2d point = p.get(k);
                    name_queue.add(point.getName());
                    tagid_queue.add(point.getTagid());
                    x=x+point.getX();
                    y=y+point.getY();
                    cnt++;
                }

            ans = Math.max(ans,cnt);
                if(ans==cnt){
                    gatherRes.warningIndex=null;
                    gatherRes.warningIndex=name_queue;
                    gatherRes.warningTagid=tagid_queue;
                    double finalsize=tagid_queue.size();
                    if(finalsize!=0) {
                        double[] centerpos = {x/finalsize, y/finalsize};
                        gatherRes.centerPos = centerpos;
                    }
                }
        }
    }


    if(ans>=count){
        gatherRes.warningflag=true;
    }
    return gatherRes;
}


    public static void main(String[] args) {


        List<Point2d> allperson_everyfloor=new ArrayList<>();//每层人员位置列表，包含x，y,人员名称
        Random rand=new Random();

      for(int i=0;i<20;i++){//20人数，
            float f1 = rand.nextFloat();
            float f2 = rand.nextFloat();
            Point2d p0=new   Point2d(100+f1,100+f2);
            p0.setName(i+"许");
          //  System.out.println("::"+i*i+":"+Math.sqrt((double)i));
            allperson_everyfloor.add(p0);

        }

        System.out.println("start:"+new Timestamp(new Date().getTime()));
        Cal cal= new Cal();
        Gather res= cal.getMax(0.1,0,allperson_everyfloor);//r 为聚集设置半径，count为聚集人数，res.warningflag为true则报警，返回报警人员名称数组

      //  System.out.println("end:"+new Timestamp(new Date().getTime())+":"+res.warningflag+"::"+res.warningIndex+"centerpos:"+res.centerPos[0]+":"+res.centerPos[1]);
     }
    }


