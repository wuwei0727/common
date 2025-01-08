package com.tgy.rtls.data.algorithm;

public class Location_football_tw {
/*
	public	static  ConcurrentHashMap<Long,Bs_inf> bsstation=new ConcurrentHashMap<Long , Bs_inf>();
	public	static  ConcurrentHashMap<Long,Bs_inf> bsstation1=new ConcurrentHashMap<Long , Bs_inf>();
*/

	double coef = 0.00469176397861579;
	double[] time_coef;
	double anneDelay = 77.81;
	double[] bsDelay;
	int bsnum;
	double[][] resortBSpos;
	double[] resortTS;
	double[] R;
	double det = 5;
	double devR = 5;
	double[] tagPos = {0,0,0};
	double[] Ref = {0,0,0};
	double[] detR ;
	double MaxRDiff = 10;
public static	double AssumptionZ = 0.4;
	double BSMinx , BSMiny ;
	double BSMaxx , BSMaxy ; 
	int max_bsnum;
	
/*
	public double[] location(long tagid,long[] bsname,double[] timestamp, double[] coef_tmp, double[] delay_tmp,Boolean containmbs)
	{
		max_bsnum = coef_tmp.length;
		System.out.print(":containmbs:"+containmbs);
		double[][] BSpos= new double[max_bsnum][3];
		Bs_inf mbslist = bsstation.get(bsname[0]);
		long[] bsarraycom=new long[max_bsnum];
		String sortlist="";
		for(int i=0;i<max_bsnum;i++) 
		{
			bsarraycom[i]=bsname[i];
			if(i==0)
			sortlist=sortlist+bsname[i];
			else
			sortlist=sortlist+":"+bsname[i];	
			coef_tmp[i]=0.00469176397861579;
		}
		Arrays.sort(bsarraycom);
		
		
       for(int  i=0;i<max_bsnum;i++)
       {
    	   Bs_inf bs=bsstation.get(bsname[i]);
    	   BSpos[i][0]=bs.x;
    	   BSpos[i][1]=bs.y;
    	   BSpos[i][2]=bs.z;
    	  Double antennadelay= (Double)mbslist.antennadelaylist.get(bsname[i]);
    	  if(antennadelay!=null)
    	  { 
    		  delay_tmp[i]=antennadelay;
    	     // System.out.print("antennadelay:"+antennadelay);
    	  }else 
    	  {
    		  mbslist.antennadelaylist.put(bsname[i], 77.81);
    		  delay_tmp[i]=77.81;
    	  }
    	   
       }
    		
		for(int i=0;i<max_bsnum;i++) 
		{
			System.out.print(":bsid:"+bsname[i]+":T:"+timestamp[i*2]+":t:"+timestamp[i*2+1]+":coef:"+coef_tmp[i]+"antennadelay:"+delay_tmp[i]);
			
		}
		//Bs_inf[] arraay;
	*//*	Bs_inf mbs=bsstation.get(bsname[0]);
		Bs_inf bs1=bsstation.get(bsname[1]);
		Bs_inf bs2=bsstation.get(bsname[2]);
		Bs_inf bs3=bsstation.get(bsname[3]);
*//*
	*//*	System.out.print("mbs"+mbs.x);
		System.out.print("bs1"+bs1.x);
		System.out.print("bs2"+bs2.x);
		System.out.print("bs3"+bs3.x);*//*
		
	
   
		
	//	max_bsnum = bsname.length;
		double[] timeSdev = new double[max_bsnum];

		for(int i = 0; i < max_bsnum; i ++)
		{
			timeSdev[i] = getdiff(timestamp[2*i+1], timestamp[2*i]);
		}

		getEdge(BSpos);
		getValidData(timeSdev, BSpos,coef_tmp, delay_tmp,containmbs,tagid,bsname);
		
		

		double[][] squareBSpos = getSquBS();
		if(invalidRes(BSpos) || notSameRegion(squareBSpos) || validRefR() || tagPos[2] == 0)
		{
			for(int i = 0; i < 3; i++)
				tagPos[i] = 0;
			 tag_timestamp taginf=firstweb.tag.get(tagid);
		       if(taginf!=null&&taginf.antennadelayfix.size()>0&&R.length==(max_bsnum-1)&&(max_bsnum==4||max_bsnum==6))
			  {     
		    	   antennadelayfix_pos fixpos = null;
		    	   String key = null;
		    	   for(Entry<String, antennadelayfix_pos> entity:taginf.antennadelayfix.entrySet()) 
		    	   {
		    		   fixpos=entity.getValue();
		    		   key=entity.getKey();
		    		   break;
		    	   }
		    	   
		    	    double x_real=fixpos.x_real;
					double y_real=fixpos.y_real;
					double z_real=fixpos.z_real;
		    	   
		    	   for(int i=1;i<max_bsnum;i++)
			       {
			          double R01=Math.sqrt(Math.pow(BSpos[i][0]-x_real, 2)+Math.pow(BSpos[i][1]-y_real, 2)+Math.pow(BSpos[i][2]-z_real, 2))- 
			        		  Math.sqrt(Math.pow(BSpos[0][0]-x_real, 2)+Math.pow(BSpos[0][1]-y_real, 2)+Math.pow(BSpos[0][2]-z_real, 2));
			        
			          double antennadelay=(R01-R[i-1])/2;
			          System.out.println(tagid+"in realx"+x_real+"realy"+y_real+"realz"+z_real);
			          System.out.println("antennadelay fix mbs:"+bsname[0]+":slavebs:"+bsname[i]+":R01:"+R01+":fix before:"+delay_tmp[i]+":fix after:"+(delay_tmp[i]+antennadelay));
			    
			          oneantennadelaylist antennadelayone = mbslist.manyantennadelaylist.get(key);
			           if(antennadelayone==null) 
			           {
			        	   antennadelayone=new oneantennadelaylist();
			        	   mbslist.manyantennadelaylist.put(key,antennadelayone);
			           }
			           
			            if(!antennadelayone.containbs0bs1.containsKey(sortlist))
			        	   {
			        		   antennadelayone.containbs0bs1.put(sortlist, sortlist);
			        		   fixpos.fixcount++;
			        	   }
			         
			           System.out.println(key+"........fixpos.fixcount"+fixpos.fixcount);
			           if(!antennadelayone.oneantennadelaylist.containsKey(bsname[i]))
			        	   antennadelayone.oneantennadelaylist.put(bsname[i], delay_tmp[i]+antennadelay);
			           else
			        	   antennadelayone.oneantennadelaylist.replace(bsname[i], delay_tmp[i]+antennadelay);
			       }
		    	      
		    	   
		    	   for(int k=0;k<firstweb.bslist.length;k++) 
	    	       {
	    	    	   long bsid=firstweb.bslist[k];
	    	    	   if(bsid!=0&&bsid!=bsname[0]) 
	    	    	   {  double sum=0;
	    	    	     int indexx=0;
	    	    		   for( Entry<String, oneantennadelaylist> entry: mbslist.manyantennadelaylist.entrySet()) 
	    		    	   {
	    	    			 String poskey=entry.getKey();
	    	    			 oneantennadelaylist postantennalist=entry.getValue();
	    	    			 	   for(Entry<Long, Double> entity:postantennalist.oneantennadelaylist.entrySet()) 
		    		    		 {
		    		    			 long sbsid=entity.getKey();
		    		    			 double sbsantennadelay=entity.getValue();
		    		    			 if(bsid==sbsid) 
		    		    			 {
		    		    				 sum=sum+sbsantennadelay;
		    		    				 System.out.println("fix pos "+poskey+"syn mbs:"+bsname[0]+"slavebs:"+bsid+"delay:"+sbsantennadelay);
		    		    				 indexx++;
		    		    				 
		    		    			
		    		    		 }
	    	    			   }
	    		    	   }
	    	    		   if(indexx!=0) 
	    	    		   {
	    	    			   double aver=sum/indexx;
	    	    			   System.out.println("fix aver "+aver);
	    	    			   if(mbslist.antennadelaylist.containsKey(bsid))
	    	    			   mbslist.antennadelaylist.replace(bsid, aver);
	    	    			   else
	    	    			   mbslist.antennadelaylist.put(bsid, aver);	   
	    	    		   }
	    	    	   }
	    	       }
		    	   
		    	   
		    	   String data= bsname[0]+"           "+mbslist.getport()+"             "+mbslist.x+"              "+mbslist.y+"            "+mbslist.z+"      "+mbslist.area+" "+"0";
		    	       
		    	        for(int k=0;k<firstweb.bslist.length;k++) 
		    	       {
		    	    	   long bsid=firstweb.bslist[k];
		    	    	   if(bsid!=0&&bsid!=bsname[0]) 
		    	    	   {
		    	    		   data=data+":"+mbslist.antennadelaylist.get(bsid);
		    	    	   }
		    	       }
		    	   firstweb.replaceTxtByLineNo("D:\\bsinf.txt", bsname[0]+"", data, 0);
		    	   int fixcount=6;
		    	   if(max_bsnum==6)
		    		   fixcount=6;
		    	   else if(max_bsnum==4)
		    		   fixcount=6;
		    	   
		    	   if(fixpos.fixcount==fixcount)
		    	   {
		    		   taginf.antennadelayfix.remove(key,fixpos);
		    	   }
		    	 
		    	 
			    	
		    	   tagPos=locationre(tagid, bsname, timestamp, coef_tmp, delay_tmp, containmbs);
		       }
			return tagPos;
		}
		else
		{
			//tagPos[2] = AssumptionZ;
		    tag_timestamp taginf=firstweb.tag.get(tagid);
		       if(taginf!=null&&taginf.antennadelayfix.size()>0&&R.length==(max_bsnum-1)&&(max_bsnum==4||max_bsnum==6))
			  {     
		    	   antennadelayfix_pos fixpos = null;
		    	   String key = null;
		    	   for(Entry<String, antennadelayfix_pos> entity:taginf.antennadelayfix.entrySet()) 
		    	   {
		    		   fixpos=entity.getValue();
		    		   key=entity.getKey();
		    		   break;
		    	   }
		    	   
		    	    double x_real=fixpos.x_real;
					double y_real=fixpos.y_real;
					double z_real=fixpos.z_real;
		    	   
		    	   for(int i=1;i<max_bsnum;i++)
			       {
			          double R01=Math.sqrt(Math.pow(BSpos[i][0]-x_real, 2)+Math.pow(BSpos[i][1]-y_real, 2)+Math.pow(BSpos[i][2]-z_real, 2))- 
			        		  Math.sqrt(Math.pow(BSpos[0][0]-x_real, 2)+Math.pow(BSpos[0][1]-y_real, 2)+Math.pow(BSpos[0][2]-z_real, 2));
			        
			          double antennadelay=(R01-R[i-1])/2;
			          System.out.println(tagid+"in realx"+x_real+"realy"+y_real+"realz"+z_real);
			          System.out.println("antennadelay fix mbs:"+bsname[0]+":slavebs:"+bsname[i]+":R01:"+R01+":fix before:"+delay_tmp[i]+":fix after:"+(delay_tmp[i]+antennadelay));
			    
			          oneantennadelaylist antennadelayone = mbslist.manyantennadelaylist.get(key);
			           if(antennadelayone==null) 
			           {
			        	   antennadelayone=new oneantennadelaylist();
			        	   mbslist.manyantennadelaylist.put(key,antennadelayone);
			           }
			           
			            if(!antennadelayone.containbs0bs1.containsKey(sortlist))
			        	   {
			        		   antennadelayone.containbs0bs1.put(sortlist, sortlist);
			        		   fixpos.fixcount++;
			        	   }
			         
			           System.out.println(key+"........fixpos.fixcount"+fixpos.fixcount);
			           antennadelayone.oneantennadelaylist.put(bsname[i], delay_tmp[i]+antennadelay);
			
			           
			       }
		    	      
		    	   
		    	   for(int k=0;k<firstweb.bslist.length;k++) 
	    	       {
	    	    	   long bsid=firstweb.bslist[k];
	    	    	   if(bsid!=0&&bsid!=bsname[0]) 
	    	    	   {  double sum=0;
	    	    	     int indexx=0;
	    	    		   for( Entry<String, oneantennadelaylist> entry: mbslist.manyantennadelaylist.entrySet()) 
	    		    	   {
	    	    			 String poskey=entry.getKey();
	    	    			 oneantennadelaylist postantennalist=entry.getValue();
	    	    			 	   for(Entry<Long, Double> entity:postantennalist.oneantennadelaylist.entrySet()) 
		    		    		 {
		    		    			 long sbsid=entity.getKey();
		    		    			 double sbsantennadelay=entity.getValue();
		    		    			 if(bsid==sbsid) 
		    		    			 {
		    		    				 sum=sum+sbsantennadelay;
		    		    				 System.out.println("fix pos "+poskey+"syn mbs:"+bsname[0]+"slavebs:"+bsid+"delay:"+sbsantennadelay);
		    		    				 indexx++;
		    		    				 
		    		    			
		    		    		 }
	    	    			   }
	    		    	   }
	    	    		   if(indexx!=0) 
	    	    		   {
	    	    			   double aver=sum/indexx;
	    	    			   System.out.println("fix aver "+aver);
	    	    			   if(mbslist.antennadelaylist.containsKey(bsid))
	    	    			   mbslist.antennadelaylist.replace(bsid, aver);
	    	    			   else
	    	    			   mbslist.antennadelaylist.put(bsid, aver);	   
	    	    		   }
	    	    	   }
	    	       }
		    	   
		    	   
		    	   String data= bsname[0]+"           "+mbslist.getport()+"             "+mbslist.x+"              "+mbslist.y+"            "+mbslist.z+"      "+mbslist.area+" "+"0";
		    	       
		    	        for(int k=0;k<firstweb.bslist.length;k++) 
		    	       {
		    	    	   long bsid=firstweb.bslist[k];
		    	    	   if(bsid!=0&&bsid!=bsname[0]) 
		    	    	   {
		    	    		   data=data+":"+mbslist.antennadelaylist.get(bsid);
		    	    	   }
		    	       }
		    	   firstweb.replaceTxtByLineNo("D:\\bsinf.txt", bsname[0]+"", data, 0);
		    	   int fixcount=6;
		    	   if(max_bsnum==6)
		    		   fixcount=6;
		    	   else if(max_bsnum==4)
		    		   fixcount=6;
		    	   
		    	   if(fixpos.fixcount==fixcount)
		    	   {
		    		   taginf.antennadelayfix.remove(key,fixpos);
		    	   }
		    	 
			    	
		    	   tagPos=locationre(tagid, bsname, timestamp, coef_tmp, delay_tmp, containmbs);
		       }
			return tagPos;
		}
		
	}
	
	void setRtoTag(long tagid,long[] bsname,double[] dis) {
		
		System.out.println("tagid"+tagid+" ----location res not null");
		 tag_timestamp	taginf=firstweb.outtag.get(tagid);
		 if(taginf==null)
			 return;
		 int len=bsname.length;
		 taginf.tdoa.clear();
		 for(int i=1;i<len;i++) {
				System.out.println("tagid"+tagid+"::"+bsname[0]+":"+bsname[i]+":::diff::"+dis[i-1]+"");
			 taginf.tdoa.put(bsname[0]+":"+bsname[i],dis[i-1]+"");
			 
		 }
		
	}*/
	
/*	public double[] locationre(long tagid,long[] bsname,double[] timestamp, double[] coef_tmp, double[] delay_tmp,Boolean containmbs)
	{
		max_bsnum = coef_tmp.length;
		double[][] BSpos= new double[4][3];
		for(int i=0;i<max_bsnum;i++)
		{
			System.out.print(":bsid:"+bsname[i]+":T:"+timestamp[i*2]+":t:"+timestamp[i*2+1]+":coef:"+coef_tmp[i]);

		}





	//	max_bsnum = bsname.length;
		double[] timeSdev = new double[max_bsnum];

		for(int i = 0; i < max_bsnum; i ++)
		{
			timeSdev[i] = getdiff(timestamp[2*i+1], timestamp[2*i]);
		}

		getEdge(BSpos);
		getValidData(timeSdev, BSpos,coef_tmp, delay_tmp,containmbs,tagid,bsname);
		
		

		double[][] squareBSpos = getSquBS();
		if(invalidRes(BSpos) || notSameRegion(squareBSpos) || validRefR() || tagPos[2] == 0)
		{
			for(int i = 0; i < 3; i++)
				tagPos[i] = 0;
			return null;
		}
		else
		{
			//tagPos[2] = AssumptionZ;
			return tagPos;
		}
	}*/
	
	double[][] getSquBS()
	{
		double[][] squareBSpos = new double[4][2];
		squareBSpos[0][0] = BSMinx;
		squareBSpos[0][1] = BSMaxy;
		
		squareBSpos[1][0] = BSMaxx;
		squareBSpos[1][1] = BSMaxy;
		
		squareBSpos[2][0] = BSMaxx;
		squareBSpos[2][1] = BSMiny;
		
		squareBSpos[3][0] = BSMinx;
		squareBSpos[3][1] = BSMiny;
		return squareBSpos;
	}
	
	boolean notSameRegion(double[][] BSpos)
	{
		boolean notSame = false;
		double[] tag;
		double[] tmp;
		double Xmin, Ymin, Xmax, Ymax;
		
		// Xmin Ymin
		tag = BSpos[0];
		tmp = getRegionEdge(tag, BSpos);
		Xmin = tmp[0];
		Ymin = tmp[1];
		
		//Xmin Ymax
		tag = BSpos[3];
		tmp = getRegionEdge(tag, BSpos);
		Xmin = (Xmin + tmp[0])/2;
		Ymax = tmp[1];
		
		
		// Xmax Ymax
		tag = BSpos[2];
		tmp = getRegionEdge(tag, BSpos);
		Xmax = tmp[0];
		Ymax = (Ymax + tmp[1])/2;
		
		// Xmax Ymin
		tag = BSpos[1];
		tmp = getRegionEdge(tag, BSpos);
		Xmax = (Xmax + tmp[0])/2;
		Ymin = (Ymin + tmp[1])/2;
		
		//tmp = getRegionEdge(tagPos, BSpos);  //#TODO �˴��߼�������= = ��Ӧ���ý��������λ���жϡ�
		if(R!= null && R.length == 3)
		{
			tmp = tmpCaltR(resortBSpos);
			if(tmp[0] < Xmin - devR || tmp[0] > Xmax + devR || tmp[1] < Ymin - devR || tmp[1] > Ymax + devR)
			{
				notSame = true;
				System.out.println("�ж����磺������������");
			}
		}
		return notSame;
	}
	
	double[] tmpCaltR(double[][] BSpos)
	{
		double[] temp = new double[2];
		int act_bsnum = BSpos.length;
		if(act_bsnum == 4)
		{
			double midX = 0;
			double midY = 0;
			for(int i = 0; i < act_bsnum; i ++)
			{
				midX += BSpos[i][0];
				midY += BSpos[i][1];
			}
			midX /= act_bsnum;
			midY /= act_bsnum;
			int[] BSindex = new int[act_bsnum];
			for(int i = 0; i < act_bsnum; i ++)
			{
				BSindex[i] = BSserNo(BSpos[i], midX, midY);
			}
			double[] tmpR = new double[act_bsnum];
			for(int i = 0; i < act_bsnum - 1; i++)
			{
				tmpR[BSindex[i+1]] = R[i];
			}
			switch(BSindex[0])
			{
			case 0:
				temp[0] = -tmpR[1] - tmpR[2] + tmpR[3];
				temp[1] = tmpR[1] - tmpR[2] - tmpR[3];
				break;
			case 1:
				temp[0] = tmpR[0] - tmpR[2] + tmpR[3];
				temp[1] = tmpR[0] - tmpR[2] - tmpR[3];
				break;
			case 2:
				temp[0] = tmpR[0] - tmpR[1] + tmpR[3];
				temp[1] = tmpR[0] + tmpR[1] - tmpR[3];
				break;
			case 3:
				temp[0] = tmpR[0] - tmpR[1] - tmpR[2];
				temp[1] = tmpR[0] + tmpR[1] - tmpR[2];
				break;
			default:
				break;
			}
			
		}
		return temp;
	}
	
	int BSserNo(double[] BSpos, double midX, double midY)
	{
		int BSindex = 0;
		if(BSpos[0] < midX && BSpos[1] < midY)
			BSindex = 3;
		if(BSpos[0] < midX && BSpos[1] > midY)
			BSindex = 0;
		if(BSpos[0] > midX && BSpos[1] < midY)
			BSindex = 2;
		if(BSpos[0] > midX && BSpos[1] > midY)
			BSindex = 1;
		return BSindex;
	}
	

	
	
	double[] getRegionEdge(double[] tag, double[][] BSpos)
	{
		double[] res = new double[2];
		double temp12  = norm(tag,BSpos[0]) - norm(tag,BSpos[1]);
		double temp34  = norm(tag,BSpos[2]) - norm(tag,BSpos[3]);
		double temp14  = norm(tag,BSpos[0]) - norm(tag,BSpos[3]);
		double temp23  = norm(tag,BSpos[1]) - norm(tag,BSpos[2]);
		res[0] = temp12 - temp34;
		res[1] = temp14 + temp23;
		return res;
	}
	
	void getEdge(double[][] BSpos)
	{
		BSMinx = BSpos[0][0];
		BSMaxx = BSpos[0][0];
		BSMiny = BSpos[0][1];
		BSMaxy = BSpos[0][1];
		
		for(int i = 1; i < max_bsnum; i++)
		{
			if(BSMinx > BSpos[i][0])
				BSMinx = BSpos[i][0];
			if(BSMaxx < BSpos[i][0])
				BSMaxx = BSpos[i][0];
			if(BSMiny > BSpos[i][1])
				BSMiny = BSpos[i][1];
			if(BSMaxy < BSpos[i][1])
				BSMaxy = BSpos[i][1];
		}
	}
	

	
	boolean invalidRes(double[][] BSpos)
	{
		boolean flag = false;
		if(tagPos[0] > BSMaxx + det|| tagPos[0] < BSMinx - det|| tagPos[1] > BSMaxy + det || tagPos[1] < BSMiny - det)
		{
			flag = true;
			System.out.println("�ж����磺��λ�������");
		}
		return flag;
	}
	
	void getValidData(double[] timeStamp, double[][] BSpos, double[] coef_tmp, double[] delay_tmp,Boolean containmbs,long tagid,long[] bsname)
	{
		bsnum = 0;

		int[] state = new int[max_bsnum];
		for(int j = 0; j < max_bsnum; j++)
		{
			System.out.println("timeStamp:"+timeStamp[j]);
			if(timeStamp[j] < 0.1)
				state[j] = 0;
			else
			{
				state[j] = 1;
				bsnum++;
			}
		}
		

		detR = new double[bsnum-1];
		if(bsnum < 3)
			System.out.println("��վ��������3��");
		else
		{
			if(state[0]!=0)
			{
				resortBSpos = resortBS(BSpos,state);
				resortTS = resortTS(timeStamp,state);
				time_coef = resortTS(coef_tmp,state);
				bsDelay = resortTS(delay_tmp,state);
				// todo ����һ��flag��ʾ��һ����վ�Ƿ�Ϊ����ͬ��������վ
				double[] tdoa=new  double[bsnum-1];
				
				R = getValidR(containmbs,tdoa);
				System.out.print("\n ");
				for(int i = 0; i < R.length; i++){
					System.out.print("R_"+i+" "+R[i]+" ");
				}
				System.out.print("\n ");
				//R = getValidR_incMBS();
				if(bsnum == 3)
					location_3bs();
				else if(bsnum == 4)
					location_4bs();
				else
					location_RLS();
			
				
			/*	if(tagPos!=null&&tagPos[2]!=0) {
					setRtoTag(tagid, bsname, tdoa);
					
				}*/
				

			}
			else
			{
				resortBSpos = resortBS(BSpos,state);
				resortTS = resortTS(timeStamp,state);
				time_coef = resortTS(coef_tmp,state);
				for(int i = 0; i < 3; i++)
				{
					Ref[i] = BSpos[0][i];
				}
				R = getValidR_3bs();

				location_3bs();
			}

		}
	}
	
	boolean validRefR()
	{
		boolean flag = false;
		double sum = 0;
		for(int i = 0; i < detR.length; i++)
		{
			sum += detR[i];
		}
		System.out.println("MaxRDiff sum:"+sum);
		if(sum > MaxRDiff)
		{
			flag = true;
			System.out.println("�ж����磺����������ֵ������");
		}
		return flag;

	}
	
	double[][] resortBS(double BSpos[][], int state[])
	{
		double[][] BS = new double[bsnum][3];
		int i,j,k;
		k = 0;
		for(i = 0; i < max_bsnum; i++)
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

	double[] resortTS(double timeStamp[], int state[])
	{
		double[] TS = new double[bsnum];
		int i,k;
		k = 0;
		for(i = 0; i < max_bsnum; i++)
		{
			if(state[i] == 1)
			{
				TS[k] = timeStamp[i];
				k++;
			}
		}
		return TS;
	}

	double[] getValidR(Boolean containmbs,double[] tdoa)
	{

	//	time_coef[0] =  0.00469176397861579;

		double[] vR = new double[bsnum-1];
		 for(int i = 0; i < time_coef.length; i++)
		 {
			 if(time_coef[i] < 0.001d)
				 time_coef[i] = 0.00469176397861579;
		 }
		if(containmbs) {
			
			double[] d = new double[bsnum-1];
			for (int i = 0; i < bsnum - 1; i++) {
				d[i] = Math.sqrt(Math.pow(resortBSpos[i + 1][0] - resortBSpos[0][0], 2) + Math.pow(resortBSpos[i + 1][1] - resortBSpos[0][1], 2) + Math.pow(resortBSpos[i + 1][2] - resortBSpos[0][2], 2));
				//vR[i] = resortTS[i + 1] * time_coef[i+1]  - resortTS[0] * time_coef[0] + 2 * anneDelay + d[i];
				vR[i] = resortTS[i + 1] * time_coef[i + 1] - resortTS[0] * time_coef[0] + 2 * bsDelay[i + 1] + d[i];
				//tdoa[i]=resortTS[i + 1] * time_coef[i + 1] - resortTS[0] * time_coef[0] + 2 * bsDelay[i + 1];
				tdoa[i]=	vR[i];
				
				//System.out.println("�����:"+tdoa[i]);
			}
		}else{
			
			double[] d = new double[bsnum];
			for(int i = 0; i < bsnum; i++)
			{
				d[i] = Math.sqrt(Math.pow(resortBSpos[i][0] - Ref[0], 2) + Math.pow(resortBSpos[i][1] - Ref[1], 2) + Math.pow(resortBSpos[i][2] - Ref[2], 2));
			}

			for(int i = 0; i < bsnum-1; i++)
			{
				vR[i] = resortTS[i + 1] * time_coef[i+1]  - resortTS[0] * time_coef[0] + d[i+1] - d[0];
			}

		}
	
		//debug
//		double R21 = resortTS[2] * time_coef[2] - resortTS[1] * time_coef[1] + d[1] - d[0];
		
		return vR;
	}
	
	double[] getValidR_3bs()
	{
		double[] vR = new double[bsnum-1];
		double[] d = new double[bsnum];
		for(int i = 0; i < bsnum; i++)
		{
			d[i] = Math.sqrt(Math.pow(resortBSpos[i][0] - Ref[0], 2) + Math.pow(resortBSpos[i][1] - Ref[1], 2) + Math.pow(resortBSpos[i][2] - Ref[2], 2));
		}
		
		for(int i = 0; i < bsnum-1; i++)
		{
			vR[i] = resortTS[i + 1] * time_coef[i+1]  - resortTS[0] * time_coef[0] + d[i+1] - d[0];
		}
		
		return vR;
	}
	
	
	void location_3bs()
	{
		tagPos = Location_algo.TDOA_3bs(R, resortBSpos, AssumptionZ, BSMinx,  BSMaxx,  BSMaxy,  BSMiny,  bsnum,  detR);

	}

	void location_4bs()
//	static double[] locationChan2D(double R[], double BSpos[][], int BSnum, double z_assump, long[] bsname)
	{
		//��ά��λ����վû�и߶Ȳ�
		tagPos = Location_algo.TDOA_chan2D(R, resortBSpos, AssumptionZ, BSMinx,  BSMaxx,  BSMaxy,  BSMiny,  bsnum,  detR);
		//tagPos = Location_algo.location3DChan(R, resortBSpos, 4);
	}
	
	void location_RLS()
//	static double[] locationChan2D(double R[], double BSpos[][], int BSnum, double z_assump, long[] bsname)
	{
		//��ά��λ����վû�и߶Ȳ�
		tagPos = Location_algo.locationRLS(bsnum, R, resortBSpos, AssumptionZ, detR);
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
			System.out.println("������������3�����㿪������");
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













