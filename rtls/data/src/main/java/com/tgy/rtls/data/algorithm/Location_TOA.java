package com.tgy.rtls.data.algorithm;

public class Location_TOA {
    static double pos[]= {0,0,0};
    static double BSposition[][];
    //static double[][] timeS[][];
    static double[] stat[];
    static double maxRdiff = 10;
    static double maxBoundDet = 10;
    double BSMinx, BSMaxx, BSMiny, BSMaxy;
    static double det_1D = 10;

    public static void main(String[] args) {
        double[] R = {15, 12, 0.1};
        long[] bsname = {1001632000, 1001632001, 1001632002};
        double[][] BSpos = {{0, 0, 1},
                {9,0, 1},
                {9, 12, 1}};
        double z_assump = 1;
        Location_TOA test = new Location_TOA();
        double[] postest = test.SINOPEC_location2D(R, bsname, BSpos, z_assump);
        System.out.println("x:" + postest[0] + "y:" + postest[1] + "z:" + postest[2]);

    }

    static double getDis(double timeStamp[], double anneDelayBS0, double anneDelayTag) {
        //鍒╃敤2涓熀绔欐潵杩涜鍥哄畾楂樺害鐨勫钩闈㈠畾浣�
        //timeStamp  涓篢ag_tx_timestamp,Bs_rx_timestamp,Bs_tx_timestamp,Tag_rx_timestamp
        //anneDelay 澶╃嚎寤舵椂  鍩虹珯鍜屾爣绛句笉鍚� 77.38


        double coef = 0.00469176397861579;

        double dT[] = new double[2];
        double dis1;
        dT[0] = getdiff(timeStamp[3], timeStamp[0]);
        dT[1] = getdiff(timeStamp[2], timeStamp[1]);
        dis1 = (dT[0] - dT[1]) * coef / 2 - anneDelayBS0 - anneDelayTag;
        return dis1;

    }

    static double[] location_8(int bsnum, long[] bsname, double timeStamp[], double T10, double BSpos[][], double anneDelay, double time_coef[], int state[]) {
        //8BS 绔嬩綋瀹氫綅

//		input: bsname涓�8涓熀绔橧D锛宼imeStamp涓�16涓椂闂存埑绗�2N鍜�2N+1涓厓绱犱负瀵瑰簲绗琋涓熀绔橧D鐨勬椂闂存埑T鍜宼锛屽墠鍏釜涓虹涓�涓诲熀绔欑殑鍖哄煙锛屽悗鍏釜涓虹浜屼釜涓诲熀绔欑殑鍖哄煙锛孴10涓轰富鍩虹珯M1鐩戝惉鍒癕0鐨剆ync淇″彿鏃禡1鑷繁鐨勬椂闂存埑T10锛孊Spos[N][3]涓轰笌bsname瀵瑰簲鐨勫熀绔檟,y,z
//		anneDelay涓哄ぉ绾垮欢鏃讹紝time_coef涓轰笌bsname瀵瑰簲鐨勬櫠鎸牎姝ｅ弬鏁帮紝鍏朵腑M0瀵瑰簲鐨勪负default鍊硷紝M1瀵瑰簲鐨勪负浠0涓哄弬鑰冪殑鏍℃鍙傛暟锛宻tate鏄熀绔欐帴鏀剁姸鎬�
//		output锛歱os[4]鍒嗗埆涓篬x,y,z,绮惧害鍥犲瓙];

        int minBSnum = 6;
        double xyzDet[] = new double[4];

        double ans[] = new double[6];
        double R[] = new double[7];
        double d[] = new double[8];

        int i, j;

        double dt[] = new double[8];
        if (bsnum < minBSnum) {
            return null;
        }

        for (i = 0; i < 8; i++) {
            dt[i] = getdiff(timeStamp[2 * i + 1], timeStamp[2 * i]);
        }

        time_coef[0] = 0.00469176397861579;


        for (i = 0; i < 3; i++) {
            d[i] = Math.sqrt(Math.pow(BSpos[i + 1][0] - BSpos[0][0], 2) + Math.pow(BSpos[i + 1][1] - BSpos[0][1], 2) + Math.pow(BSpos[i + 1][2] - BSpos[0][2], 2));
            R[i] = dt[i + 1] * time_coef[i + 1] - dt[0] * time_coef[0] + 2 * anneDelay + d[i];
        }
        double dTM10 = getdiff(timeStamp[9], T10);
        d[3] = Math.sqrt(Math.pow(BSpos[4][0] - BSpos[0][0], 2) + Math.pow(BSpos[4][1] - BSpos[0][1], 2) + Math.pow(BSpos[4][2] - BSpos[0][2], 2));
        R[3] = dTM10 * time_coef[4] - dt[0] * time_coef[0] + 2 * anneDelay + d[3];

        for (i = 4; i < 7; i++) {
            d[i] = Math.sqrt(Math.pow(BSpos[i + 1][0] - BSpos[4][0], 2) + Math.pow(BSpos[i + 1][1] - BSpos[4][1], 2) + Math.pow(BSpos[i + 1][2] - BSpos[4][2], 2));
            R[i] = dt[i + 1] * time_coef[i + 1] - dt[4] * time_coef[0] + 2 * anneDelay + d[i] + R[3];
        }


        double[][] BS = resortBS(BSpos, state);
        int BSnum = 0;

        for (i = 0; i < 8; i++) {
            if (state[i] == 1)

                BSnum++;
        }
        if (BSnum < 6) {
            System.out.println("鍩虹珯鏁伴噺灏忎簬6");
            return null;
        } else {
            double RT[] = new double[BSnum - 1];
            j = 0;
            for (i = 1; i < BSnum; i++) {
                if (state[i] == 1) {
                    RT[j] = R[i - 1];
                    j++;
                }
            }


            xyzDet = location3DChan(RT, BS, BSnum);
            if (xyzDet != null) {
                for (i = 0; i < 3; i++) {
                    ans[i] = xyzDet[i];
                }

                return ans;
            } else {
                return null;
            }

        }

    }

    static double[][] resortBS(double BSpos[][], int state[]) {
        double[][] BS = new double[4][3];
        int i, j, k;
        k = 0;
        for (i = 0; i < state.length; i++) {
            if (state[i] == 1) {
                for (j = 0; j < 3; j++) {
                    BS[k][j] = BSpos[i][j];
                }
                k++;
            }
        }
        return BS;
    }

    public static double getdiff(double x2, double x1) {
        double diff;
        if (x2 < 1 || x1 < 1 || x2 > 1099511627775d || x1 > 1099511627775d)
            return 0;
        else {
            if (x2 < x1) {
                x2 += 1099511627775d;
            }
            diff = x2 - x1;
            return diff;
        }

    }

    public static double[] location_4(int bsnum, long[] bsname, double timeStamp[], double BSpos[][], double anneDelay, double time_coef[], double z_assump) {
        //4BS 骞抽潰瀹氫綅

//		input: bsname涓�4涓熀绔橧D锛宼imeStamp涓�8涓椂闂存埑锛屾瘡涓熀绔欑殑涓や釜鏃堕棿鎴筹紝鎸塨sname椤哄簭鎺掑垪
//		BSpos[N][3]涓轰笌bsname瀵瑰簲鐨勫熀绔檟,y,z
//		anneDelay涓哄ぉ绾垮欢鏃讹紝
//		time_coef涓轰笌bsname瀵瑰簲鐨勬櫠鎸牎姝ｅ弬鏁�
//		z_assump涓哄亣璁剧殑鏍囩楂樺害
//		output锛歱os[3]鍒嗗埆涓篬x,y,det];

        int minBSnum = 4;
        double xyzDet[] = new double[4];

        double ans[] = new double[6];
        double R[] = new double[3];
        double d[] = new double[3];

        int i, j;

        double dt[] = new double[4];
        if (bsnum < minBSnum) {
            System.out.println("基站数据小于4");
            return null;
        }

        for (i = 0; i < 4; i++) {
            dt[i] = getdiff(timeStamp[2 * i + 1], timeStamp[2 * i]);
        }

        time_coef[0] = 0.00469176397861579;


        for (i = 0; i < 3; i++) {
            d[i] = Math.sqrt(Math.pow(BSpos[i + 1][0] - BSpos[0][0], 2) + Math.pow(BSpos[i + 1][1] - BSpos[0][1], 2) + Math.pow(BSpos[i + 1][2] - BSpos[0][2], 2));
            R[i] = dt[i + 1] * time_coef[i + 1] - dt[0] * time_coef[0] + 2 * anneDelay + d[i];
        }

        System.out.println("chan算法计算二维位置");
        xyzDet = locationChan2D(R, BSpos, bsnum, z_assump);
        if (xyzDet != null) {
            for (i = 0; i < 3; i++) {
                ans[i] = xyzDet[i];
            }
            ans[2] = 0;
            return ans;
        } else {
            System.out.println("chan算法计算位置为null");
            return null;
        }

    }


    public static double[] location2BS2D(double[] disC, double [][] BSpos, double z_assump, int dir) {
        //鍒╃敤2涓熀绔欐潵杩涜鍥哄畾楂樺害鐨勫钩闈㈠畾浣�
        //timeStamp[T0绗�1涓熀绔欏彂灏勭殑绗竴涓椂闂存埑  T1 鏍囩鏀跺埌淇″彿鐨勬椂闂存埑 T2 鏍囩鍙戝皠鐨勬椂闂存埑 T3 绗�1涓熀绔欐敹鍒颁俊鍙风殑鏃堕棿鎴� T4绗�2涓熀绔欏彂灏勭殑绗竴涓椂闂存埑  T5 鏍囩鏀跺埌淇″彿鐨勬椂闂存埑 T6 鏍囩鍙戝皠鐨勬椂闂存埑 T7 绗�2涓熀绔欐敹鍒颁俊鍙风殑鏃堕棿鎴� ]
        //BSpos涓哄熀绔欏潗鏍� 鐩墠绠楁硶涓婅姹侭Spos鐨剎鍜寊涓�鑷达紝鍙互鍚庢湡杩涜鍧愭爣杞崲
    	//dir 鏈夋晥瀹氫綅鍖哄煙涓轰粠bs0鎸囧悜bs1鐨勫熀绔欑殑鍚戦噺鐨勫乏渚�(0)鎴栬�呭彸渚�(1)
        //z_assump 鍋囧畾鐨勬爣绛鹃珮搴�
     //   Bs_inf bs0 = Constant.bsstation.get(bs0number);
      //  Bs_inf bs1 = Constant.bsstation.get(bs1number);
        double dis1 = disC[0];
        double dis2 = disC[1];


     //   System.out.println("bs0number:" + bs0number + "bs0number1" + bs1number + "dis1:" + dis1 + "dis2:" + dis2);

        double[][] BSnew = new double[2][3];
        BSnew[0][2] = BSpos[0][2];
        BSnew[1][1] = Math.sqrt(Math.pow(BSpos[0][0] - BSpos[1][0], 2) + Math.pow(BSpos[0][1] - BSpos[1][1], 2));
        BSnew[1][2] = BSpos[1][2];
    //    System.out.println("鍩虹珯鍧愭爣: [" + BSnew[0][0] + " " + BSnew[0][1] + "; " + BSnew[1][0] + " " + BSnew[1][1] + "] ");

        if(dis1 + dis2 <= BSnew[1][1]+1) {
        	double disBS = Math.sqrt(Math.pow(BSpos[0][0] - BSpos[1][0], 2) + Math.pow(BSpos[0][1] - BSpos[1][1], 2));

            if((dis1 + dis2) < (disBS + det_1D))
            {
                pos[0] = BSpos[1][0]+ 0.5 * (BSpos[0][0]-BSpos[1][0]) * (disBS + (dis2 - dis1))/ disBS;
                pos[1] = BSpos[1][1]+ 0.5 * (BSpos[0][1]-BSpos[1][1]) * (disBS + (dis2 - dis1))/ disBS;
            } else if(dis1 < dis2){
                pos[0] = BSpos[1][0]+ (BSpos[0][0]-BSpos[1][0]) * dis2 / disBS;
                pos[1] = BSpos[1][1]+ (BSpos[0][1]-BSpos[1][1]) * dis2 / disBS;
            } else{
                pos[0] = BSpos[1][0] - (BSpos[0][0]-BSpos[1][0]) * dis2 / disBS;
                pos[1] = BSpos[1][1] - (BSpos[0][1]-BSpos[1][1]) * dis2 / disBS;
            }

            pos[2] = z_assump;
     //       System.out.println("瀹氫綅缁撴灉锛�" + pos[0] + " " + pos[1] + " " + pos[2]);
            return pos;
        }else {
	        
	        double[] mat_B = new double[2];
	        mat_B[0] = BSpos[1][0] - BSpos[0][0];
	        mat_B[1] = BSpos[1][1] - BSpos[0][1];
	
	        double[][] mat_A = new double[2][2];
	        mat_A[0][0] = BSnew[1][0];
	        mat_A[0][1] = BSnew[1][1];
	        mat_A[1][0] = BSnew[1][1];
	        mat_A[1][1] = -BSnew[1][0];
	
	        double[][] invA = pinvMatrix(mat_A, 2, 2);
	        double[] transCoef = new double[2];
	        transCoef[0] = invA[0][0] * mat_B[0] + invA[0][1] * mat_B[1];
	        transCoef[1] = invA[1][0] * mat_B[0] + invA[1][1] * mat_B[1];
	        double[][] coorTrans = {{transCoef[0], transCoef[1]}, {-transCoef[1], transCoef[0]}};
	        double pos[] = new double[3];
	        double b, a, aa, bb, cc, x1, x2, y1, y2;
	        a = (Math.pow(BSnew[0][0], 2) - Math.pow(BSnew[1][0], 2) + Math.pow(BSnew[0][1], 2) - Math.pow(BSnew[1][1], 2) + Math.pow(z_assump - BSnew[0][2], 2) - Math.pow(z_assump - BSnew[1][2], 2) + dis2 * dis2 - dis1 * dis1) / (BSnew[0][1] - BSnew[1][1]) / 2;
	        b = -(BSnew[0][0] - BSnew[1][0]) / (BSnew[0][1] - BSnew[1][1]);
	        aa = 1 + b * b;
	        bb = 2 * b * (a - BSnew[0][1]) - 2 * BSnew[0][0];
	        cc = BSnew[0][0] * BSnew[0][0] + (a - BSnew[0][1]) * (a - BSnew[0][1]) + (z_assump - BSnew[0][2]) * (z_assump - BSnew[0][2]) - dis1 * dis1;
	
	        x1 = (-bb + Math.sqrt(bb * bb - 4 * aa * cc)) / (2 * aa);
	        x2 = (-bb - Math.sqrt(bb * bb - 4 * aa * cc)) / (2 * aa);
	        y1 = a + b * x1;
	        y2 = a + b * x2;
	 //       System.out.println("鏈浆鎹㈢殑瀹氫綅缁撴灉锛�"+x1+" "+y1);
	        if (x1 > 0) {
	        	if(dir == 1) {
		            pos[0] = coorTrans[0][0] * x1 + coorTrans[0][1] * y1 + BSpos[0][0];
		            pos[1] = coorTrans[1][0] * x1 + coorTrans[1][1] * y1 + BSpos[0][1];
		            pos[2] = z_assump;
		      //      System.out.println("瀹氫綅缁撴灉锛�" + pos[0] + " " + pos[1] + " " + pos[2]);
	        	}else {
		            pos[0] = coorTrans[0][0] * x2 + coorTrans[0][1] * y2 + BSpos[0][0];
		            pos[1] = coorTrans[1][0] * x2 + coorTrans[1][1] * y2 + BSpos[0][1];
		            pos[2] = z_assump;
		    //        System.out.println("瀹氫綅缁撴灉锛�" + pos[0] + " " + pos[1] + " " + pos[2]);
	        	}
	            return pos;
	        } else {
	            pos[0] = coorTrans[0][0] * x1 + coorTrans[0][1] * y1 + BSpos[0][0];
	            pos[1] = coorTrans[1][0] * x1 + coorTrans[1][1] * y1 + BSpos[0][1];
	            pos[2] = z_assump;
	         //   System.out.println("鏃犳寚瀹氬尯鍩熸湁鏁堣В锛屽畾浣嶇粨鏋滐細" + pos[0] + " " + pos[1] + " " + pos[2]);
	
	            return null;
	        }
        }
    }

    public double[] location1D(double[] disC, double BSpos[][], double z_assump) {
        //鍒╃敤2涓熀绔欐潵杩涜鍥哄畾楂樺害鐨勪竴缁村畾浣�
        //BSpos涓哄熀绔欏潗鏍� 鐩墠绠楁硶涓婅姹侭Spos鐨剎鍜寊涓�鑷达紝鍙互鍚庢湡杩涜鍧愭爣杞崲
        //z_assump 鍋囧畾鐨勬爣绛鹃珮搴�
        double dis1 = disC[0];
        double dis2 = disC[1];
        double pos[] = new double[3];

        double disBS = Math.sqrt(Math.pow(BSpos[0][0] - BSpos[1][0], 2) + Math.pow(BSpos[0][1] - BSpos[1][1], 2));
        det_1D = disBS/20;
        if(det_1D < 2)
        {
        	det_1D = 2;
        }
        if((dis1+dis2)>(disBS+det_1D))
        {
        	//System.out.println("dis sum bigger than 2， return null");
        	  return null;
        }
        if(dis1 < -1 || dis2 < -1 || dis1 > disBS + det_1D/4 || dis2 > disBS + det_1D/4)
        {
        	//System.out.println("dis invalid， return null");
        	return null;
        }
        if ((dis1 + dis2) < (disBS + det_1D)) {
        	if((dis1 > det_1D && dis2 > det_1D) || (dis1 + dis2) < (disBS + det_1D/2))
        	{
        		pos[0] = BSpos[1][0] + 0.5 * (BSpos[0][0] - BSpos[1][0]) * (disBS + (dis2 - dis1)) / disBS;
                pos[1] = BSpos[1][1] + 0.5 * (BSpos[0][1] - BSpos[1][1]) * (disBS + (dis2 - dis1)) / disBS;
                pos[2] = 1;
        	}
        } else if (dis1 < dis2) {
            pos[0] = BSpos[1][0] + (BSpos[0][0] - BSpos[1][0]) * dis2 / disBS;
            pos[1] = BSpos[1][1] + (BSpos[0][1] - BSpos[1][1]) * dis2 / disBS;
            pos[2] = 0.5;
        } else {
            pos[0] = BSpos[1][0] - (BSpos[0][0] - BSpos[1][0]) * dis2 / disBS;
            pos[1] = BSpos[1][1] - (BSpos[0][1] - BSpos[1][1]) * dis2 / disBS;
            pos[2] = 0.5;
        }


        //System.out.println("final out" + pos[0] + " " + pos[1] + " " + pos[2]);
        return pos;

    }


    static double[] location3DChan(double R[], double BSpos[][], int BSnum) {
        //鐪熉蜂笁缁村畾浣�
        double xyzDet[] = {0, 0, 0, 0};

        if (BSnum > 6) {
            double[] K = new double[BSnum];
            int i, j;
            for (i = 0; i < BSnum; i++) {
                K[i] = Math.pow(BSpos[i][0], 2) + Math.pow(BSpos[i][1], 2) + Math.pow(BSpos[i][2], 2);
            }

            double[] m = new double[3];
            for (i = 0; i < 3; i++) {
                m[i] = BSpos[0][i];
            }


            double[][] A = new double[BSnum - 1][3];
            double[] RK = new double[BSnum - 1];
            for (i = 0; i < BSnum - 1; i++) {
                A[i][0] = (BSpos[i + 1][0] - BSpos[0][0]);
                A[i][1] = (BSpos[i + 1][1] - BSpos[0][1]);
                A[i][2] = (BSpos[i + 1][2] - BSpos[0][2]);
                RK[i] = Math.pow(R[i], 2) - K[i + 1] + K[0];
            }
            double[][] pinvA = pinvMatrix(A, BSnum - 1, 3);

            double[] e = new double[3];
            double[] f = new double[3];

            for (i = 0; i < 3; i++) {
                for (j = 0; j < BSnum - 1; j++) {
                    e[i] -= pinvA[i][j] * R[j];
                    f[i] -= 0.5 * pinvA[i][j] * RK[j];
                }
            }

            double aa = 1 - e[0] * e[0] - e[1] * e[1] - e[2] * e[2];
            double bb = 2 * (m[0] * e[0] + m[1] * e[1] + m[2] * e[2] - (f[0] * e[0] + f[1] * e[1] + f[2] * e[2]));
            double cc = 2 * (m[0] * f[0] + m[1] * f[1] + m[2] * f[2]) - (f[0] * f[0] + f[1] * f[1] + f[2] * f[2]) - K[0];

            xyzDet[3] = bb * bb - 4 * aa * cc;

            if (xyzDet[3] >= 0) {

                double R0_max = (-bb + Math.sqrt(xyzDet[3])) / (2 * aa);
                double R0_min = (-bb - Math.sqrt(xyzDet[3])) / (2 * aa);
                double pinvA2[][] = pinvMatrix(A, BSnum - 1, 3);
                double x_max, x_min, y_max, y_min, z_max, z_min;
                double matB_max[][] = new double[BSnum - 1][1];
                double matB_min[][] = new double[BSnum - 1][1];
                for (i = 0; i < BSnum - 1; i++) {
                    matB_max[i][0] = R[i] * R0_max + 0.5 * RK[i];
                    matB_min[i][0] = R[i] * R0_min + 0.5 * RK[i];
                }

                double ans[][] = new double[2][1];
                ans = multMatrix(pinvA2, matB_max, 3, BSnum - 1, 1);
                x_max = -ans[0][0];
                y_max = -ans[1][0];
                z_max = -ans[2][0];

                ans = multMatrix(pinvA2, matB_min, 3, BSnum - 1, 1);
                x_min = -ans[0][0];
                y_min = -ans[1][0];
                z_min = -ans[2][0];

                if (R0_min <= 0) {
                    xyzDet[0] = x_max;
                    xyzDet[1] = y_max;
                    xyzDet[2] = z_max;
                } else {
                    if (x_max > 0 && y_max > 0) {
                        xyzDet[0] = x_max;
                        xyzDet[1] = y_max;
                        xyzDet[2] = z_max;
                    } else if (x_min > 0 && y_min > 0) {
                        xyzDet[0] = x_min;
                        xyzDet[1] = y_min;
                        xyzDet[2] = z_min;
                    } else {
                        return null;
                    }
                }

                //姝ゅ涓烘渶缁堢粨鏋�
                System.out.println("location: x = " + xyzDet[0] + ", y = " + xyzDet[1] + ", z = " + xyzDet[2]);
                return xyzDet;
            } else {
                System.out.println("缁撴灉涓哄鏁帮紒" + xyzDet[3]);
                return null;
            }
        } else {
            System.out.println("鍩虹珯鏁伴噺涓嶈冻");
            return null;
        }
    }


    static double[] locationChan2D(double R[], double BSpos[][], int BSnum, double z_assump) {
        //浜岀淮瀹氫綅锛屽熀绔欐病鏈夐珮搴﹀樊
        double xydet[] = {0, 0, 0};


        if (BSnum > 3) {
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
                } else {
                    if (x_max > 0 && y_max > 0) {
                        xydet[0] = x_max;
                        xydet[1] = y_max;
                    } else if (x_min > 0 && y_min > 0) {
                        xydet[0] = x_min;
                        xydet[1] = y_min;
                    } else {
                        System.out.println("瑙ｅ嚭缁撴灉瓒呰寖鍥�");
                        return null;
                    }
                }

                //姝ゅ涓烘渶缁堢粨鏋�
                System.out.println("location: x = " + xydet[0] + ", y = " + xydet[1] + ", z = " + xydet[2]);
                return xydet;
            } else {
                System.out.println("缁撴灉涓哄鏁帮紒" + xydet[2]);
                return null;
            }
        } else {
            System.out.println("鍩虹珯鏁伴噺涓嶈冻");
            return null;
        }
    }


    static double[][] pinvMatrix(double A[][], int rowNum, int colNum) {

        double[][] pinvA = new double[colNum][rowNum];
        double[][] ATA = new double[colNum][colNum];

        int i, j, k;
        for (i = 0; i < colNum; i++) {
            for (j = 0; j < colNum; j++) {
                for (k = 0; k < rowNum; k++) {
                    ATA[i][j] += A[k][i] * A[k][j];
                }
            }
        }

        // (ATA)^(-1)
        double detATA = 0;
        if (colNum == 3) {
            double a1 = ATA[0][0], a2 = ATA[1][0], a3 = ATA[2][0], b1 = ATA[0][1], b2 = ATA[1][1], b3 = ATA[1][2], c1 = ATA[0][2], c2 = ATA[1][2], c3 = ATA[2][2];
            detATA = a1 * (b2 * c3 - c2 * b3) - a2 * (b1 * c3 - c1 * b3) + a3 * (b1 * c2 - c1 * b2);
            if (Math.abs(detATA) > 0.0001) {
                double[][] invATA = new double[3][3];
                invATA[0][0] = (b2 * c3 - c2 * b3) / detATA;
                invATA[0][1] = (b3 * c1 - c3 * b1) / detATA;
                invATA[0][2] = (b1 * c2 - c1 * b2) / detATA;
                invATA[1][0] = (a3 * c2 - a2 * c3) / detATA;
                invATA[1][1] = (a1 * c3 - c1 * a3) / detATA;
                invATA[1][2] = (a2 * c1 - c2 * a1) / detATA;
                invATA[2][0] = (a2 * b3 - b2 * a3) / detATA;
                invATA[2][1] = (a3 * b1 - b3 * a1) / detATA;
                invATA[2][2] = (a1 * b2 - a2 * b1) / detATA;

                //pinv(A) = invATA * AT

                //printf("pinvA \n");

                for (i = 0; i < colNum; i++) {
                    for (j = 0; j < rowNum; j++) {
                        for (k = 0; k < colNum; k++) {
                            pinvA[i][j] += invATA[i][k] * A[j][k];
                        }
                    }
                }
            }
        } else if (colNum == 2) {
            double a1 = ATA[0][0], a2 = ATA[1][0], a3 = ATA[0][1], a4 = ATA[1][1];
            detATA = a1 * a4 - a2 * a3;
            if (Math.abs(detATA) > 0.0001) {
                double[][] invATA = new double[2][2];
                invATA[0][0] = a4 / detATA;
                invATA[0][1] = -a3 / detATA;
                invATA[1][0] = -a2 / detATA;
                invATA[1][1] = a1 / detATA;


                //pinv(A) = invATA * AT

                //printf("pinvA \n");
                for (i = 0; i < colNum; i++) {
                    for (j = 0; j < rowNum; j++) {
                        for (k = 0; k < colNum; k++) {
                            pinvA[i][j] += invATA[i][k] * A[j][k];
                        }
                    }
                }
            }
        } else {
            System.out.println("鐭╅樀鍒楁暟瓒呰繃3锛岃绠楀紑閿�杩囧ぇ");
        }

        return pinvA;
    }


    static double[][] multMatrix(double ML[][], double MR[][], int rowL, int col, int colR) {
        double res[][] = new double[rowL][colR];
        int i, j, k;
        for (i = 0; i < rowL; i++) {
            for (j = 0; j < colR; j++) {
                for (k = 0; k < col; k++) {
                    res[i][j] += ML[i][k] * MR[k][j];
                }
            }
        }
        return res;
    }


    double[] SINOPEC_location2D(double[] R, long[] bsname, double BSpos[][], double z_assump) {
        //鍒╃敤3涓熀绔欐潵杩涜鍥哄畾楂樺害鐨凾OA骞抽潰瀹氫綅
        //R_old 涓哄埌3涓熀绔欑殑璺濈 椤哄簭涓嶉檺 瀵瑰簲鍗冲彲
        //bsname 涓轰笁涓熀绔欑殑瀵瑰簲缂栧彿
        //BSpos_old涓哄熀绔欏搴斿潗鏍�
        //anneDelay 澶╃嚎寤舵椂  鍩虹珯鍜屾爣绛句笉鍚� default鍊间负 78.4
        //z_assump 鍋囧畾鐨勬爣绛鹃珮搴�
        if (bsname.length < 3) {
            System.out.println("基站数目小于3无法定位");
            return null;
        } else {

            for (int i = 0; i < R.length; i++) {
                System.out.println("基站编号 " + bsname[i] + " 距离: " + R[i] + "坐标: " + BSpos[i][0] + " " + BSpos[i][1] + "; " + BSpos[i][2] + "\n");
            }

            double[] pos;


            if (R != null) {
                if (R.length == 3) {
                    if (!BSisLine(bsname)) {
                        pos = TOA_3BS(R, BSpos, z_assump);
                    } else {
                        pos = TOA_2BS(R, BSpos, bsname, z_assump);
                    }
                    if (pos != null) {
                        System.out.println("瀹氫綅缁撴灉锛�" + pos[0] + " " + pos[1] + " " + pos[2]);
                    } else {
                        System.out.println("鏈В鍑烘纭粨鏋滐紒");
                    }
                    // todo 鍒ゆ柇瀹氫綅缁撴灉鏄惁鍚堟硶锛屽湪妗嗘灦浠ュ唴
                    // 鐩墠鐨勯�昏緫鏈夊彲鑳戒娇妗嗘灦澶栫殑鏍囩瀹氫綅鍒版鏋朵互鍐�
                    // 瑙ｅ喅鍔炴硶鍙互鏄湪鏀堕泦璺濈鏃讹紝鐩存帴鎺掗櫎鎺変竴鏉＄洿绾夸笂鐨勫熀绔欑粍鍚�
                    boundPos(pos, BSpos, bsname);
                    return pos;
                } else if (R.length == 4) {
                    double[] R_tdoa = {R[1] - R[0], R[2] - R[0], R[3] - R[0]};
                    pos = FreeWay_RLS.TDOA_chan2D(R_tdoa, BSpos, z_assump, BSMinx, BSMaxx, BSMaxy, BSMiny, 4);
                    if (pos[2] != 0) {
                        System.out.println("瀹氫綅缁撴灉锛�" + pos[0] + " " + pos[1] + " " + pos[2]);
                    } else {
                        System.out.println("鏈В鍑烘纭粨鏋滐紒");
                        pos = null;
                    }
                    // todo 鍒ゆ柇瀹氫綅缁撴灉鏄惁鍚堟硶锛屽湪妗嗘灦浠ュ唴
                    // 鐩墠鐨勯�昏緫鏈夊彲鑳戒娇妗嗘灦澶栫殑鏍囩瀹氫綅鍒版鏋朵互鍐�
                    // 瑙ｅ喅鍔炴硶鍙互鏄湪鏀堕泦璺濈鏃讹紝鐩存帴鎺掗櫎鎺変竴鏉＄洿绾夸笂鐨勫熀绔欑粍鍚�
                    boundPos(pos, BSpos, bsname);
                    return pos;
                } else {
                    return null;
                }
            } else
                return null;
        }
    }

    void getEdge(double[][] BSpos, long[] bsname) {
        int floor = whichFloor(bsname);

        BSMinx = BSpos[0][0];
        BSMaxx = BSpos[0][0];
        BSMiny = BSpos[0][1];
        BSMaxy = BSpos[0][1];

        for (int i = 1; i < BSpos.length; i++) {
            if (BSMinx > BSpos[i][0])
                BSMinx = BSpos[i][0];
            if (BSMaxx < BSpos[i][0])
                BSMaxx = BSpos[i][0];
            if (BSMiny > BSpos[i][1])
                BSMiny = BSpos[i][1];
            if (BSMaxy < BSpos[i][1])
                BSMaxy = BSpos[i][1];
        }
        if (floor == 0) {
            BSMaxx += maxBoundDet;
            BSMaxy += maxBoundDet;
            BSMinx -= maxBoundDet;
            BSMiny -= maxBoundDet;
        }
    }

    void boundPos(double[] pos, double[][] BSpos, long[] bsname) {
        int floor = whichFloor(bsname);

        if (pos[0] < BSMinx - maxBoundDet || pos[1] > BSMaxy + maxBoundDet || pos[1] < BSMiny - maxBoundDet || pos[0] > BSMaxx + maxBoundDet) {
            pos = null;
        }
  /*      if (pos != null) {
            if (pos[0] < BSMinx) {
                pos[0] = BSMinx;
            }
            if (pos[0] > BSMaxx) {
                pos[0] = BSMaxx;
            }
            if (pos[1] < BSMiny) {
                pos[1] = BSMiny;
            }
            if (pos[1] > BSMaxy) {
                pos[1] = BSMaxy;
            }
        }*/


    }

    static int whichFloor(long[] bsname) {
        int flag = ((int) bsname[0] / 10) % 10;
        switch (flag) {
            case 0:
                return 0;
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 1;
            default:
                return 0;
        }
    }

    static boolean isleft(long[] bsname) {
        int flag = ((int) bsname[0] / 10) % 10;

        switch (flag) {
            case 0:
                return subisleft(bsname, true);
            case 1:
                return subisleft(bsname, false);
            case 2:
                return subisleft(bsname, true);
            case 3:
                return subisleft(bsname, false);
            default:
                return false;
        }
    }

    static boolean subisleft(long[] bsname, boolean bsleft) {
        int sum;
        sum = ((int) bsname[0] % 10) + ((int) bsname[1] % 10);
        boolean flag = true;
        switch (sum) {
            case 1:
                flag = true;
                break;
            case 2:
                flag = false;
                break;
            case 3:
                flag = false;
                break;
        }
        if (!bsleft) {
            flag = !flag;
        }
        return flag;
    }

    static boolean BSisLine(long[] bsname) {
        long[] line = new long[bsname.length];
        for (int i = 0; i < bsname.length; i++) {
            line[i] = (bsname[i] / 10) % 10;
        }
        if (line[0] == line[1] && line[0] == line[2]) {
            return true;
        } else {
            return false;
        }
    }



    static double[] TOA_2BS(double[] R_old, double BSpos_old[][], long[] bsname, double z_assump) {
        //鍒╃敤2涓熀绔欐潵杩涜鍥哄畾楂樺害鐨勮繃鏉嗗紡骞抽潰瀹氫綅锛屽叾浣欎竴涓熀绔欏畾鍏蜂綋鏂逛綅
        //R_old 涓哄埌3涓熀绔欑殑璺濈 椤哄簭涓嶉檺 瀵瑰簲鍗冲彲
        //bsname 涓轰笁涓熀绔欑殑瀵瑰簲缂栧彿
        //BSpos_old涓哄熀绔欏搴斿潗鏍�
        //isleft 鏄惁涓哄満鍦板乏渚х殑鍩虹珯
        //z_assump 鍋囧畾鐨勬爣绛鹃珮搴�

        //鑾峰彇鏈夋晥璺濈
        int[] index = {0, 0, 0};
        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 3; j++) {
                if (j != i) {
                    if (R_old[i] >= R_old[j]) {
                        index[i]++;
                    }
                }
            }
        }
        int bsnum = 0;
        double[] R = new double[2];
        long[] bsNo = new long[2];
        double[][] BSpos = new double[2][3];
        for (int i = 0; i < 3; i++) {
            if (index[i] > 0 && bsnum < 2) {
                R[bsnum] = R_old[i];
                bsNo[bsnum] = bsname[i];
                System.arraycopy(BSpos_old[i], 0, BSpos[bsnum], 0, 3);
                bsnum++;
            }
        }

        double dis1, dis2;
        double[][] BSnew = new double[2][3];
        if (bsNo[0] < bsNo[1]) {
            dis1 = R[0];
            dis2 = R[1];
        } else {
            dis1 = R[1];
            dis2 = R[0];
            double[] tempbs = BSpos[0];
            BSpos[0] = BSpos[1];
            BSpos[1] = tempbs;
        }

        BSnew[0][2] = BSpos[0][2];
        BSnew[1][1] = Math.sqrt(Math.pow(BSpos[0][0] - BSpos[1][0], 2) + Math.pow(BSpos[0][1] - BSpos[1][1], 2));
        BSnew[1][2] = BSpos[1][2];

        double[] mat_B = new double[2];
        mat_B[0] = BSpos[1][0] - BSpos[0][0];
        mat_B[1] = BSpos[1][1] - BSpos[0][1];

        double[][] mat_A = new double[2][2];
        mat_A[0][0] = BSnew[1][0];
        mat_A[0][1] = BSnew[1][1];
        mat_A[1][0] = BSnew[1][1];
        mat_A[1][1] = -BSnew[1][0];

        double[][] invA = pinvMatrix(mat_A, 2, 2);
        double[] transCoef = new double[2];
        transCoef[0] = invA[0][0] * mat_B[0] + invA[0][1] * mat_B[1];
        transCoef[1] = invA[1][0] * mat_B[0] + invA[1][1] * mat_B[1];
        double[][] coorTrans = {{transCoef[0], transCoef[1]}, {-transCoef[1], transCoef[0]}};

        double b, a, aa, bb, cc, x1, x2, y1, y2;
        a = (Math.pow(BSnew[0][0], 2) - Math.pow(BSnew[1][0], 2) + Math.pow(BSnew[0][1], 2) - Math.pow(BSnew[1][1], 2) + Math.pow(z_assump - BSnew[0][2], 2) - Math.pow(z_assump - BSnew[1][2], 2) + dis2 * dis2 - dis1 * dis1) / (BSnew[0][1] - BSnew[1][1]) / 2;
        b = -(BSnew[0][0] - BSnew[1][0]) / (BSnew[0][1] - BSnew[1][1]);
        aa = 1 + b * b;
        bb = 2 * b * (a - BSnew[0][1]) - 2 * BSnew[0][0];
        cc = BSnew[0][0] * BSnew[0][0] + (a - BSnew[0][1]) * (a - BSnew[0][1]) + (z_assump - BSnew[0][2]) * (z_assump - BSnew[0][2]) - dis1 * dis1;

        double det = bb * bb - 4 * aa * cc;
        if (det < 0) {
            double disSum = dis1 + dis2;
            double disThreshhold = BSnew[1][1] + 2 * (BSnew[0][2] - z_assump);
            if (disSum < disThreshhold) {
                double k = dis1 / (disSum * BSnew[1][1]);
                for (int i = 0; i < 2; i++) {
                    pos[i] = k * (BSpos[1][i] - BSpos[0][i]) + BSpos[0][i];
                }
                pos[2] = z_assump;
                System.out.println("2BS location: x " + pos[0] + " y " + pos[1]);
                return pos;
            } else {
                return null;
            }
        }
        else {
            x1 = (-bb + Math.sqrt(det)) / (2 * aa);
            x2 = (-bb - Math.sqrt(det)) / (2 * aa);
            y1 = a + b * x1;
            y2 = a + b * x2;
            double[] pos = new double[3];
            if (isleft(bsNo)) {
                pos[0] = coorTrans[0][0] * x1 + coorTrans[0][1] * y1 + BSpos[0][0];
                pos[1] = coorTrans[1][0] * x1 + coorTrans[1][1] * y1 + BSpos[0][1];
                pos[2] = z_assump;
            } else {
                pos[0] = coorTrans[0][0] * x2 + coorTrans[0][1] * y2 + BSpos[0][0];
                pos[1] = coorTrans[1][0] * x2 + coorTrans[1][1] * y2 + BSpos[0][1];
                pos[2] = z_assump;
            }
            System.out.println("2BS location: x " + pos[0] + " y " + pos[1]);
            return pos;
        }
    }


    static double[] TOA_3BS(double[] R, double[][] BSpos, double z_assump) {
        double[] xyz = new double[3];
        double K1, K2, K3;

        double a11, a12;
        double a21, a22;
        double b11, b21;
        double b12, b22;

        double XConstant, YConstant;
        double XCoefficient, YCoefficient;
        double AVariableZ, BVariableZ, CVariableZ;

        K1 = BSpos[0][0] * BSpos[0][0] + BSpos[0][1] * BSpos[0][1] + BSpos[0][2] * BSpos[0][2];
        K2 = BSpos[1][0] * BSpos[1][0] + BSpos[1][1] * BSpos[1][1] + BSpos[1][2] * BSpos[1][2];
        K3 = BSpos[2][0] * BSpos[2][0] + BSpos[2][1] * BSpos[2][1] + BSpos[2][2] * BSpos[2][2];

        a11 = -2 * (BSpos[1][0] - BSpos[0][0]);
        a12 = -2 * (BSpos[1][1] - BSpos[0][1]);
        a21 = -2 * (BSpos[2][0] - BSpos[0][0]);
        a22 = -2 * (BSpos[2][1] - BSpos[0][1]);

        b11 = R[1] * R[1] - R[0] * R[0] - K2 + K1;
        b21 = R[2] * R[2] - R[0] * R[0] - K3 + K1;

        b12 = 2 * (BSpos[1][2] - BSpos[0][2]);
        b22 = 2 * (BSpos[2][2] - BSpos[0][2]);


        XConstant = (a22 * b11) / (a11 * a22 - a12 * a21) - (a12 * b21) / (a11 * a22 - a12 * a21) - BSpos[0][0];
        YConstant = (a11 * b21) / (a11 * a22 - a12 * a21) - (a21 * b11) / (a11 * a22 - a12 * a21) - BSpos[0][1];
        XCoefficient = (a22 * b12) / (a11 * a22 - a12 * a21) - (a12 * b22) / (a11 * a22 - a12 * a21);
        YCoefficient = (a11 * b22) / (a11 * a22 - a12 * a21) - (a21 * b12) / (a11 * a22 - a12 * a21);

        AVariableZ = XCoefficient * XCoefficient + YCoefficient * YCoefficient + 1;
        BVariableZ = 2 * XConstant * XCoefficient + 2 * YConstant * YCoefficient - 2 * BSpos[0][2];
        CVariableZ = XConstant * XConstant + YConstant * YConstant + BSpos[0][2] * BSpos[0][2] - R[0] * R[0];

        double BB4AC;

        BB4AC = (BVariableZ * BVariableZ - 4 * AVariableZ * CVariableZ);
        if (BB4AC < 0) {
            xyz[2] = (-BVariableZ) / (2 * AVariableZ);
        } else {

            xyz[2] = (-BVariableZ - Math.sqrt(BVariableZ * BVariableZ - 4 * AVariableZ * CVariableZ)) / (2 * AVariableZ);
        }

        xyz[0] = XConstant + BSpos[0][0] + XCoefficient * xyz[2];
        xyz[1] = YConstant + BSpos[0][1] + YCoefficient * xyz[2];
        xyz[2] = z_assump;

        if (Math.abs(getVecDiff(xyz, BSpos[0]) - R[0]) < maxRdiff) {
            System.out.println("3BS location: x " + xyz[0] + " y " + xyz[1]);
            return xyz;
        } else {
            System.out.println("瑙ｇ畻缁撴灉涓庢祴閲忓�间笉绗�: diff R " + Math.abs(getVecDiff(xyz, BSpos[0]) - R[0]));
            return null;
        }
    }

    static double getVecDiff(double[] X1, double[] X2) {
        double norm = 0;
        int size = X1.length;
        if (size != X2.length)
            return 0;
        else {
            for (int i = 0; i < size; i++) {
                norm += (X1[i] - X2[i]) * (X1[i] - X2[i]);
            }
            norm = Math.sqrt(norm);
            return norm;
        }

    }

    static long getPartner(long bsname) {
        long partner;
        if (bsname % 10 == 0 || bsname % 10 == 2)
            partner = bsname + 1;
        else if (bsname % 10 == 1 || bsname % 10 == 3)
            partner = bsname - 1;
        else
            partner = 0;
        return partner;
    }

    static int[] getResortIdx(long[] bsname) {
        int[] idx = new int[bsname.length];
        long partner = getPartner(bsname[0]);
        if (bsname[1] == partner) {
            if (bsname[0] % 10 == 0 || bsname[0] % 10 == 3) {
                idx[0] = 0;
                idx[1] = 1;
                idx[2] = 2;
            } else {
                idx[0] = 1;
                idx[1] = 0;
                idx[2] = 2;
            }
        } else if (bsname[2] == partner) {
            if (bsname[0] % 10 == 0 || bsname[0] % 10 == 3) {
                idx[0] = 0;
                idx[1] = 2;
                idx[2] = 1;
            } else {
                idx[0] = 2;
                idx[1] = 0;
                idx[2] = 1;
            }
        } else {
            partner = getPartner(bsname[1]);
            if (bsname[2] == partner) {
                if (bsname[1] % 10 == 0 || bsname[1] % 10 == 3) {
                    idx[0] = 1;
                    idx[1] = 2;
                    idx[2] = 0;
                } else {
                    idx[0] = 2;
                    idx[1] = 1;
                    idx[2] = 0;
                }
            } else {
                idx = null;
            }
        }
        return idx;
    }

    static double[] resortIndex(double[] R, int[] idx) {

        if (R.length == idx.length) {
            double[] R_new = new double[R.length];
            for (int i = 0; i < idx.length; i++) {
                R_new[i] = R[idx[i]];
            }
            return R_new;
        } else
            return null;
    }

    static double[][] resortIndex(double[][] R, int[] idx) {

        if (R.length == idx.length) {
            double[][] R_new = new double[R.length][R[0].length];
            for (int i = 0; i < idx.length; i++) {
                for (int j = 0; j < R[0].length; j++)
                    R_new[i][j] = R[idx[i]][j];
            }
            return R_new;
        } else
            return null;
    }


}

