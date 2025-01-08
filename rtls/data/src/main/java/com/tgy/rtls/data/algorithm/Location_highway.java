package com.tgy.rtls.data.algorithm;

import java.math.BigDecimal;

public class Location_highway {


	public double pos[];
	int size=1;
	public double BSpos[][]; // = {{17, 0, 1.5},{0, 10, 1.5},{0, 10, 1.5},{ 17, 10, 1.5},{34, 10, 1.5},{34, 0, 1.5}};
	double z_assump=0.3;
	public double[][] res=new double[1][8]; 
	public double[] time_coef;
   public int[] state;
	public Location_highway() {
		// TODO Auto-generated constructor stub
	}
	
	//mbs0.0bs127.6bs20.0bs327.6bs413.8bs5100.0x:-5267602879315276y-30065400018645680z1187301824
	
	
double[] location(long[] bsname,double[][] bspos,double[] timestamp,int[] bsstate,double[] coef)

{


	
	/*double bspos[][]={{mbs.x,mbs.y,mbs.z},
			          {bs1.x,bs1.y,bs1.z},
			          {bs2.x,bs2.y,bs2.z},
			          {bs3.x,bs3.y,bs3.z}
			       
			          };*/
	BSpos=bspos;
	res[0]=timestamp;
	state=bsstate;
	time_coef=coef;
		
	getLocation();
		
	return pos;	
	
}
public void getLocation()
{

	double bs_antennadelay =77.81;
	
	int i,j;

	for(i = 0; i < size; i++)
	{
		for(j = 0; j < 4; j++)
		{
			if(res[i][2 * j] < 0.1)
				state[j] = 0;
			else
				state[j] = 1;
		}
		if(true)  //门限设置
		{
			double corr[] = new double[3];  //车内距离差补偿
			//pos = location(res,i, BSpos, bs_antennadelay, time_coef, state, z_assump,corr);
			//System.out.println("x:"+pos[0]+"y"+pos[1]+"z"+pos[2]);
			//if(pos != null)
			//	System.out.println( res[i][12] + " " + "location: x = " + pos[0] + ", y = " + pos[1] + ", res = " + pos[2] + " BS State: " + state[0] + " "+ state[1] + " "+ state[2] + " "+ state[3] + " "+ state[4] + " "+ state[5]);				
		}
		
	}
}
public  static double getdiff(double x2, double x1)
{
	double diff;
	if(x2 < 1 || x1 < 1 || x2 > 1099511627775d || x1 > 1099511627775d)
		return 0;			
	else
	{
		if(x2 < x1)
		{
			x2 += 1099511627775d;

		}
		diff = x2 - x1;
		return diff;
	}

}

	public  static double getBsdiff(double x2, double x1)
	{
		double diff;
			diff = x2 - x1;
			return diff;

	}


static double[] locationChan2D(double R[], double BSpos[][], int BSnum, double z_assump)
{
	//二维定位，基站没有高度差
	double xydet[] = {0, 0, 0};

	if(BSnum > 3)
	{
		double[] K = new double[BSnum];
		int i, j;
		for(i = 0; i < BSnum; i++)
		{
			K[i] = Math.pow(BSpos[i][0],2) + Math.pow(BSpos[i][1],2);
		}

		double[] m = new double[3];
		for(i = 0; i < 2; i++)
		{
			m[i] = BSpos[0][i];
		}
		
		
		
		
		double[][] A = new double[BSnum-1][2];
		double[] RK = new double[BSnum-1];
		for(i = 0; i < BSnum - 1; i++)
		{
			A[i][0] = (BSpos[i + 1][0] - BSpos[0][0]);
			A[i][1] = (BSpos[i + 1][1] - BSpos[0][1]);
			//A[i][2] = (BSpos[i + 1][2] - BSpos[0][2]);
			
		}
		double[][] pinvA = pinvMatrix(A,  BSnum - 1, 2);
		
		for(i = 0; i < BSnum - 1; i++)
		{
			//RK[i]    = 0.5 * (Math.pow(R[i],2) - K[i + 1] + K[0]) + (BSpos[i + 1][2] - BSpos[0][2])*z_assump + R[i]*R0;
			RK[i]    = Math.pow(R[i],2) - K[i + 1] + K[0];
		}
		
		
		double[] e = new double[3];
		double[] f = new double[3];

		for(i = 0; i < 2; i++)
		{
			for(j = 0; j < BSnum-1; j++)
			{
				e[i] -= pinvA[i][j] * R[j];
				f[i] -= 0.5 * pinvA[i][j] * RK[j]; 
			}
		}
		
		double aa = 1 - e[0]*e[0] - e[1]*e[1] - e[2]*e[2];
		double bb = 2 * (m[0]*e[0] + m[1]*e[1] + m[2]*e[2] - (f[0]*e[0] + f[1]*e[1] + f[2]*e[2]));
		double cc = 2*(m[0]*f[0] + m[1]*f[1] + m[2]*f[2]) - (f[0]*f[0] + f[1]*f[1] + f[2]*f[2]) - K[0];
		
		xydet[2] = bb*bb - 4*aa*cc;

		if(xydet[2] >= 0)
		{
			
			double R0_max = (- bb + Math.sqrt(xydet[2])) / (2 * aa);
			double R0_min = (- bb - Math.sqrt(xydet[2])) / (2 * aa);
			double pinvA2[][] = pinvMatrix(A, BSnum-1, 2);
			double x_max, x_min, y_max, y_min;
			double matB_max[][] = new double[BSnum-1][1];
			double matB_min[][] = new double[BSnum-1][1];
			for(i = 0; i < BSnum - 1; i++)
			{
				matB_max[i][0] = R[i]*R0_max + 0.5 * RK[i];// + (BSpos[i + 1][2] - BSpos[0][2])*z_assump ;
				matB_min[i][0] = R[i]*R0_min + 0.5 * RK[i];// + (BSpos[i + 1][2] - BSpos[0][2])*z_assump ;
			}
			
			double ans[][] = new double[2][1];
			ans  = multMatrix(pinvA2, matB_max, 2, BSnum - 1, 1);
			x_max = -ans[0][0];
			y_max = -ans[1][0];
			
			ans  = multMatrix(pinvA2, matB_min, 2, BSnum - 1, 1);
			x_min = -ans[0][0];
			y_min = -ans[1][0];
			
			if(R0_min <= 0)
			{
				xydet[0] = x_max;					
				xydet[1] = y_max;
			}
			else
			{
				if(x_max > 0 && y_max > 0)
				{
					xydet[0] = x_max;					
					xydet[1] = y_max;
				}
				else if(x_min > 0 && y_min > 0)
				{
					xydet[0] = x_min;					
					xydet[1] = y_min;	
				}
				else
				{
					return null;
				}
			}
				
				//此处为最终结果
			System.out.println("location: x = " + xydet[0] + ", y = " + xydet[1] + ", z = " + xydet[2]);
				return xydet;
		}
		else
		{
			System.out.println("结果为复数！"+xydet[2]);
			return null;
		}
	}
	else
	{
		System.out.println("基站数量不足");
		return null;
	}
}

public static double[] location(BigDecimal timeStamp[][], String[] bsNames, int time, double BSpos[][], double anneDelay, double time_coef[], int state[], double z_assump, double corr[])
{
	double xyz[] = new double[3];

	int i,j;
    int bsSize=BSpos.length;
	double dt[] = new double[bsSize];
	double R[] = new double[bsSize];
	double d[] = new double[bsSize];

	for(i = 0; i < bsSize; i++)
	{
		dt[i] = Range.getdiff(timeStamp[i][1] , timeStamp[i][0]);
		//System.out.print("diff tic is:"+dt[i]);
	}

	for(i = 0; i < bsSize-1; i++)
	{
		d[i] = Math.sqrt(Math.pow(BSpos[i+1][0] - BSpos[0][0], 2) + Math.pow(BSpos[i+1][1] - BSpos[0][1], 2) + Math.pow(BSpos[i+1][2] - BSpos[0][2], 2));

		double timesd=dt[i + 1] * time_coef[i + 1]  - dt[0] * time_coef[0];
		R[i] = timesd + 2 * anneDelay + d[i] + corr[i];
	}

	double[][] BS = resortBS(BSpos, state);
	int BSnum = 0;

	for(i = 0; i < bsSize; i++)
	{
		if(state[i] == 1)
			BSnum++;
	}
	if(BSnum==2)
	{
		//一维定位
		xyz[0]=R[0];
		System.out.println("diff is:"+R[0]);
		return xyz;
	}
	else if(BSnum >=3)
	{
		double RT[] = new double[bsSize - 1];
		j = 0;
		for(i = 1; i < bsSize; i++)
		{
			if(state[i] == 1)
			{
				RT[j] = R[i - 1];
				j++;
				}
		}
		if(BSnum==3)
		xyz = locationChan2D(RT, BS, BSnum, z_assump);
		else if(BSnum>3){
			xyz=Location_algo.locationRLS(BSnum, RT, BS, z_assump, dt);
		}
		return xyz;
	}else{
		return null;
	}
	
}

public   static double[][] resortBS(double BSpos[][], int state[])
{
	double[][] BS = new double[state.length][3];
	int i,j,k;
	k = 0;
	for(i = 0; i < state.length; i++)
	{
		if(state[i] == 1)
		{
			for(j = 0; j < 3; j++)
			{
				BS[k][j] = BSpos[i][j];
			}
			k++;
		}
	}
	return BS;
}
static double[] locationChan(double R[], double BSpos[][], int BSnum, double z_assump)
{
	double xydet[] = {0, 0, 0};

	for(int i = 0; i < BSnum - 1; i++)
	{
		System.out.print(R[i] +"\t");
	}
	System.out.print("\n");
	

	if(BSnum > 3)
	{
		double[] K = new double[BSnum];
		int i, j;
		for(i = 0; i < BSnum; i++)
		{
			K[i] = Math.pow(BSpos[i][0],2) + Math.pow(BSpos[i][1],2) + Math.pow(BSpos[i][2],2);
		}

		double[] m = new double[3];
		for(i = 0; i < 3; i++)
		{
			m[i] = BSpos[0][i];
		}
		
		
		
		
		double[][] A = new double[BSnum-1][3];
		double[] b = new double[BSnum-1];
		double[] RK = new double[BSnum-1];
		for(i = 0; i < BSnum - 1; i++)
		{
			A[i][0] = (BSpos[i + 1][0] - BSpos[0][0]);
			A[i][1] = (BSpos[i + 1][1] - BSpos[0][1]);
			A[i][2] = (BSpos[i + 1][2] - BSpos[0][2]);
			
		}
		double[][] pinvA = pinvMatrix(A,  BSnum - 1, 3);
		
		for(i = 0; i < BSnum - 1; i++)
		{
			//RK[i]    = 0.5 * (Math.pow(R[i],2) - K[i + 1] + K[0]) + (BSpos[i + 1][2] - BSpos[0][2])*z_assump + R[i]*R0;
			RK[i]    = Math.pow(R[i],2) - K[i + 1] + K[0];
		}
		
		
		double[] e = new double[3];
		double[] f = new double[3];

		for(i = 0; i < 3; i++)
		{
			for(j = 0; j < 3; j++)
			{
				e[i] -= pinvA[i][j] * R[j];
				f[i] -= 0.5 * pinvA[i][j] * RK[j]; 
			}
		}
		
		double aa = 1 - e[0]*e[0] - e[1]*e[1] - e[2]*e[2];
		double bb = 2 * (m[0]*e[0] + m[1]*e[1] + m[2]*e[2] - (f[0]*e[0] + f[1]*e[1] + f[2]*e[2]));
		double cc = 2*(m[0]*f[0] + m[1]*f[1] + m[2]*f[2]) - (f[0]*f[0] + f[1]*f[1] + f[2]*f[2]) - K[0];
		
		xydet[2] = bb*bb - 4*aa*cc;

		if(xydet[2] >= 0)
		{
			
			double R0_max = (- bb + Math.sqrt(xydet[2])) / (2 * aa);
			double R0_min = (- bb - Math.sqrt(xydet[2])) / (2 * aa);
			double pinvA2[][] = pinvMatrix(A, BSnum-1, 2);
			double x_max, x_min, y_max, y_min;
			double matB_max[][] = new double[BSnum-1][1];
			double matB_min[][] = new double[BSnum-1][1];
			for(i = 0; i < BSnum - 1; i++)
			{
				matB_max[i][0] = R[i]*R0_max + 0.5 * RK[i] + (BSpos[i + 1][2] - BSpos[0][2])*z_assump ;
				matB_min[i][0] = R[i]*R0_min + 0.5 * RK[i] + (BSpos[i + 1][2] - BSpos[0][2])*z_assump ;
			}
			
			double ans[][] = new double[2][1];
			ans  = multMatrix(pinvA2, matB_max, 2, BSnum - 1, 1);
			x_max = -ans[0][0];
			y_max = -ans[1][0];
			
			ans  = multMatrix(pinvA2, matB_min, 2, BSnum - 1, 1);
			x_min = -ans[0][0];
			y_min = -ans[1][0];
			
			if(R0_min <= 0)
			{
				xydet[0] = x_max;					
				xydet[1] = y_max;
			}
			else
			{
				if(x_max > 0 && y_max > 0)
				{
					xydet[0] = x_max;					
					xydet[1] = y_max;
				}
				else if(x_min > 0 && y_min > 0)
				{
					xydet[0] = x_min;					
					xydet[1] = y_min;	
				}
				else
				{
					return null;
				}
			}
				
				//此处为最终结果
				//System.out.println("location: x = " + xyz[0] + ", y = " + xyz[1] + ", z = " + xyz[2]);
				return xydet;
		}
		else
		{
			System.out.println("结果为复数！"+xydet[2]);
			return null;
		}
	}
	else
	{
		System.out.println("基站数量不足");
		return null;
	}
}
static double[][] pinvMatrix(double A[][], int rowNum, int colNum)
{
	
	double[][] pinvA = new double[colNum][rowNum];
	double[][] ATA = new double[colNum][colNum];
	
	int i,j,k;
	for(i = 0; i < colNum; i++)
	{
		for(j = 0; j < colNum; j++)
		{
			for(k = 0; k < rowNum; k++)
			{
				ATA[i][j] += A[k][i] * A[k][j];
			}
		}
	}

	 // (ATA)^(-1)
	double detATA = 0;
	if(colNum == 3)
	{
		double a1 = ATA[0][0], a2 = ATA[1][0], a3 = ATA[2][0], b1 = ATA[0][1], b2 = ATA[1][1], b3 = ATA[1][2], c1 = ATA[0][2], c2 = ATA[1][2], c3 = ATA[2][2];
		detATA = a1*(b2*c3 - c2*b3) -  a2*(b1*c3 - c1*b3) + a3*(b1*c2 - c1*b2);
		if(Math.abs(detATA) > 0.0001)
		{
			double[][] invATA = new double[3][3];
			invATA[0][0] = (b2*c3 - c2*b3)/detATA;
			invATA[0][1] = (b3*c1 - c3*b1)/detATA;
			invATA[0][2] = (b1*c2 - c1*b2)/detATA;
			invATA[1][0] = (a3*c2 - a2*c3)/detATA;
			invATA[1][1] = (a1*c3 - c1*a3)/detATA;
			invATA[1][2] = (a2*c1 - c2*a1)/detATA;
			invATA[2][0] = (a2*b3 - b2*a3)/detATA;
			invATA[2][1] = (a3*b1 - b3*a1)/detATA;
			invATA[2][2] = (a1*b2 - a2*b1)/detATA;

			//pinv(A) = invATA * AT

			//printf("pinvA \n");
			
			for(i = 0; i < colNum; i++)
			{
				for(j = 0; j < rowNum; j++)
				{
					for(k = 0; k < colNum; k++)
					{
						pinvA[i][j] += invATA[i][k] * A[j][k];
					}
				}
			}
		}
	}
	else if(colNum == 2)
	{
		double a1 = ATA[0][0], a2 = ATA[1][0], a3 = ATA[0][1], a4 = ATA[1][1];
		detATA = a1 * a4 - a2 * a3;			
		if(Math.abs(detATA) > 0.0001)
		{
			double[][] invATA = new double[2][2];
			invATA[0][0] = a4/detATA;
			invATA[0][1] = -a3/detATA;
			invATA[1][0] = -a2/detATA;
			invATA[1][1] = a1/detATA;
			

			//pinv(A) = invATA * AT

			//printf("pinvA \n");
			for(i = 0; i < colNum; i++)
			{
				for(j = 0; j < rowNum; j++)
				{
					for(k = 0; k < colNum; k++)
					{
						pinvA[i][j] += invATA[i][k] * A[j][k];
					}
				}
			}
		}
	}
	else
	{
		System.out.println("矩阵列数超过3，计算开销过大");
	}
	
	return pinvA;
}


static double[][] multMatrix(double ML[][], double MR[][], int rowL, int col, int colR)
{
	double res[][] = new double[rowL][colR];
	int i,j,k;
	for(i = 0; i < rowL; i++)
	{
		for(j = 0; j < colR; j++)
		{
			for(k = 0; k < col; k++)
			{
				res[i][j] += ML[i][k] * MR[k][j];
			}
		}
	}
	return res;
}
double[] locationRLS(double R[], double BSpos[][], int BSnum, double z_assump)
{
	double[] xyz = {0,0,0};

	if(BSnum > 3)
	{
		double[] K = new double[BSnum];
		int i;
		for(i = 0; i < BSnum; i++)
		{
			K[i] = Math.pow(BSpos[i][0],2) + Math.pow(BSpos[i][1],2) + Math.pow(BSpos[i][2],2);
		}

		double[][] A = new double[BSnum-1][3];
		double[] b = new double[BSnum-1];
		for(i = 0; i < BSnum - 1; i++)
		{
			A[i][0] = -(BSpos[i + 1][0] - BSpos[0][0]);
			A[i][1] = -(BSpos[i + 1][1] - BSpos[0][1]);
			A[i][2] = -R[i];
			b[i]    = 0.5 * (Math.pow(R[i],2) - K[i + 1] + K[0] + 2 * (BSpos[i + 1][2] - BSpos[0][2])*z_assump );
		}

			double[][] ATA = new double[3][3];
			int j,k;
			for(i = 0; i < 3; i++)
			{
				for(j = 0; j < 3; j++)
				{
					for(k = 0; k < BSnum - 1; k++)
					{
						ATA[i][j] += A[k][i] * A[k][j];
					}
				}
			}

			 // (ATA)^(-1)
			double a1 = ATA[0][0], a2 = ATA[1][0], a3 = ATA[2][0], b1 = ATA[0][1], b2 = ATA[1][1], b3 = ATA[1][2], c1 = ATA[0][2], c2 = ATA[1][2], c3 = ATA[2][2];
			double detATA = a1*(b2*c3 - c2*b3) -  a2*(b1*c3 - c1*b3) + a3*(b1*c2 - c1*b2);
			if(Math.abs(detATA) > 0.0001)
			{
				double[][] invATA = new double[3][3];
				invATA[0][0] = (b2*c3 - c2*b3)/detATA;
				invATA[0][1] = (b3*c1 - c3*b1)/detATA;
				invATA[0][2] = (b1*c2 - c1*b2)/detATA;
				invATA[1][0] = (a3*c2 - a2*c3)/detATA;
				invATA[1][1] = (a1*c3 - c1*a3)/detATA;
				invATA[1][2] = (a2*c1 - c2*a1)/detATA;
				invATA[2][0] = (a2*b3 - b2*a3)/detATA;
				invATA[2][1] = (a3*b1 - b3*a1)/detATA;
				invATA[2][2] = (a1*b2 - a2*b1)/detATA;

				//pinv(A) = invATA * AT
				double[][] pinvA = new double[3][BSnum-1];
				//printf("pinvA \n");
				for(i = 0; i < 3; i++)
				{
					for(j = 0; j < BSnum - 1; j++)
					{
						for(k = 0; k < 3; k++)
						{
							pinvA[i][j] += invATA[i][k] * A[j][k];
						}
					}
				}

				//pinv(A)*b
				double[] ans = new double[3];
				for(i = 0; i < 3; i++)
				{
					for(j = 0; j < BSnum - 1; j++)
						ans[i] += pinvA[i][j]*b[j];
				}
				xyz[0] = ans[0];
				xyz[1] = ans[1];
				xyz[2] = ans[2];
				//此处为最终结果
				//System.out.println("location: x = " + xyz[0] + ", y = " + xyz[1] + ", z = " + xyz[2]);
				return xyz;
			}
			else
			{
				System.out.println("病态矩阵！");
				return null;
			}
	}
	else
	{
		System.out.println("基站数量不足");
		return null;
	}
}




















}

