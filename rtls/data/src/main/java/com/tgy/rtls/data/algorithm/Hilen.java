package com.tgy.rtls.data.algorithm;


import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Hilen {

    public static void main(String[] args) {
/*        getVerticalDis(8.13,4.85,10.6);
        double[] disc={5,5};
        double[][] pos={{0,0,0},{13.6,0,0}};
        double[][] res=location1D(disc,pos);
        System.out.println("x:"+res[0][0]);*/

 /*      double[][] bondary={{0,0,0},{10,0,0},{10,10,0},{0,10,0}};
       double[]  is={10,10.1,0};
       Boolean in= isPtInPoly(is,bondary);
        System.out.println("point is in "+in);*/
/*
        Point2D.Double p1 = new Point2D.Double(0d, 0d);
        Point2D.Double p2 = new Point2D.Double(4, 0d);
        Point2D.Double p3 = new Point2D.Double(4d, 4d);
        Point2D.Double p4 = new Point2D.Double(0d, 4d);
        List<Point2D.Double> sd=new ArrayList<>();
         sd.add(p1);
        sd.add(p2);
        sd.add(p3);
        sd.add(p4);
        double[] tag={2,0};
        double sad = getSqre(sd, tag);
*/
        Double[] disc={20d,0.1d};
        Double[][] pos={{1d,0d,1d},{2d,0d,1d}};
        double[][] res=location1D(disc,pos);
        System.out.println("x:"+res[0][0]);



    }
    //  a ,b 为标签与基站的距离，c为两个基站之间的距离，返回值为标签到两个基站的垂线距离
   public static double getVerticalDis(double a,double b,double c ){
        double p=(a+b+c)/2;
        double s=Math.sqrt(p*(p-a)*(p-b)*(p-c));
        double vertical=2*s/c;
       // System.out.println("垂线："+vertical);
        return vertical;
    }

  public static double getSqre(List<Point2D.Double> bsPos, double[] tagPos){
        int len=bsPos.size();
        double sum=0;
        List<Double> list =new ArrayList<>();
       for(int k=0;k<len;k++){
           Point2D.Double point1 ;
           Point2D.Double point2 ;
           if((k+1)==len){
               point1= bsPos.get(0);
               point2= bsPos.get(k);
           }else{
               point1= bsPos.get(k);
               point2= bsPos.get(k + 1);
           }

           double twoBs_dis = Math.sqrt((point1.x - point2.x) * (point1.x - point2.x)  +(point1.y - point2.y) *(point1.y - point2.y) );
           double tagBs1_dis = Math.sqrt((point1.x - tagPos[0]) * (point1.x - tagPos[0]) + (point1.y - tagPos[1]) * (point1.y - tagPos[1]));
           double tagBs2_dis = Math.sqrt((point2.x - tagPos[0]) * (point2.x - tagPos[0]) + (point2.y - tagPos[1]) * (point2.y - tagPos[1]));
           double vertical = getVerticalDis(tagBs1_dis, tagBs2_dis, twoBs_dis);
           list.add(vertical);
        }
      //求和
      for(int i = 0; i < list.size(); ++i)
      {
          sum += (list.get(i));
      }
      //求平均数
      double average = sum / list.size();
      DecimalFormat df = new DecimalFormat(".000");
    //  System.out.println("总和==="+df.format(sum));
    //  System.out.println("平均数==="+df.format(average));
      //求总体标准偏差
      double dsum=0;
      for(int i = 0; i < list.size(); ++i)
      {
          double s=(list.get(i)) - average;
          dsum += Math.pow(s,2);
      }
      double dStdDev = Math.sqrt(dsum / list.size());
    //  System.out.println("标准偏差值==="+df.format(dStdDev));
       return sum;

    }
    public static double[][] location1D(Double[] disC, Double BSpos[][]) {

        double d1 = disC[0];
        double d2 = disC[1];
        Double[] pos1=BSpos[0];
        Double[] pos2=BSpos[1];
        double a00=-2*(pos1[0]-pos2[0]);
        double a01=-2*(pos1[1]-pos2[1]);
        double a10=pos1[1]-pos2[1];
        double a11=pos2[0]-pos1[0];


        double b0=d1*d1-d2*d2-pos1[0]*pos1[0]+pos2[0]*pos2[0]-pos1[1]*pos1[1]+pos2[1]*pos2[1];
        double b1=pos2[0]*pos1[1]-pos1[0]*pos2[1];

        double[][] a={{a00,a01},{a10,a11}};
        double[][] b={{b0},{b1}};
        System.out.println("a矩阵");
       // print(a);
        System.out.println("b矩阵");
       // print(b);
        mrinv(a,2);
        System.out.println("a的逆矩阵");
        //print(a);
        double[][] res = matrix(a, b);


        return  res;

    }


    ////////////////////////////////////////////////////////////////////////
    //函数：Mrinv
    //功能：求矩阵的逆
    //参数：n---整数，矩阵的阶数
    //a---Double型n*n二维数组，开始时为原矩阵，返回时为逆矩阵
    ////////////////////////////////////////////////////////////////////////
    public static void mrinv(double[][] a, int n) {
        int i, j, row, col, k;
        double max, temp;
        int[] p = new int[n];
        double[][] b = new double[n][n];
        for (i = 0; i < n; i++) {
            p[i] = i;
            b[i][i] = 1;
        }

        for (k = 0; k < n; k++) {
            // 找主元
            max = 0;
            row = col = i;
            for (i = k; i < n; i++)
                for (j = k; j < n; j++) {
                    temp = Math.abs(b[i][j]);
                    if (max < temp) {
                        max = temp;
                        row = i;
                        col = j;
                    }
                }
            // 交换行列，将主元调整到 k 行 k 列上
            if (row != k) {
                for (j = 0; j < n; j++) {
                    temp = a[row][j];
                    a[row][j] = a[k][j];
                    a[k][j] = temp;
                    temp = b[row][j];
                    b[row][j] = b[k][j];
                    b[k][j] = temp;
                }
                i = p[row];
                p[row] = p[k];
                p[k] = i;
            }
            if (col != k) {
                for (i = 0; i < n; i++) {
                    temp = a[i][col];
                    a[i][col] = a[i][k];
                    a[i][k] = temp;
                }
            }
            // 处理
            for (j = k + 1; j < n; j++)
                a[k][j] /= a[k][k];
            for (j = 0; j < n; j++)
                b[k][j] /= a[k][k];
            a[k][k] = 1;

            for (j = k + 1; j < n; j++) {
                for (i = 0; i < k; i++)
                    a[i][j] -= a[i][k] * a[k][j];
                for (i = k + 1; i < n; i++)
                    a[i][j] -= a[i][k] * a[k][j];
            }
            for (j = 0; j < n; j++) {
                for (i = 0; i < k; i++)
                    b[i][j] -= a[i][k] * b[k][j];
                for (i = k + 1; i < n; i++)
                    b[i][j] -= a[i][k] * b[k][j];
            }
            for (i = 0; i < k; i++)
                a[i][k] = 0;
            a[k][k] = 1;
        }
        // 恢复行列次序；
        for (j = 0; j < n; j++)
            for (i = 0; i < n; i++)
                a[p[i]][j] = b[i][j];
    }

    /**
     * 矩阵乘法
     * a点乘b，当矩阵a的列数x与矩阵b的行数y相等时可进行相乘
     * a乘b得到的新矩阵c，c的行数y等于a的行数，c的列数x等于b的列数
     * Created by Queena on 2017/8/19.
     */

    public static double[][] matrix(double a[][], double b[][]) {
        //当a的列数与矩阵b的行数不相等时，不能进行点乘，返回null
      /*  if (a[0].length != b.length)
            return null;*/
        //c矩阵的行数y，与列数x
        int y = a.length;
        int x = b[0].length;
        double c[][] = new double[y][x];
        for (int i = 0; i < y; i++)
            for (int j = 0; j < x; j++)
                //c矩阵的第i行第j列所对应的数值，等于a矩阵的第i行分别乘以b矩阵的第j列之和
                for (int k = 0; k < b.length; k++)

                    c[i][j] += a[i][k] * b[k][j];
        return c;



    }
    static void print(double[][] sss) {
        int row=sss.length;
        int colum=sss[0].length;
        for(int i=0;i<row;i++) {
            for(int j=0;j<colum;j++) {
                System.out.print("  "+sss[i][j]);
            }
            System.out.println("");
        }


    }



    //矩阵转置
    public static double[][] transpose(double [][] matrix){

        int row=matrix.length;
        int col=matrix[0].length;
        double[][] res=new double[col][row];
        for(int i=0;i<row;i++)
        {
            for(int j=0;j<col;j++)
            {
                res[j][i]=matrix[i][j];
            }
        }
        return res;
    }
    public static boolean isPtInPoly(double[] point, double[][] pts){
        int N = pts.length;
        boolean boundOrVertex = true; //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        int intersectCount = 0;//cross points count of x
        double precision = 2e-10; //浮点类型计算时候与0比较时候的容差
        double[] p1, p2;//neighbour bound vertices
        double[] p = point; //当前点

        p1 = pts[0];//left vertex
        for(int i = 1; i <= N; ++i){//check all rays
            if(p.equals(p1)){
                return boundOrVertex;//p is an vertex
            }

            p2 = pts[i % N];//right vertex
            if(p[0] < Math.min(p1[0], p2[0]) || p[0] > Math.max(p1[0], p2[0])){//ray is outside of our interests
                p1 = p2;
                continue;//next ray left point
            }

            if(p[0] > Math.min(p1[0], p2[0]) && p[0] < Math.max(p1[0], p2[0])){//ray is crossing over by the algorithm (common part of)
                if(p[1]<= Math.max(p1[1], p2[1])){//x is before of ray
                    if(p1[0] == p2[0] && p[1] >= Math.min(p1[1], p2[1])){//overlies on a horizontal ray
                        return boundOrVertex;
                    }

                    if(p1[1] == p2[1]){//ray is vertical
                        if(p1[1] == p[1]){//overlies on a vertical ray
                            return boundOrVertex;
                        }else{//before ray
                            ++intersectCount;
                        }
                    }else{//cross point on the left side
                        double xinters = (p[0] - p1[0]) * (p2[1] - p1[1]) / (p2[0] - p1[0]) + p1[1];//cross point of y
                        if(Math.abs(p[1] - xinters) < precision){//overlies on a ray
                            return boundOrVertex;
                        }

                        if(p[1] < xinters){//before ray
                            ++intersectCount;
                        }
                    }
                }
            }else{//special case when ray is crossing through the vertex
                if(p[0] == p2[0] && p[1] <= p2[1]){//p crossing over p2
                    double[] p3 = pts[(i+1) % N]; //next vertex
                    if(p[0] >= Math.min(p1[0], p3[0]) && p[0] <= Math.max(p1[0], p3[0])){//p.x lies between p1.x & p3.x
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



}

