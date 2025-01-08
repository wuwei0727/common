package com.tgy.rtls.data.algorithm;


public class M3D {

public static String data="";

    public static void main(String[] args) {
       System.out.println("开始：");
       
       double[] x={0,0,1,};
        double[] y={0,1,1,};
        double[][] same_linearray={{0,0,0},{0,1,0},{1,0,0}};
        for(int m=0;m<3;m++) {
            double[] x_array = {same_linearray[m%3][0],same_linearray[(m+1)%3][0],same_linearray[(m+2)%3][0]};
            double[] y_array = {same_linearray[m%3][1],same_linearray[(m+1)%3][1],same_linearray[(m+2)%3][1]};
            double angle = getAngleByThreeP(x_array, y_array);
            System.out.println(angle);
        }
       //double angle = getAngleByThreeP(x, y);
       
       
          Double[][] bspos= {
                  {2.13d,2.68d,2d},//00
                  {5.28d,2.65d,2d},//00
                  {2.07d,11.19d,2d},//04
                {5.22d,11.2d,2d},//03*/
          /*  {0d,0d,0d}*/};//05
        Double[] dis= {1.995d,
                2.51d,
                4.298d,
                6.8d
           /*     207.2d-155d,*/
           //     185.2d-155d};
         /*182.9d-155d*/};
/*        ArrayList list=new ArrayList();
        for(int i=0;i<dis.length;i++){
              DisSort disSort=new DisSort(bspos[i][0],bspos[i][1],bspos[i][2],"",dis[i]);
              list.add(disSort);
        }
        Collections.sort(list);

        double[][] a={{1d,1d,0d},
                {1d,1d,0d},
                {1d,0d,1d},
                {1d,0d,1d},
                {0d,1d,1d},
                {0d,1d,1d}
        };
        double[][] b={{153.727},
                {154.006},{153.284},{153.638},{153.592},{153.615}};*/
/*        double[][] a_t=new double[3][6];
        Transpose(a,6,3,a_t);//x转置运算
        //   System.out.println("X_T");
        //   print(X_T);
        //  System.out.println("X_T * X");
        double[][] X_TX=matrix(a_t,a);//矩阵相乘
        double[][] X_Tb=matrix(a_t,b);//矩阵相乘
        print(X_TX);
        print(X_Tb);
        Mrinv(X_TX,3);
       double[][]res= matrix(X_TX ,X_Tb) ;*/

        //sameLine(bspos);
/*
        double[] dos={0,0,0,0};
        double[][] bsposs= {{0f,0f,0},{200f,0f,0}};
        double[][] diss= {{19.9,201.1},
                {19.8,201.2},
                {19.7,201.3},
                {19.6,201.4},
                {19.5,201.5}
        };
        Double[] bs1={0d,0d,0d};
        Double[] bs2={1d,2d,4d};
        Double[] bs3={2d,3d,6d};
        ArrayList<Double[]> list=new ArrayList();
        list.add(bs1);
        list.add(bs2);
        list.add(bs3);
        Double[][]  sdas=list.toArray(new Double[0][0]);
*/


      double[] res1= location_Minum(bspos,dis);
      double[][] weight=new double[dis.length-1][dis.length-1];

          for(int i=0;i<dis.length-1;i++) {
              //double diss = Math.sqrt(Math.pow(bspos[i][0] - res1[0], 2) + Math.pow(bspos[i][1] - res1[1], 2));
              weight[i][i] =Math.pow(Math.abs(dis[i]-dis[dis.length-1]),3);
          }
        double[] res2= location_WeightMinum(bspos,dis);
        System.out.println("dasda");
 //     double[] res2= Location_TOA.TOA_3BS(dis,bspos,1);
       // long[] bsname={100,101};
/*        for(int i=0;i<5;i++) {
            double[] res4 = Location_TOA.location2BS2D(diss[i], bsposs, 0, 0);
            System.out.println("定位结果" + res4[0] + "," + res4[1]);
        }*/
        //double[] res3= Location_TOA.locationChan2D(dos,bspos,4,1);


      /* Location_Res finalres = location( bspos, dis);
       System.out.println("has res:"+finalres.hasRes+"has mutiple res:"+finalres.mutipleRes+"::"+finalres.res[0][0]+":::"+finalres.res[0][1]);
*/
    	//double ss=computeAccuracy(-51,-58);
 /*   	double x=20,y=10;
    	double angle=90;*/
    /*	double angle1=Math.toRadians(90-angle);
    	double x0=x* Math.cos(angle1)-y*Math.sin(angle1);
        double y0=x* Math.sin(angle1)+y*Math.cos(angle1);
    	
    	
    	*/
    	 System.out.println("computeAccuracy"+Math.sin(Math.toRadians(90))+":::");
    	//xyChangeAxis(x,y,90);
       /* for(int i=0;i<30;i++) {
           double diss= MainActivity.calcDistByRSSI(-60-i, -60);
            System.out.println("rssi:"+(-60-i)+"距离"+diss);
        }*/
    /*	 边界点:6.7554244585335255,74.58957733539864边界点:6.606133386492729,55.77890240587294边界点:29.671603836119175,55.62961133522913边界点:29.746249372139573,74.51493180030957
    	 旧:x13.574206912890077--y:55.73380160657689
    	 新:x13.797992830878954--y:56.63776172688095*/
    	 
    	 
    	 
    	/*PointDouble a=new  PointDouble(5,0);
    	PointDouble b=new PointDouble(5,5);
		PointDouble c=new PointDouble(1,5);
		PointDouble d=new PointDouble(1,6);
		PointDouble e=new  PointDouble(5,6);
    	PointDouble f=new PointDouble(5,11);
		PointDouble g=new PointDouble(1,11);
		PointDouble h=new PointDouble(1,12);
		PointDouble i=new  PointDouble(10,12);
    	PointDouble j=new PointDouble(10,11);
		PointDouble k=new PointDouble(6,11);
		PointDouble l=new PointDouble(6,6);		
    	PointDouble m=new PointDouble(10,6);
		PointDouble n=new PointDouble(10,5);
		PointDouble o=new PointDouble(6,5);
		PointDouble p=new PointDouble(6,0);
		
*//*		PointDouble e=new PointDouble(4,10);*//*
		List<PointDouble> list=new ArrayList<>();
		list.add(a);
		list.add(b);
		list.add(c);
		list.add(d);
		list.add(e);
		list.add(f);
		list.add(g);
		list.add(h);
		list.add(i);
		list.add(j);
		list.add(k);
		list.add(l);
		list.add(m);
		list.add(n);
		list.add(o);
		list.add(p);
	
		
		*//*list.add(e);*//*
		
		
		
		PointDouble ff=new PointDouble(4.839548598974943+12634080.343077801,-9.76927639869973+2653848.860516095);
		PointDouble fff=new PointDouble(5.331433175131679+12634080.343077801,-9.44754939526319+2653848.860516095);
	*//*	旧:x13.501238516346202--y:55.44109485914231
		新:x13.650268174723472--y:56.038915125386005*//*
		
	*//*PointDouble f=new PointDouble(13.501238516346202,55.44109485914231);
		PointDouble ff=new PointDouble(13.650268174723472,56.038915125386005);*//*
	

			
			
			PointDouble aa=new  PointDouble(-0.603077,-3.8305);
			PointDouble bb=new  PointDouble(5.316922,-3.8605);
			
			PointDouble mm=new  PointDouble(5.4069,-9.8905);
					
			PointDouble cc=new  PointDouble(-0.563077,-9.7005);
			PointDouble  his =new PointDouble(5.191701782867312,-9.883666533976793);
			List<PointDouble> pts=new ArrayList<>();
			pts.add(aa);
			pts.add(bb);
			pts.add(mm);
			pts.add(cc);
			boolean in= HasCrossPoint.isPtInPoly(his, pts);
			ArrayList<PointDouble>  doubles =new ArrayList<>();
			doubles.add(new PointDouble(0.6030778009444475,-3.8305160952731967));
			doubles.add(new PointDouble(5.316922198981047,-3.8605160950683057));
			doubles.add(new PointDouble(5.406922198832035,-9.890516094863415));
			doubles.add(new PointDouble(-0.5630778018385172,-9.700516094919294));
			PointDouble  hiss =new PointDouble(5.191701782867312,-9.883666533976793);
			boolean in1=HasCrossPoint.isPtInPoly(hiss,doubles);
			
			 PointDouble res = HasCrossPoint.getCrossPoint(ff, fff, pts);
			 System.out.println("in :"+in1);
				
				if(res!=null) {
					System.out.println("交点:"+res.x+":"+res.y+"相交的边为");
					
					System.out.println(res.start.x+":"+res.start.y);
					System.out.println(res.end.x+":"+res.end.y);
				}
			
		*//*	PointDouble out=new  PointDouble(  12634090.5682815,2653850.202269219);
			PointDouble in=new   PointDouble(  12634090.927605005,2653850.6805635807);
		*//*
			
		
			//PointDouble line1=HasCrossPoint.getCrossPoint(out,in, pts);
		
		//	System.out.println("trend:"+line1.x+"::"+line1.y);
		//	System.out.println("trend:"+ress2.x+"::"+ress2.y);
			
		
			PointDouble f1=new  PointDouble( 4.543497925624251,-4.1806699126027524);
			
			PointDouble ff1=new  PointDouble( 3.681561851873994,-4.21547719184309);

			
			
			*/
		
		//	line.start=new PointDouble();
		//	line.start=new PointDouble();

		
			//boolean res1 = HasCrossPoint.isPtInPoly(currentPosition, pts);
			
//			res=HasCrossPoint.calculateForbidden(res, f1, ff1, pts);
		//	System.out.println("current in forbidden area"+res.x+":"+res.y);
		
			
		/*	int[] glare_data = {11,10,8,10,12};
			int t = VectorLine.ordered(glare_data,glare_data.length);
			System.out.println("trend:"+t);*/
/*			#define LIGHT_DATA_COUNT_MAX	 7
			typedef enum
			{
				LIGHT_DATA_DECREASE=-1,
				LIGHT_DATA_DISORDER=0,
				LIGHT_DATA_INCREASE,
				LIGHT_DATA_STABLE,
				LIGHT_DATA_MAX

			————————————————
			版权声明：本文为CSDN博主「深深生生」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
			原文链接：https://blog.csdn.net/qq_41960196/article/details/81303314		
*/    			
    		//}
			//gdal.AllRegister();
		/*	String ss="00000001";
			String.format("%16s",ss);
			System.out.println(String.format("%16s",ss));
			System.out.println(String.format("%09d", Long.parseLong("00123")));*/
			//System.load("C:\\Program Files\\Java\\jdk1.8.0_144\\bin\\gdalalljni.dll");
	
		
			
			
			
		  
    }
    public static void  curString(byte[] data1) {
        {
            byte[] inByte = new byte[1024*2];
            try {
                int inLength =data1.length;
                if (inLength > 0) {

                    String data2=new String(data1);
                    M3D.data= M3D.data.concat(data2);
                    //  System.out.println("OTG receive::" + M.data+"---"+data2);
                    int k=0;
                    while (k!=-1){
                        int l=  data.indexOf("/n",0);
                        if(l!=-1){
                            String dat=  data.substring(0,l);
                            data=data.substring(l+2,data.length());
                            {
                                System.out.println("OTG receive::" + dat);

                            }
                        }


                        k=l;
                        System.out.println("k" + k);



                    }


                } else {
                    //break;
                }
            }catch (Exception e){

            }
        }
    }

    static double[] xyChangeAxis(double x,double y,double angle){

        double angle1=Math.toRadians(angle);
        double x0=x* Math.cos(angle1)+y*Math.sin(angle1);
        double y0=-x* Math.sin(angle1)+y*Math.cos(angle1);
        double[] finalres={x0,y0};
        System.out.println("computeAccuracy"+x0+":::"+y0);
        return finalres;
    }

    public static double computeAccuracy(int rssi,int mesurepower)
    {
        if (rssi == 0)
            return -1.0D;
        double d1 =rssi;
        Double.isNaN(d1);
        double d2 = mesurepower;
        Double.isNaN(d2);
        d1 = d1 * 1.0D / d2;
        d2 = Math.pow(Math.abs(rssi), 3.0D) % 10.0D / 150.0D + 0.96D;
        if (d1 <= 1.0D)
            return Math.pow(d1, 9.98D) * d2;
        return (Math.pow(d1, 7.71D) * 0.89978D + 0.103D) * d2;

    }

 /* public  static Location_Res location( double[][] bspos,double[] dis){
        int len=bspos.length;
        double[][] newBspos=new double[len][3];
        double[] newDis=new double[len];
        for(int i=0;i<len;i++){
            newBspos[len-1-i][0]= bspos[i][0];
                    newBspos[len-1-i][1]=bspos[i][1];
                            newBspos[len-1-i][2]=bspos[i][2];
                            newDis[len-1-i]=dis[i];
        }
        bspos=newBspos;
        dis=newDis;

        double[][] reverse=new double[bspos.length][3];
        for(int i=0;i<bspos.length;i++) {
            reverse[i][0]=bspos[i][1];
            reverse[i][1]=bspos[i][0];
        }
        Location_Res finalres=new Location_Res();

 //double[] res=location_Minum(bspos,dis);
  double[] res=location_WeightMinum(bspos,dis);

        if(res!=null&&!Double.isNaN(res[0])&&!Double.isNaN(res[1])){
            //finalres=res;
            finalres.hasRes=true;
            finalres.mutipleRes=false;
            finalres.resType="least square";
            finalres.res[0][0]=res[0];
            finalres.res[0][1]=res[1];
            System.out.println("最小二乘计算结果");
            MainActivity.appendDebuginf("最小二乘结果");
            return finalres;
        }else {
		*//*double[] reverseres=location_Minum(reverse,dis);
		if(reverseres!=null&&!Double.isNaN(reverseres[0])&&!Double.isNaN(reverseres[1])){
    		double[] finalress= {reverseres[1],reverseres[0]};
    		finalres.hasRes=true;
    		finalres.mutipleRes=false;
    		finalres.res[0][0]=res[0];
    		finalres.res[0][1]=res[1];
		}else *//*
            boolean sameline= sameLine(bspos);
            if(sameline) {

                double[] originarray=Arrays.copyOf(dis, dis.length);
                Arrays.sort(dis);
                int[] index=new int[dis.length];
                for(int i=0;i<dis.length;i++) {
                    for(int j=0;j<dis.length;j++) {
                        if(originarray[j]==dis[i])
                        {
                            index[i]=j;
                            originarray[j]=-10;
                        }

                    }

                }
                MainActivity.appendDebuginf("共线计算");
                System.out.println("index[0]"+index[0]+"index[1]"+index[1]);


                double[][] neartwbs= {bspos[index[0]],bspos[index[1]]};

                double[][] twobs=FreeWay_RLS.location1D(dis[0], dis[1], neartwbs, 1.5);
                if(twobs!=null) {
                    finalres.hasRes=true;
                    finalres.mutipleRes=true;
                    finalres.res[0][0]=twobs[0][0];
                    finalres.res[0][1]=twobs[0][1];
                    finalres.res[1][0]=twobs[1][0];
                    finalres.res[1][1]=twobs[1][1];
                    finalres.resType="same line mutiple res";
                    System.out.println("same line");
                    return finalres;
                }
            }

            {
                finalres.hasRes=true;
                finalres.mutipleRes=false;
                double xsum=0,ysum=0;
                for(int i=0;i<bspos.length;i++) {
                    xsum=xsum+bspos[i][0];
                    ysum=ysum+bspos[i][1];
                }

                finalres.res[0][0]=xsum/bspos.length;
                finalres.res[0][1]=ysum/bspos.length;
                finalres.hasRes=true;
                finalres.mutipleRes=false;
                finalres.resType="barycenter";
                System.out.println("barycenter"+bspos.length);
                MainActivity.appendDebuginf("位置中心");
                return finalres;


            }



        }


    }
*/
    public static boolean sameLine(double[][] bspos) {
        // TODO Auto-generated method stub
        double range=0.05;
        int len=bspos.length;
        if(len==2)
            return true;
        else {
            double sum=0;
            double[] gradient=new double[len-1];
            for(int i=1;i<len;i++) {
                double ss=(bspos[0][1]-bspos[i][1])==0?0.00001:(bspos[0][1]-bspos[i][1]);
                gradient[i-1]=(bspos[0][0]-bspos[i][0])/ss;
                sum=sum+gradient[i-1];
            }
            double average=sum/(len-1);
            int count=0;
            for(int i=0;i<len-1;i++) {
                double sub = Math.abs(gradient[i] - average);
                if(sub>range) {
                    count++;
                }
            }
         //   System.out.println("count:"+count);
            if(count>2)
                return false;
            else
                return true;



        }
    }

    /*
     * 最小二乘求解
     */

    public static double[] location_Minum( Double[][] bspos,Double[] dis) {
        /*  if(bspos.length<3)
              return null;*/
        int bssize=bspos.length;
      /*  double personheight=2;
        for(int i=0;i<dis.length;i++) {
            dis[i]=Math.sqrt(Math.pow(dis[i], 2)-Math.pow(bspos[i][2]-personheight,2));
        }*/
        double[][] x=new double[bssize-1][3];
        double[][] y=new double[bssize-1][1];
/*        double[][] z=new double[bssize-1][3];
        double[][] b=new double[bssize-1][1];*/
        for(int i=0;i<bssize-1;i++) {
            x[i][0]=bspos[bssize-1][0]-bspos[i][0];
            x[i][1]=bspos[bssize-1][1]-bspos[i][1];
            x[i][2]=bspos[bssize-1][2]-bspos[i][2];
        }

        for(int i=0;i<bssize-1;i++) {
            double A= dis[i]*dis[i]-dis[bssize-1]*dis[bssize-1];
            double B=bspos[bssize-1][0]*bspos[bssize-1][0]+bspos[bssize-1][1]*bspos[bssize-1][1]+bspos[bssize-1][2]*bspos[bssize-1][2];
            double C=bspos[i][0]*bspos[i][0]+bspos[i][1]*bspos[i][1]+bspos[i][2]*bspos[i][2];
            double res=(A+B-C)/2;
              y[i][0]=res;
           /*   y[i][1]=0;
              y[i][2]=0;*/

        }
        int line=x.length;
        int colum=3;
        double[][] X_T=new double[colum][line];

     //   System.out.println("X");
       // print(x);

      //  System.out.println("Y");
     //   print(y);

        Transpose(x,line,3,X_T);//x转置运算
     //   System.out.println("X_T");
     //   print(X_T);
      //  System.out.println("X_T * X");
        double[][] X_TX=matrix(X_T,x);//矩阵相乘
    //    print(X_TX);
     //   System.out.println("X_T * X 求 NI");
        int zhi=1;
        if(X_TX[0][0]!=0&&X_TX[1][1]!=0)
            zhi=2;
        if(X_TX[0][0]!=0&&X_TX[1][1]!=0&&X_TX[2][2]!=0)
            zhi=3;

        Mrinv(X_TX,zhi) ;//矩阵求逆
      //  print(X_TX);
        //  System.out.println("X_T * X *X_T*Y");
        double[][]res=matrix(matrix(X_TX,X_T),y);
   /*     for(int i=0;i<2;i++) {
            for(int j=0;j<2;j++) {
                System.out.print("res  "+res[i][j]);
            }
            System.out.println();
        }*/
   /*if(zhi==3&&res!=null){
       double[] finalres= {res[0][0],res[1][0],res[2][0]};
       return finalres;

   }*/
        if(zhi==2&&res!=null) {
            double[] finalres= {res[0][0],res[1][0],0d,2};
            int bs_len=dis.length;
            double sum_z=0;
            int k=0;
            for(int i=0;i<bs_len;i++){
                double z1=0;

                z1= Math.sqrt(dis[i] * dis[i] - (finalres[0] - bspos[i][0]) * (finalres[0] - bspos[i][0]) - (finalres[1] - bspos[i][1]) * (finalres[1] - bspos[i][1]));
                if(!Double.isNaN(z1)) {
                  sum_z=sum_z+z1;
                  k++;
                }
            }
            if(k>0) {
                finalres[2] = sum_z / k;
            }

            return finalres;

        }else {
/*    	   if(zhi==1) {
    	  if(res[0][0]!=0) {
       double yy=0,yyreverse=0;
       for(int i=0;i<bspos.length;i++) {
    	   double yr=(bspos[i][1]-Math.sqrt( dis[i]*dis[i]-(bspos[i][0]-res[0][0])*(bspos[i][0]-res[0][0])));
    	   double yrr=(bspos[i][1]+Math.sqrt( dis[i]*dis[i]-(bspos[i][0]-res[0][0])*(bspos[i][0]-res[0][0])));
    	   System.out.print("yy  "+yr+"yrr"+yrr);
    	   yy=yy+yr;
    	   yyreverse=yyreverse+yrr;
         }
         yy=yy/bspos.length;
         yyreverse=yyreverse/bspos.length;

       System.out.print("yy final  "+yy+"::yyreverse"+yyreverse);
       double[] finalres= {res[0][0],yy};
 	     return finalres;
    	  }
       }*/

       }
        return null;

    }

    /**
     * 判断三点组成的角度
     * @param pointx
     * @param pointy
     * @return
     */
    public static double getAngleByThreeP(double[] pointx, double[] pointy) {
        double a_b_x = pointx[0] - pointx[1];
        double a_b_y = pointy[0] - pointy[1];
        double c_b_x = pointx[2] - pointx[1];
        double c_b_y = pointy[2] - pointy[1];
        double ab_mul_cb = a_b_x * c_b_x + a_b_y * c_b_y;
        double dist_ab = Math.sqrt(a_b_x * a_b_x + a_b_y * a_b_y);
        double dist_cd = Math.sqrt(c_b_x * c_b_x + c_b_y * c_b_y);
        double cosValue = ab_mul_cb / (dist_ab * dist_cd);
        return Math.acos(cosValue)*180/3.1415;
    }

  

    /*
     * 加权最小二乘求解
     */

    public static double[] location_WeightMinum( Double[][] bspos,Double[] dis) {

        int bssize=bspos.length;
        double personheight=1.5;
       /* for(int i=0;i<dis.length;i++) {
            dis[i]=Math.sqrt(Math.pow(dis[i], 2)-Math.pow(bspos[i][2]-personheight,2));
        }*/
        double[][] x=new double[bssize-1][2];
        double[][] y=new double[bssize-1][2];
        for(int i=0;i<bssize-1;i++) {
            x[i][0]=bspos[bssize-1][0]-bspos[i][0];
            x[i][1]=bspos[bssize-1][1]-bspos[i][1];

        }

        for(int i=0;i<bssize-1;i++) {
            double A= dis[i]*dis[i]-dis[bssize-1]*dis[bssize-1];
            double B=bspos[bssize-1][0]*bspos[bssize-1][0]+bspos[bssize-1][1]*bspos[bssize-1][1];
            double C=bspos[i][0]*bspos[i][0]+bspos[i][1]*bspos[i][1];
            double res=(A+B-C)/2;
            y[i][0]=res;
            y[i][1]=0;

        }




        int line=x.length;
      // double[][] weight=new double[line][line];
        double[][] weight=   calculateWeight(dis);

        int colum=2;
        double[][] X_T=new double[colum][line];

        System.out.println("X");
        print(x);

        System.out.println("Y");
        print(y);

        Transpose(x,line,2,X_T);//x转置运算
        System.out.println("X_T");
        print(X_T);
        System.out.println("X_T * W");

        double[][] X_TW=matrix(X_T,weight);//矩阵相乘
        System.out.println("X_T * W*X");
        double[][] X_TWX=matrix(X_TW,x);//矩阵相乘
        print(X_TWX);

        System.out.println("X_T * X 求 NI");
        int zhi=1;
        if(X_TWX[0][0]!=0&&X_TWX[1][1]!=0)
            zhi=2;
        Mrinv(X_TWX,zhi) ;//矩阵求逆
        print(X_TWX);
        //  System.out.println("X_T * W*X *X_T*W*Y");
        double[][]res=matrix(matrix(matrix(X_TWX,X_T),weight),y);
        int len=dis.length;

            for (int i=0;i<len;i++){
                System.out.println("bspos"+bspos[i][0]+"::"+bspos[i][1]+"::"+bspos[i][2]+":dis:"+dis[i]);
            }
        for(int i=0;i<2;i++) {
            for(int j=0;j<2;j++) {
                System.out.print("res  "+res[i][j]);
            }
            System.out.println();
        }
        if(zhi==2&&res!=null&&!Double.isNaN(res[0][0])) {
            double[] finalres= {res[0][0],res[1][0],0d,2};
            int bs_len=dis.length;
            double sum_z=0;
            int k=0;
            for(int i=0;i<bs_len;i++){
                double z1=0;

                z1= Math.sqrt(dis[i] * dis[i] - (finalres[0] - bspos[i][0]) * (finalres[0] - bspos[i][0]) - (finalres[1] - bspos[i][1]) * (finalres[1] - bspos[i][1]));
                if(!Double.isNaN(z1)) {
                    sum_z=sum_z+z1;
                    k++;
                }
            }
            if(k>0) {
                finalres[2] = sum_z / k;
            }
          //  int len=dis.length;
      /*      if(finalres[0]==0&&finalres[1]==0){
                for (int i=0;i<len;i++){
                    System.out.println("bspos"+bspos[i][0]+"::"+bspos[i][1]+"::"+bspos[i][2]+":dis:"+dis[i]);
                }

            }*/

            return finalres;

        }else {/*
    	   if(zhi==1) {
    	  if(res[0][0]!=0) {
       double yy=0,yyreverse=0;
       for(int i=0;i<bspos.length;i++) {
    	   double yr=(bspos[i][1]-Math.sqrt( dis[i]*dis[i]-(bspos[i][0]-res[0][0])*(bspos[i][0]-res[0][0])));
    	   double yrr=(bspos[i][1]+Math.sqrt( dis[i]*dis[i]-(bspos[i][0]-res[0][0])*(bspos[i][0]-res[0][0])));
    	   System.out.print("yy  "+yr+"yrr"+yrr);
    	   yy=yy+yr;
    	   yyreverse=yyreverse+yrr;
         }
         yy=yy/bspos.length;
         yyreverse=yyreverse/bspos.length;

       System.out.print("yy final  "+yy+"::yyreverse"+yyreverse);
       double[] finalres= {res[0][0],yy};
 	     return finalres;
    	  }
       }

       */}
        return null;

    }

    /*
     * 计算加权矩阵，距离越小权值越大
     */
    public static double[][] calculateWeight(Double[] dis){

   /*     int len=dis.length;
        double sum=0;
        for(int k=0;k<len-1;k++) {
            sum=sum+(dis[k]);
        }
        double[][] weight=new double[len-1][len-1];
        for(int i=0;i<len-1;i++) {
            weight[i][i]=Math.pow(sum/dis[i],10);
            System.out.println("weight"+weight[i][i]);
         //   weight[i][i]=1/dis[i];
          //  weight[i][i]=1;
        }*/

      /*  System.out.println("权重逆矩阵");
        Mrinv(weight,weight.length);

        print(weight);*/
      int size=dis.length-1;
        double[][] weight=new double[size][size];

        for(int i=0;i<size;i++) {
            //double diss = Math.sqrt(Math.pow(bspos[i][0] - res1[0], 2) + Math.pow(bspos[i][1] - res1[1], 2));
           // weight[i][i] =Math.pow(Math.abs(dis[i]-dis[size]),2);
            weight[i][i] =1;
        }
       // Mrinv(weight,size) ;

        return weight;

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

    public static void Transpose(double [][]Matrix,int Line,int List,double[][]MatrixC){
        for(int i=0;i<Line;i++)
        {
            for(int j=0;j<List;j++)
            {
                MatrixC[j][i]=Matrix[i][j];
            }
        }
    }



    ////////////////////////////////////////////////////////////////////////
    //函数：Mrinv
    //功能：求矩阵的逆
    //参数：n---整数，矩阵的阶数
    //a---Double型n*n二维数组，开始时为原矩阵，返回时为逆矩阵
    ////////////////////////////////////////////////////////////////////////
    public static void Mrinv(double[][] a, int n) {
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






}

