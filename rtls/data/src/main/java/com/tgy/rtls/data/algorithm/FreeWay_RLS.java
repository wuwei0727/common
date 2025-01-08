package com.tgy.rtls.data.algorithm;

public class FreeWay_RLS {
	static double pos[];
	static double BSposition[][];
	//static double[][] timeS[][];
	static double[] stat[];


	public static void main(String[] args) {

		
		

		//302
		double BSpos[][] = {{0, 0, 0.15},{0, 3.5, 0.15},{6.4, 2.7, 0.15},{6.4, 0, 0.15},{0, 0, 3.2},{0, 3.5, 3.2},{6.4, 2.7, 3.26},{6.4, 0, 3.25}};
		
		//中石化
		//double BSpos[][] = {{18.2, 7, 1.9},{18.1, 18.2, 1.9},{5.9, 17.05, 2.1},{7, 7.2, 1.9},{11.2, 10, 8.1},{11.25, 16, 8.1},{18.2, 16, 8},{20, 10, 8.07}};		
		double bs_antennadelay = 77.43;

		double time_coef[] = {0, 	0.004691764496356104, 	0.004691765123378587, 	0.004691765406547129, 	0.004691764070210821, 	0.004691764842773581, 	0.004691764358359331, 	0.004691765014322532}; 
		//double time_coef[] = {};
		
		int state[] = {1, 1, 1, 1, 1, 1, 1, 1};
		int j,bsnum = 8;
		//debug
		long bsname[] = {0,1,2,3,4,5,6,7};

		//8BS
		double timeStamp[] = {466160914944d , 466767906429d, 198611846840d, 199218804676d, 222780354408d, 223387311780d, 
				608194332128d, 608801289648d, 145545790464d, 149780820845d,0d,0d, 1095121767653d,  1099356763825d,918381717851d, 922616713086d};
		//double T10 = 9.93307190075E11;
		double T10 = 149173863069d;

				bsnum = 0;
				for(j = 0; j < 8; j++)
				{
					if(timeStamp[2 * j] < 0.1)
						state[j] = 0;
					else
					{
						state[j] = 1;
						bsnum++;
					}
				}
				if(true)  //门限设置
				{			
					pos = location_8(bsnum,bsname, timeStamp, T10, BSpos, bs_antennadelay, time_coef, state);
					System.out.println("x "+pos[0]+" y "+pos[1]+" z "+pos[2]);
				}



        
	}
	
	public static double getDis(double timeStamp[], double anneDelayBS0,double anneDelayTag)
	{
		//利用2个基站来进行固定高度的平面定位
		//timeStamp  为Tag_tx_timestamp,Bs_rx_timestamp,Bs_tx_timestamp,Tag_rx_timestamp
  		//anneDelay 天线延时  基站和标签不同 77.38


		
		double coef = 0.00469176397861579;

		double dT[] = new double[2];
		double dis1;
        dT[0] = getdiff(timeStamp[3] , timeStamp[0]);
    	dT[1] = getdiff(timeStamp[2] , timeStamp[1]);
		dis1 = ( dT[0] -  dT[1]) * coef / 2 - anneDelayBS0 - anneDelayTag;
		return dis1;

	}
	static double[] location_8(int bsnum, long[] bsname, double timeStamp[], double T10, double BSpos[][], double anneDelay, double time_coef[], int state[])
	{
		//8BS 立体定位
		
//		input: bsname为8个基站ID，timeStamp为16个时间戳第2N和2N+1个元素为对应第N个基站ID的时间戳T和t，前八个为第一主基站的区域，后八个为第二个主基站的区域，T10为主基站M1监听到M0的sync信号时M1自己的时间戳T10，BSpos[N][3]为与bsname对应的基站x,y,z
//		anneDelay为天线延时，time_coef为与bsname对应的晶振校正参数，其中M0对应的为default值，M1对应的为以M0为参考的校正参数，state是基站接收状态
//		output：pos[4]分别为[x,y,z,精度因子];

		int minBSnum = 6;
		double xyzDet[] = new double[4];
		
		double ans[] = new double[6];
		double R[] = new double[7];
		double d[] = new double[8];

		int i,j;

		double dt[] = new double[8];
		if(bsnum <  minBSnum)
		{
			return null; 
		}

		for(i = 0; i < 8; i++)
		{
			dt[i] = getdiff(timeStamp[2 * i + 1] , timeStamp[2 * i]); 
		}
		 
		time_coef[0] =  0.00469176397861579;

		
		for(i = 0; i < 3; i++)
		{
			d[i] = Math.sqrt(Math.pow(BSpos[i+1][0] - BSpos[0][0], 2) + Math.pow(BSpos[i+1][1] - BSpos[0][1], 2) + Math.pow(BSpos[i+1][2] - BSpos[0][2], 2));
			R[i] = dt[i + 1] * time_coef[i + 1]  - dt[0] * time_coef[0] + 2 * anneDelay + d[i];
		}
		double dTM10 = getdiff(timeStamp[9] , T10);
		d[3] = Math.sqrt(Math.pow(BSpos[4][0] - BSpos[0][0], 2) + Math.pow(BSpos[4][1] - BSpos[0][1], 2) + Math.pow(BSpos[4][2] - BSpos[0][2], 2));
		R[3] = dTM10 * time_coef[4]  - dt[0] * time_coef[0] + 2 * anneDelay + d[3];

		for(i = 4; i < 7; i++)
		{
			d[i] = Math.sqrt(Math.pow(BSpos[i+1][0] - BSpos[4][0], 2) + Math.pow(BSpos[i+1][1] - BSpos[4][1], 2) + Math.pow(BSpos[i+1][2] - BSpos[4][2], 2));
			R[i] = dt[i + 1] * time_coef[i + 1]  - dt[4] * time_coef[0] + 2 * anneDelay + d[i] + R[3];
		}
		

		double[][] BS = resortBS(BSpos, state);
		int BSnum = 0;

		for(i = 0; i < 8; i++)
		{
			if(state[i] == 1)

				BSnum++;
		}
		if(BSnum < 6)
		{
			System.out.println("基站数量小于6");
			return null;
		}
		else
		{
			double RT[] = new double[BSnum - 1];
			j = 0;
			for(i = 1; i < BSnum; i++)
			{
				if(state[i] == 1)
				{
					RT[j] = R[i - 1];
					j++;
					}
			}


			xyzDet = location3DChan(RT, BS, BSnum);
			if(xyzDet != null)
			{
				for(i = 0; i < 3; i++)
				{
					ans[i] = xyzDet[i];
				}
				
				return ans;
			}
			else
			{
				return null;
			}

		}
		
	}
	
	static	double[][] resortBS(double BSpos[][], int state[])
	{
		double[][] BS = new double[4][3];
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
	public static double getdiff(double x2, double x1)
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

	
public	static double[] location_4(int bsnum, long[] bsname, double timeStamp[], double BSpos[][], double anneDelay, double time_coef[], double z_assump)
	{
		//4BS 平面定位
		
//		input: bsname为4个基站ID，timeStamp为8个时间戳，每个基站的两个时间戳，按bsname顺序排列
//		BSpos[N][3]为与bsname对应的基站x,y,z
//		anneDelay为天线延时，
//		time_coef为与bsname对应的晶振校正参数
//		z_assump为假设的标签高度
//		output：pos[3]分别为[x,y,det];

		int minBSnum = 4;
		double xyzDet[] = new double[4];
		
		double ans[] = new double[6];
		double R[] = new double[3];
		double d[] = new double[3];

		int i,j;

		double dt[] = new double[4];
		if(bsnum <  minBSnum)
		{
			System.out.println("基站数量不足4");
			return null; 
		}

		for(i = 0; i < 4; i++)
		{
			dt[i] = getdiff(timeStamp[2 * i + 1] , timeStamp[2 * i]); 
		}
		 
		time_coef[0] =  0.00469176397861579;

		
		for(i = 0; i < 3; i++)
		{
			d[i] = Math.sqrt(Math.pow(BSpos[i+1][0] - BSpos[0][0], 2) + Math.pow(BSpos[i+1][1] - BSpos[0][1], 2) + Math.pow(BSpos[i+1][2] - BSpos[0][2], 2));
			R[i] = dt[i + 1] * time_coef[i + 1]  - dt[0] * time_coef[0] + 2 * anneDelay + d[i];
		}

		System.out.println("进入4基站解算 R值 "+R[0]+" "+R[1]+" "+R[2]);
		xyzDet = locationChan2D(R, BSpos, bsnum, z_assump);
		if(xyzDet != null)
		{
			for(i = 0; i < 3; i++)
			{
				ans[i] = xyzDet[i];
			}
			ans[2]=0;
			return ans;
		}
		else
		{
			System.out.println("未解除有效结果");
			return null;
		}
		
	}
	
//校准4基站
public	static double[] location_4re(int bsnum, long[] bsname, double timeStamp[], double BSpos[][], double anneDelay, double time_coef[], double z_assump, double[] pos_new )
{
	//4BS 平面定位
	
//	input: bsname为4个基站ID，timeStamp为8个时间戳，每个基站的两个时间戳，按bsname顺序排列
//	BSpos[N][3]为与bsname对应的基站x,y,z
//	anneDelay为天线延时，
//	time_coef为与bsname对应的晶振校正参数
//	z_assump为假设的标签高度
//	output：pos[3]分别为[x,y,det];

	int minBSnum = 4;
	double xyzDet[] = new double[4];
	
	double ans[] = new double[6];
	double R[] = new double[3];
	double d[] = new double[3];

	int i,j;

	double dt[] = new double[4];
	if(bsnum <  minBSnum)
	{
		System.out.println("基站数量不足4");
		return null; 
	}

	for(i = 0; i < 4; i++)
	{
		dt[i] = getdiff(timeStamp[2 * i + 1] , timeStamp[2 * i]); 
	}
	 
	time_coef[0] =  0.00469176397861579;

	
	for(i = 0; i < 3; i++)
	{
		d[i] = Math.sqrt(Math.pow(BSpos[i+1][0] - BSpos[0][0], 2) + Math.pow(BSpos[i+1][1] - BSpos[0][1], 2) + Math.pow(BSpos[i+1][2] - BSpos[0][2], 2));
		R[i] = dt[i + 1] * time_coef[i + 1]  - dt[0] * time_coef[0] + 2 * anneDelay + d[i];
	}
	double[] R_new =  corr_R( pos_new,  R,  bsname,  bsnum);
	System.out.println("进入4基站解算");
	xyzDet = locationChan2D(R_new, BSpos, bsnum, z_assump);
	if(xyzDet != null)
	{
		for(i = 0; i < 3; i++)
		{
			ans[i] = xyzDet[i];
		}
		ans[2]=0;
		return ans;
	}
	else
	{
		System.out.println("未解除有效结果");
		return null;
	}
	
}



static double[][] ref_pos_103 = {{49.296, 1.828, 1.302},{51.855, 5.55, 1.308},{55.883, 1.77, 1.331},{52.24, -1.38, 1.328}};
static double[][] corr_para_103 = {{0.3798711951174978, 0.0728251873801602, -0.0349783996310048},{-0.013822312813917437, -0.26600191031918996, -0.09522205876021905},
{0.04260034092835152, 0.35918573583810787, 0.49494232307068453},{0.4482933078958964, 0.639256025210325, -0.10770992907501409}};

static double[][] ref_pos_104 = {{88.102, 21.504, 1.434},
		{96.195, 32.687, 1.465},
		{84.652, 31.006, 1.585},
		{81.482, 11.007, 1.381},
		{93.047, 8.726, 1.231},
		{88.11, 9.979, 1.307},
		{81.0, 10.879, 1.397}};

static double[][] corr_para_104 = {{0.30960296221203, 0.1360886977023087, 0.18589907247464388},
		{0.53474815855507, 0.16505556486364625, 0.039761191192244194 },
		{0.44819331358582204, 0.2687162985311389, 0.3285806382862999 },
		{0.3277384842883855, 0.12697895046276741, 0.23980055374204223},
		{0.2911551641713679, 0.07653236114864015, 0.1541651839452589 },
		{0.16173223748215193, 0.12026875270573001, 0.09701182822409637 },
		{0.3315960136852052, 0.08545398375316093, 0.1648177468920622}};


static double[][] ref_pos_102 = {{20.474, 18.45, 1.256},
{41.971, 27.348, 1.222},
{54.523, 10.892, 1.24},
{70.857, 14.52, 1.194},
{73.68, 28.936, 1.174},
{48.827, 17.268, 1.212}};
static double[][] corr_para_102 = {{0.5388738340499017, 0.6529411795347642, 0.6263579205838945 }, 
 {0.31585881983075303, 0.1444738939298924, 0.36367941918476454 },
 {0.663359404270544, 0.21011053212092579, 0.31067404923981323 },
 {0.10592593527358218, 0.4631305050012742, 0.27633810771708056 },
 {0.35810263576897583, 0.3206740168221245, 0.14981589126913875 },
 {0.33485946104313324, 0.29480484936668727, 0.34552420521403215}};


static double getVecDiff(double[] X1, double[] X2)
{
	double norm = 0;
	int size = X1.length;
	//if(size != X2.length)
		//return 0;
	//else
	//{
		for(int i = 0; i < size; i++)
		{
			norm += (X1[i] - X2[i])*(X1[i] - X2[i]);
		}
		norm = Math.sqrt(norm );
		return norm;
	//}
}
static double[] corr_R(double[] pos_new, double[] R, long[] bsname, int bsnum)
{
int i,j;
if((int)(bsname[0]/10%1000) == 103)
{
	double[] dis = new double[ref_pos_103.length];
	for(i =0; i <ref_pos_103.length; i++)
	{
		dis[i] = getVecDiff(ref_pos_103[i], pos_new);
	}
	double sumDis = 0;
	for(i =0; i <ref_pos_103.length; i++)
	{
		sumDis += dis[i];
	}
	for(i =0; i <ref_pos_103.length; i++)
	{
		dis[i] = sumDis/dis[i];
	}
	sumDis = 0;
	for(i =0; i <ref_pos_103.length; i++)
	{
		sumDis += dis[i];
	}
	double[] weight = new double[bsnum-1];
	for(i =0; i <ref_pos_103.length; i++)
	{
		dis[i] /= sumDis;
	}
	for(i = 0; i <bsnum-1; i++)
	{
		for(j = 0; j<ref_pos_103.length; j++)
		{
			weight[i] += dis[j]*corr_para_103[j][i];
		}
		R[i] += weight[i];
	}
}
else if((int)(bsname[0]/10%1000) == 104)
{
	int size = ref_pos_104.length;
	double[] dis = new double[size];
	for(i =0; i <size; i++)
	{
		dis[i] = getVecDiff(ref_pos_104[i], pos_new);
	}
	double sumDis = 0;
	for(i =0; i <size; i++)
	{
		sumDis += dis[i];
	}
	for(i =0; i <size; i++)
	{
		dis[i] = sumDis/dis[i];
	}
	sumDis = 0;
	for(i =0; i <size; i++)
	{
		sumDis += dis[i];
	}
	double[] weight = new double[bsnum-1];
	for(i =0; i <size; i++)
	{
		dis[i] /= sumDis;
	}
	for(i = 0; i <bsnum-1; i++)
	{
		for(j = 0; j<size; j++)
		{
			weight[i] += dis[j]*corr_para_104[j][i];
		}
		R[i] += weight[i];
	}
}
else if((int)(bsname[0]/10%1000) == 102)
{
	int size = ref_pos_102.length;
	double[] dis = new double[size];
	for(i =0; i <size; i++)
	{
		dis[i] = getVecDiff(ref_pos_102[i], pos_new);
	}
	double sumDis = 0;
	for(i =0; i <size; i++)
	{
		sumDis += dis[i];
	}
	for(i =0; i <size; i++)
	{
		dis[i] = sumDis/dis[i];
	}
	sumDis = 0;
	for(i =0; i <size; i++)
	{
		sumDis += dis[i];
	}
	double[] weight = new double[bsnum-1];
	for(i =0; i <size; i++)
	{
		dis[i] /= sumDis;
	}
	for(i = 0; i <bsnum-1; i++)
	{
		for(j = 0; j<size; j++)
		{
			weight[i] += dis[j]*corr_para_102[j][i];
		}
		R[i] += weight[i];
	}
}
else if((int)(bsname[0]/10%1E6) == 32132)
{
	R[0] += 0;
	R[1] += 2;
	R[2] += 2;
}
else if((int)(bsname[0]/100%1E5) == 1208)
{
	R[0] += 0;
	R[1] += 2;
	R[2] += 2;
}
return R;
}

//   校准钢架高度
	
	
/*
public static double[] location1D(double dis1, double dis2,long bs0number,long bs1number, double anneDelayBS0,double anneDelayBS1,double anneDelayTag, double z_assump)
{
	//利用2个基站来进行固定高度的平面定位
	//timeStamp[T0第1个基站发射的第一个时间戳  T1 标签收到信号的时间戳 T2 标签发射的时间戳 T3 第1个基站收到信号的时间戳 T4第2个基站发射的第一个时间戳  T5 标签收到信号的时间戳 T6 标签发射的时间戳 T7 第2个基站收到信号的时间戳 ]
	//BSpos为基站坐标 目前算法上要求BSpos的x和z一致，可以后期进行坐标转换
	//目前有效的输入为：对于铁罐后的盲区，输入顺序为1013,1010； 对于楼梯侧的盲区，输入顺序为1001， 1000
	//anneDelay 天线延时  基站和标签不同 default值为 78.28
	//z_assump 假定的标签高度
	Bs_inf bs0=Constant.bsstation.get(bs0number);
	Bs_inf bs1=Constant.bsstation.get(bs1number);

  
*/
/*	System.out.print("bs1"+bs1.x);
	System.out.print("bs2"+bs2.x);
	System.out.print("bs3"+bs3.x);
	System.out.print("bs4"+bs4.x);
	System.out.print("bs5"+bs5.x);
	*//*

	
	System.out.println("bs0number:"+bs0number+"bs0number1"+bs1number+"dis1:"+dis1+"dis2:"+dis2);
	double BSpos[][]={{bs0.x,bs0.y,bs0.z},
			          {bs1.x,bs1.y,bs1.z}
			      
			          };
	double[][] BSnew = new double[2][3];
	BSnew[0][2] = BSpos[0][2];
	BSnew[1][1] = Math.sqrt(Math.pow(BSpos[0][0]-BSpos[1][0],2)+Math.pow(BSpos[0][1]-BSpos[1][1],2));
	BSnew[1][2] = BSpos[1][2];
	System.out.println("基站坐标: ["+BSnew[0][0]+" "+BSnew[0][1]+"; "+BSnew[1][0]+" "+BSnew[1][1]+"] ");
	
	double[] mat_B = new double[2];
	mat_B[0] = BSpos[1][0] - BSpos[0][0];
	mat_B[1] = BSpos[1][1] - BSpos[0][1];
	
	double[][] mat_A = new double[2][2];
	mat_A[0][0] = BSnew[1][0];
	mat_A[0][1] = BSnew[1][1];
	mat_A[1][0] = BSnew[1][1];
	mat_A[1][1] = -BSnew[1][0];
	
	double[][] invA = pinvMatrix(mat_A,2,2);
	double[] transCoef = new double[2];
	transCoef[0] = invA[0][0] * mat_B[0] + invA[0][1] * mat_B[1];
	transCoef[1] = invA[1][0] * mat_B[0] + invA[1][1] * mat_B[1];
	double[][] coorTrans = {{transCoef[0], transCoef[1]}, {-transCoef[1], transCoef[0]}};
	double pos[] = new double[3];
	double b, a, aa, bb, cc, x1, x2, y1, y2;
    a = (Math.pow(BSnew[0][0],2) - Math.pow(BSnew[1][0],2) + Math.pow(BSnew[0][1],2) - Math.pow(BSnew[1][1],2) + Math.pow(z_assump - BSnew[0][2],2) - Math.pow(z_assump - BSnew[1][2],2) + dis2*dis2 - dis1*dis1) /(BSnew[0][1] - BSnew[1][1]) /2;
	b = - (BSnew[0][0] - BSnew[1][0]) /(BSnew[0][1] - BSnew[1][1]);
	aa = 1 + b*b;
	bb = 2 * b * (a - BSnew[0][1]) - 2 * BSnew[0][0];
	cc =BSnew[0][0] * BSnew[0][0] + (a - BSnew[0][1]) *  (a - BSnew[0][1]) + (z_assump - BSnew[0][2]) * (z_assump - BSnew[0][2]) - dis1*dis1;
	
	x1 = (- bb + Math.sqrt(bb * bb - 4*aa*cc))/(2*aa);
	x2 = (- bb - Math.sqrt(bb * bb - 4*aa*cc))/(2*aa);
	y1 = a + b*x1;
	y2 = a + b*x2;
	//System.out.println("未转换的定位结果："+x1+" "+y1);
	if(x1 > 0)
	{
		pos[0] = coorTrans[0][0]*x2 + coorTrans[0][1]*y2 + BSpos[0][0];
		pos[1] = coorTrans[1][0]*x2 + coorTrans[1][1]*y2 + BSpos[0][1];
		pos[2] = z_assump;
		System.out.println("多值定位结果："+pos[0]+" "+pos[1]+" "+pos[2]);
		pos[0] = coorTrans[0][0]*x1 + coorTrans[0][1]*y1 + BSpos[0][0];
		pos[1] = coorTrans[1][0]*x1 + coorTrans[1][1]*y1 + BSpos[0][1];
		pos[2] = z_assump;
		System.out.println("定位结果："+pos[0]+" "+pos[1]+" "+pos[2]);
		return pos;
	}
	else
	{
		pos[0] = coorTrans[0][0]*x1 + coorTrans[0][1]*y1 + BSpos[0][0];
		pos[1] = coorTrans[1][0]*x1 + coorTrans[1][1]*y1 + BSpos[0][1];
		pos[2] = z_assump;
		System.out.println("无指定区域有效解，定位结果："+pos[0]+" "+pos[1]+" "+pos[2]);

		return null;
	}
}
*/

	/*
	static double[] location_2(double timeStamp[], double BSpos[][], double anneDelayBS0,double anneDelayBS1,double anneDelayTag, double z_assump)
	{
		//利用2个基站来进行固定高度的平面定位
		//timeStamp[8] = [T0第1个基站发射的第一个时间戳  T1 标签收到信号的时间戳 T2 标签发射的时间戳 T3 第1个基站收到信号的时间戳 T4第2个基站发射的第一个时间戳  T5 标签收到信号的时间戳 T6 标签发射的时间戳 T7 第2个基站收到信号的时间戳 ]
		//BSpos[2][3]为基站坐标 目前算法上要求BSpos的x和z一致，可以后期进行坐标转换
		//anneDelay 天线延时  基站和标签不同 
		//z_assump 假定的标签高度
		//output pos[3]为[x,y,z]
		double coef = 0.00469176397861579;
		double pos[] = new double[3];
		double dT[] = new double[2];
		double dis1, dis2, c, b, a, aa, bb, cc, x1, x2, y1, y2;
        dT[0] = getdiff(timeStamp[3] , timeStamp[0]);
    	dT[1] = getdiff(timeStamp[2] , timeStamp[1]);
		dis1 = ( dT[0] -  dT[1]) * coef / 2 - anneDelayBS0 - anneDelayTag;
        dT[0] = getdiff(timeStamp[7] , timeStamp[4]);
    	dT[1] = getdiff(timeStamp[6] , timeStamp[5]);
		dis2 = ( dT[0] -  dT[1]) * coef / 2 - anneDelayBS1 - anneDelayTag;
	    a = (Math.pow(BSpos[0][0],2) - Math.pow(BSpos[1][0],2) + Math.pow(BSpos[0][1],2) - Math.pow(BSpos[1][1],2) + Math.pow(z_assump - BSpos[0][2],2) - Math.pow(z_assump - BSpos[1][2],2) + dis2*dis2 - dis1*dis1) /(BSpos[0][1] - BSpos[1][1]) /2;
		b = - (BSpos[0][0] - BSpos[1][0]) /(BSpos[0][1] - BSpos[1][1]);
		aa = 1 + b*b;
		bb = 2 * b * (a - BSpos[0][1]) - 2 * BSpos[0][0];
		cc =BSpos[0][0] * BSpos[0][0] + (a - BSpos[0][1]) *  (a - BSpos[0][1]) + (z_assump - BSpos[0][2]) * (z_assump - BSpos[0][2]) - dis1*dis1;
		
		x1 = (- bb + Math.sqrt(bb * bb - 4*aa*cc))/(2*aa);
		x2 = (- bb - Math.sqrt(bb * bb - 4*aa*cc))/(2*aa);
		y1 = a + b*x1;
		y2 = a + b*x2;
		
		if(x1 > 0)
		{
			pos[0] = x1;
			pos[1] = y1;
			pos[2] = z_assump;
			return pos;
		}
		else
		{
			System.out.println("无有效解");
			return null;
		}
	}
	
	
	
	public static double getdiff(double x2, double x1)
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
	
	
	static double[][] resortBS(double BSpos[][], int state[])
	{
		int BSnum = BSpos.length; 
		double[][] BS = new double[BSnum][3];
		int i,j,k;
		k = 0;
		for(i = 0; i < BSnum; i++)
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
	*/
	
	static double[] location3DChan(double R[], double BSpos[][], int BSnum)
	{
		//真·三维定位 
		double xyzDet[] = {0, 0, 0, 0};

		if(BSnum > 6)
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
			double[] RK = new double[BSnum-1];
			for(i = 0; i < BSnum - 1; i++)
			{
				A[i][0] = (BSpos[i + 1][0] - BSpos[0][0]);
				A[i][1] = (BSpos[i + 1][1] - BSpos[0][1]);
				A[i][2] = (BSpos[i + 1][2] - BSpos[0][2]);
				RK[i]    = Math.pow(R[i],2) - K[i + 1] + K[0];
			}
			double[][] pinvA = pinvMatrix(A,  BSnum - 1, 3);
									
			double[] e = new double[3];
			double[] f = new double[3];

			for(i = 0; i < 3; i++)
			{
				for(j = 0; j < BSnum - 1; j++)
				{
					e[i] -= pinvA[i][j] * R[j];
					f[i] -= 0.5 * pinvA[i][j] * RK[j]; 
				}
			}
			
			double aa = 1 - e[0]*e[0] - e[1]*e[1] - e[2]*e[2];
			double bb = 2 * (m[0]*e[0] + m[1]*e[1] + m[2]*e[2] - (f[0]*e[0] + f[1]*e[1] + f[2]*e[2]));
			double cc = 2*(m[0]*f[0] + m[1]*f[1] + m[2]*f[2]) - (f[0]*f[0] + f[1]*f[1] + f[2]*f[2]) - K[0];
			
			xyzDet[3] = bb*bb - 4*aa*cc;

			if(xyzDet[3] >= 0)
			{
				
				double R0_max = (- bb + Math.sqrt(xyzDet[3])) / (2 * aa);
				double R0_min = (- bb - Math.sqrt(xyzDet[3])) / (2 * aa);
				double pinvA2[][] = pinvMatrix(A, BSnum-1, 3);
				double x_max, x_min, y_max, y_min, z_max, z_min;
				double matB_max[][] = new double[BSnum-1][1];
				double matB_min[][] = new double[BSnum-1][1];
				for(i = 0; i < BSnum - 1; i++)
				{
					matB_max[i][0] = R[i]*R0_max + 0.5 * RK[i] ;
					matB_min[i][0] = R[i]*R0_min + 0.5 * RK[i] ;
				}
				
				double ans[][] = new double[2][1];
				ans  = multMatrix(pinvA2, matB_max, 3, BSnum - 1, 1);
				x_max = -ans[0][0];
				y_max = -ans[1][0];
				z_max = -ans[2][0];
				
				ans  = multMatrix(pinvA2, matB_min, 3, BSnum - 1, 1);
				x_min = -ans[0][0];
				y_min = -ans[1][0];
				z_min = -ans[2][0];
				
				if(R0_min <= 0)
				{
					xyzDet[0] = x_max;					
					xyzDet[1] = y_max;
					xyzDet[2] = z_max;
				}
				else
				{
					if(x_max > 0 && y_max > 0)
					{
						xyzDet[0] = x_max;					
						xyzDet[1] = y_max;
						xyzDet[2] = z_max;
					}
					else if(x_min > 0 && y_min > 0)
					{
						xyzDet[0] = x_min;					
						xyzDet[1] = y_min;	
						xyzDet[2] = z_min;	
					}
					else
					{
						return null;
					}
				}
					
					//此处为最终结果
					System.out.println("location: x = " + xyzDet[0] + ", y = " + xyzDet[1] + ", z = " + xyzDet[2]);
					return xyzDet;
			}
			else
			{
				System.out.println("结果为复数！"+xyzDet[3]);
				return null;
			}
		}
		else
		{
			System.out.println("基站数量不足");
			return null;
		}
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
						System.out.println("解出结果超范围");
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


    static double[] TDOA_chan2D(double[] R, double[][] resortBSpos, double AssumptionZ, double BSMinx, double BSMaxx, double BSMaxy, double BSMiny, int bsnum)
    //适用于2维基站等高度运算 与locationChan的区别在于添加了detR的计算和修正了有效值判断
    {
        //二维定位，基站没有高度差
        double[] detR = {0, 0, 0};
        double[] tagPos = {0, 0, 0};
        double xydet[] = {0, 0, 0};
        int BSnum = bsnum;
        double[][] BSpos = resortBSpos;

        double[] K = new double[BSnum];
        int i, j;
        for (i = 0; i < BSnum; i++) {
            K[i] = Math.pow(BSpos[i][0], 2) + Math.pow(BSpos[i][1], 2);
        }

        double[] m = new double[3];
        for (i = 0; i < 2; i++) {
            m[i] = BSpos[0][i];
        }

        double[][] A = new double[BSnum - 1][2];
        double[] RK = new double[BSnum - 1];
        for (i = 0; i < BSnum - 1; i++) {
            A[i][0] = (BSpos[i + 1][0] - BSpos[0][0]);
            A[i][1] = (BSpos[i + 1][1] - BSpos[0][1]);
            //A[i][2] = (BSpos[i + 1][2] - BSpos[0][2]);

        }
        double[][] pinvA = pinvMatrix(A, BSnum - 1, 2);

        for (i = 0; i < BSnum - 1; i++) {
            //RK[i]    = 0.5 * (Math.pow(R[i],2) - K[i + 1] + K[0]) + (BSpos[i + 1][2] - BSpos[0][2])*z_assump + R[i]*R0;
            RK[i] = Math.pow(R[i], 2) - K[i + 1] + K[0];
        }


        double[] e = new double[3];
        double[] f = new double[3];

        for (i = 0; i < 2; i++) {
            for (j = 0; j < BSnum - 1; j++) {
                e[i] -= pinvA[i][j] * R[j];
                f[i] -= 0.5 * pinvA[i][j] * RK[j];
            }
        }

        double aa = 1 - e[0] * e[0] - e[1] * e[1] - e[2] * e[2];
        double bb = 2 * (m[0] * e[0] + m[1] * e[1] + m[2] * e[2] - (f[0] * e[0] + f[1] * e[1] + f[2] * e[2]));
        double cc = 2 * (m[0] * f[0] + m[1] * f[1] + m[2] * f[2]) - (f[0] * f[0] + f[1] * f[1] + f[2] * f[2]) - K[0];

        xydet[2] = bb * bb - 4 * aa * cc;

        if (xydet[2] >= 0) {

            double R0_max = (-bb + Math.sqrt(xydet[2])) / (2 * aa);
            double R0_min = (-bb - Math.sqrt(xydet[2])) / (2 * aa);
            double pinvA2[][] = pinvMatrix(A, BSnum - 1, 2);
            double x_max, x_min, y_max, y_min;
            double matB_max[][] = new double[BSnum - 1][1];
            double matB_min[][] = new double[BSnum - 1][1];
            for (i = 0; i < BSnum - 1; i++) {
                matB_max[i][0] = R[i] * R0_max + 0.5 * RK[i];// + (BSpos[i + 1][2] - BSpos[0][2])*z_assump ;
                matB_min[i][0] = R[i] * R0_min + 0.5 * RK[i];// + (BSpos[i + 1][2] - BSpos[0][2])*z_assump ;
            }

            double ans[][] = new double[2][1];
            ans = multMatrix(pinvA2, matB_max, 2, BSnum - 1, 1);
            x_max = -ans[0][0];
            y_max = -ans[1][0];

            ans = multMatrix(pinvA2, matB_min, 2, BSnum - 1, 1);
            x_min = -ans[0][0];
            y_min = -ans[1][0];

            if (R0_min <= 0) {
                xydet[0] = x_max;
                xydet[1] = y_max;
                tagPos[2] = 4;
            } else {
                if (x_max > BSMinx && y_max > BSMiny) {
                    xydet[0] = x_max;
                    xydet[1] = y_max;
                    tagPos[2] = 4;
                } else if (x_min > BSMinx && y_min > BSMiny) {
                    xydet[0] = x_min;
                    xydet[1] = y_min;
                    tagPos[2] = 4;
                } else {
                    System.out.println("进入解算，未解出有效结果");
                    tagPos[2] = 0;
                }
            }


        } else {
            System.out.println("结果为复数！" + xydet[2]);
            tagPos[2] = 0;
        }
        tagPos[0] = xydet[0];
        tagPos[1] = xydet[1];
        System.out.println("4bs location x:" + xydet[0] + "; y:" + xydet[1]);
        if (tagPos[2] == 4) {
            double[] caltPos = {tagPos[0], tagPos[1], 0.4};

            for (i = 1; i < bsnum; i++)
                detR[i - 1] = Math.abs(getVecDiff(caltPos, resortBSpos[i]) - getVecDiff(caltPos, resortBSpos[0]) - R[i - 1]);

        }

        return tagPos;
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
	


}


