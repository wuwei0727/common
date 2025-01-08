package com.tgy.rtls.data.algorithm;

public class PercentToPosition {


    public static void main(String[] args) {
        Double[] p1={20d,40d,110d};
        Double[] p2={20d,0d,110d};
        percentToPosition(p1,p2,0.87);
    }
    public  static  double[]   percentToPosition(Double[] point1,Double[] point2,double percent){
        Double[] point3_1={point2[0]-point1[0],point2[1]-point1[1],point2[2]-point1[2]};
        if(point3_1[0]==0){
            point3_1[0]=0.00001;
        }
       double dis_2=getDis(point1,point2)*percent;
       double x=Math.sqrt(Math.pow(dis_2,2)/(1+Math.pow((point3_1[2]/point3_1[0]),2)+Math.pow((point3_1[1]/point3_1[0]),2)));
       if(point3_1[0]*x<0)
           x=0-x;
       double y=x*point3_1[1]/point3_1[0];
       double z=x*point3_1[2]/point3_1[0];
       double realx=x+point1[0];
        double realy=y+point1[1];
        double realz=z+point1[2];
        double[] res={Double.valueOf(String.format("%.2f",realx)),Double.valueOf(String.format("%.2f",realy)),Double.valueOf(String.format("%.2f",realz))};
        return res;

    }

    public  static  double[]   percentToPosition(double[] point1,double[] point2,double percent){
        Double[] point3_1={point2[0]-point1[0],point2[1]-point1[1],point2[2]-point1[2]};
        if(point3_1[0]==0){
            point3_1[0]=0.00001;
        }
        double dis_2=getDis(point1,point2)*percent;
        double x=Math.sqrt(Math.pow(dis_2,2)/(1+Math.pow((point3_1[2]/point3_1[0]),2)+Math.pow((point3_1[1]/point3_1[0]),2)));
        if(point3_1[0]*x<0)
            x=0-x;
        double y=x*point3_1[1]/point3_1[0];
        double z=x*point3_1[2]/point3_1[0];
        double realx=x+point1[0];
        double realy=y+point1[1];
        double realz=z+point1[2];
        double[] res={Double.valueOf(String.format("%.2f",realx)),Double.valueOf(String.format("%.2f",realy)),Double.valueOf(String.format("%.2f",realz))};
        return res;

    }

   public static double  getDis(Double[] point1,Double[] point2){
        double sum=0;
        for(int i=0;i<3;i++ )
            sum=sum+((point1[i]-point2[i])*(point1[i]-point2[i]));
        return Math.sqrt(sum);
    }
    public static double  getDis(double[] point1,double[] point2){
        double sum=0;
        for(int i=0;i<3;i++ )
            sum=sum+((point1[i]-point2[i])*(point1[i]-point2[i]));
        return Math.sqrt(sum);
    }




}
