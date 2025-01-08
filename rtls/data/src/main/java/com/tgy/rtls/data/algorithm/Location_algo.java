package com.tgy.rtls.data.algorithm;


public class Location_algo {
	
	static double[] TDOA_3bs(double[] R, double[][] resortBSpos, double AssumptionZ, double BSMinx, double BSMaxx, double BSMaxy, double BSMiny, int bsnum, double[] detR)
	{
		double k1, k2, k3;
		double a11, a12;
		double a21, a22;
		double b11, b12;
		double b21, b22;
		double AMod;
		double xx=0, yy=0;
		double R21 = R[0];
		double R31 = R[1];
		double BSx1 = resortBSpos[0][0];
		double BSy1 = resortBSpos[0][1];
		double BSz1 = resortBSpos[0][2];
		
		double BSx2 = resortBSpos[1][0];
		double BSy2 = resortBSpos[1][1];
		double BSz2 = resortBSpos[1][2];
		
		double BSx3 = resortBSpos[2][0];
		double BSy3 = resortBSpos[2][1];
		double BSz3 = resortBSpos[2][2];
		
		


			k1 = BSx1*BSx1 + BSy1*BSy1 + BSz1*BSz1;
			k2 = BSx2*BSx2 + BSy2*BSy2 + BSz2*BSz2;
			k3 = BSx3*BSx3 + BSy3*BSy3 + BSz3*BSz3;
			a11 = -2 * (BSx2 - BSx1); a12 = -2 * (R21);
			a21 = -2 * (BSx3 - BSx1); a22 = -2 * (R31);
			b11 = R21*R21 - k2 + k1 + (AssumptionZ - BSz2)*(AssumptionZ - BSz2) - (AssumptionZ - BSz1) *(AssumptionZ - BSz1); b12 = 2 * (BSy2 - BSy1);
			b21 = R31*R31 - k3 + k1 + (AssumptionZ - BSz3)*(AssumptionZ - BSz3) - (AssumptionZ - BSz1) *(AssumptionZ - BSz1); b22 = 2 * (BSy3 - BSy1);
			AMod = (a11*a22 - a12*a21);

			double x_cons, r1_cons, r1_coef, x_coef;
			x_cons = (a22*b11) / AMod - (a12*b21) / AMod - BSx1;
			r1_cons = (a11*b21) / AMod - (a21*b11) / AMod;
			r1_coef = (a11*b22) / AMod - (a21*b12) / AMod;
			x_coef = (a22*b12) / AMod - (a12*b22) / AMod;

			double A_Y, B_Y, C_Y;
			A_Y = -r1_coef *r1_coef + x_coef *x_coef + 1;
			B_Y = -2 * BSy1 - 2 * r1_coef*r1_cons + 2 * x_coef*x_cons;
			C_Y = BSy1*BSy1 - r1_cons *r1_cons + x_cons *x_cons + (AssumptionZ - BSz1)*(AssumptionZ - BSz1);

			double y1, y2, x1, x2;
			if ((B_Y*B_Y - 4 * A_Y*C_Y) >= 0)
			{
				y1 = (-B_Y + Math.pow((B_Y*B_Y - 4 * A_Y*C_Y), 0.5)) / (2 * A_Y);
				y2 = (-B_Y - Math.pow((B_Y*B_Y - 4 * A_Y*C_Y), 0.5)) / (2 * A_Y);
			}
			else {
				y1 = (-B_Y) / (2 * A_Y);
				y2 = (-B_Y) / (2 * A_Y);
			}
			x1 = x_cons + BSx1 + x_coef*y1;
			x2 = x_cons + BSx1 + x_coef*y2;


			//	%判断踢出模糊解。

			//	%判断1：是否先验信息，R31、R21。
			double x1d31, x2d31, x1d21, x2d21;;
			x1d31 =Math. pow(((x1 - BSx3) *(x1 - BSx3) + (y1 - BSy3) *(y1 - BSy3)), 0.5) -Math. pow(((x1 - BSx1) *(x1 - BSx1) + (y1 - BSy1) *(y1 - BSy1)), 0.5) - R31;
			x2d31 =Math. pow(((x2 - BSx3) *(x2 - BSx3) + (y2 - BSy3) *(y2 - BSy3)), 0.5) -Math. pow(((x2 - BSx1) *(x2 - BSx1) + (y2 - BSy1) *(y2 - BSy1)), 0.5) - R31;

			x1d21 =Math. pow(((x1 - BSx2) *(x1 - BSx2) + (y1 - BSy2) *(y1 - BSy2)), 0.5) -Math. pow(((x1 - BSx1) *(x1 - BSx1) + (y1 - BSy1) *(y1 - BSy1)), 0.5) - R21;
			x2d21 =Math. pow(((x2 - BSx2) *(x2 - BSx2) + (y2 - BSy2) *(y2 - BSy2)), 0.5) -Math. pow(((x2 - BSx1) *(x2 - BSx1) + (y2 - BSy1) *(y2 - BSy1)), 0.5) - R21;
			
			double x1detR = Math.abs(x1d31) + Math.abs(x1d21);
			double x2detR = Math.abs(x2d31) + Math.abs(x2d21);
			
			if (x1detR < x2detR)
			{
				xx = x1;
				yy = y1;
			}
			else
			{
				xx = x2;
				yy = y2;
			}


				double[] caltPos = {xx,yy,AssumptionZ};
				
				for(int i = 1; i < bsnum; i++)
					detR[i-1] = Math.abs(getVecDiff(caltPos,resortBSpos[i]) - getVecDiff(caltPos,resortBSpos[0]) - R[i-1]);
				

		double[] tagPos = {0,0,0};
		tagPos[0]=xx;
		tagPos[1]=yy;
		tagPos[2] = 3;
		System.out.println("3bs location x:"+xx+"; y:"+yy);

		return tagPos;
	}

	static double[] TDOA_chan2D(double[] R, double[][] resortBSpos, double AssumptionZ, double BSMinx, double BSMaxx, double BSMaxy, double BSMiny, int bsnum, double[] detR)
	//适用于2维基站等高度运算 与locationChan的区别在于添加了detR的计算和修正了有效值判断
	{
		//二维定位，基站没有高度差
		double[] tagPos = {0,0,0};
		double xydet[] = {0, 0, 0};
		int BSnum = bsnum;
		double[][] BSpos = resortBSpos;
		
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
					tagPos[2] = 4;
				}
				else
				{
					if(x_max > BSMinx && y_max > BSMiny)
					{
						xydet[0] = x_max;					
						xydet[1] = y_max;
						tagPos[2] = 4;
					}
					else if(x_min > BSMinx && y_min > BSMiny)
					{
						xydet[0] = x_min;					
						xydet[1] = y_min;	
						tagPos[2] = 4;
					}
					else
					{
						System.out.println("进入解算，未解出有效结果");
						tagPos[2] = 0;
					}
				}
					

			}
			else
			{
				System.out.println("结果为复数！"+xydet[2]);
				tagPos[2] = 0;
			}		
			tagPos[0] = xydet[0];
			tagPos[1] = xydet[1];
			System.out.println("4bs location x:"+xydet[0]+"; y:"+xydet[1]);
			if(tagPos[2] == 4)
			{
				double[] caltPos = {tagPos[0],tagPos[1],0.4};
				
				for(i = 1; i < bsnum; i++)
					detR[i-1] = Math.abs(getVecDiff(caltPos,resortBSpos[i]) - getVecDiff(caltPos,resortBSpos[0]) - R[i-1]);
				
			}
			
			return tagPos;
	}

	
	 static double[] locationRLS(int bsnum, double[] R, double[][] resortBSpos, double z_assump, double[] detR) {
	        double[] tagPos = {0, 0, 0};
	        int BSnum = bsnum;
	        double[][] BSpos = resortBSpos;

	        if (BSnum > 4) {
	            double[] K = new double[BSnum];
	            int i, j;
	            for (i = 0; i < BSnum; i++) {
	                K[i] = Math.pow(BSpos[i][0], 2) + Math.pow(BSpos[i][1], 2) + Math.pow(BSpos[i][2], 2);
	            }

	            double[][] A = new double[BSnum - 1][3];
	            double[] b = new double[BSnum - 1];
	            for (i = 0; i < BSnum - 1; i++) {
	                A[i][0] = -(BSpos[i + 1][0] - BSpos[0][0]);
	                A[i][1] = -(BSpos[i + 1][1] - BSpos[0][1]);
	                A[i][2] = -R[i];
	                b[i] = 0.5 * (Math.pow(R[i], 2) - K[i + 1] + K[0] + 2 * (BSpos[i + 1][2] - BSpos[0][2]) * z_assump);
	            }
	            //double[][] pinvA = pinvMatrix3(A, BSnum - 1);
	            double[][] pinvA = pinvMatrix(A, BSnum - 1, 3);
	            double a1 = pinvA[0][0], a2 = pinvA[1][0], a3 = pinvA[2][0], b1 = pinvA[0][1], b2 = pinvA[1][1], b3 = pinvA[1][2], c1 = pinvA[0][2], c2 = pinvA[1][2], c3 = pinvA[2][2];
	            double detpA = a1 * (b2 * c3 - c2 * b3) - a2 * (b1 * c3 - c1 * b3) + a3 * (b1 * c2 - c1 * b2);
	            if (Math.abs(detpA) > 0.0000001) {
	                //pinv(A)*b
	                double[] ans = new double[3];

	                for (i = 0; i < 3; i++) {
	                    for (j = 0; j < BSnum - 1; j++)
	                        ans[i] += pinvA[i][j] * b[j];
	                }
	                tagPos[0] = ans[0];
	                tagPos[1] = ans[1];
	                tagPos[2] = bsnum;


	                double[] caltPos = {tagPos[0], tagPos[1], z_assump};

	                for (i = 1; i < bsnum; i++) {
	                    detR[i - 1] = Math.abs(getVecDiff(caltPos, resortBSpos[i]) - getVecDiff(caltPos, resortBSpos[0]) - R[i - 1]);
	                }


	                //此处为最终结果
	                System.out.println(bsnum + "基站解算：location: x = " + tagPos[0] + ", y = " + tagPos[1]);

	            } else {
	                System.out.println("病态矩阵！");
	            }
	        } else {
	            System.out.println("基站数量不足");
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

	double norm(double[] X2, double[] X1)
	{
		double norm = 0;
		int size = X1.length;

			for(int i = 0; i < size; i++)
			{
				norm += (X1[i] - X2[i])*(X1[i] - X2[i]);
			}
			norm = Math.sqrt(norm);
			return norm;


	}
	
	double getdiff(double x2, double x1)
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

	static double getVecDiff(double[] X1, double[] X2)
	{
		double norm = 0;
		int size = X1.length;
		if(size != X2.length)
			return 0;
		else
		{
			for(int i = 0; i < size; i++)
			{
				norm += (X1[i] - X2[i])*(X1[i] - X2[i]);
			}
			norm = Math.sqrt(norm);
			return norm;
		}

	}



}
