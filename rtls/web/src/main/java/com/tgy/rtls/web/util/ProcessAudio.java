package com.tgy.rtls.web.util;


import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.web.config.SpringContextHolder;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Executor;


public class ProcessAudio {
	public static RedisService redisService= SpringContextHolder.getBean(RedisService.class);
	public static KafkaTemplate kafkaTemplate=SpringContextHolder.getBean(KafkaTemplate.class);
	public  static Executor scheduledExecutorService=(Executor)  SpringContextHolder.getBean("threadPool1");;
   static int SIGN_BIT=0x80;
   static int QUANT_MASK=0xf;
   static int  SEG_SHIFT  =  4;
   static int   SEG_MASK =   0x70;
   static int[] seg_aend = {
			31,63,127,255,
			511,1023,2047,511
		};
   

 	public static void decodefile(byte[] bytes,String outfile){
		/*scheduledExecutorService.execute(new Runnable() {
		 @Override
		 public void run(){*/
		   try {
			   byte[] data=Arrays.copyOfRange(bytes, 44,bytes.length);
			   byte[] head=Arrays.copyOfRange(bytes, 0,44);

			  int datalen=(int)bytes2long(Arrays.copyOfRange(head, 40,44))*2;
			  int finalsize=(int)bytes2long(Arrays.copyOfRange(head, 4,8))*2;
			  byte[] finalbyte=bytesReverseOrder(intToBytes(finalsize));
			  byte[] iii=bytesReverseOrder(intToBytes(datalen));
			   for(int i=0;i<4;i++) {
				   head[4+i]=finalbyte[i];
			   }

			   for(int i=0;i<4;i++) {
				   head[i+40]=iii[i];
			   }
			   short[] ss= decode(data);
			   writefile(head,ss,outfile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	/*	 }
	 });*/
	  
   }	
   
   
   
  public static void encodefile(String fileName, String outfile){
	 /* scheduledExecutorService.execute(new Runnable() {
		  @Override
		  public void run(){*/
		   File file= new File(fileName);
		   //filename为 文件目录，请自行设置
		   InputStream in= null;
		   byte[] bytes= null;
		   try {
			in = new FileInputStream(file);
			 bytes= new byte[in.available()];  //in.available()是得到文件的字节数
			   in.read(bytes);  //把文件的字节一个一个地填到bytes数组中
			   byte[] data=Arrays.copyOfRange(bytes, 44,bytes.length);
			   byte[] head=Arrays.copyOfRange(bytes, 0,44);
			   int len=data.length/2;
			   int datalen=(int)bytes2long(Arrays.copyOfRange(head, 40,44))/2;
				  int finalsize=(int)bytes2long(Arrays.copyOfRange(head, 4,8));
				  byte[] finalbyte=bytesReverseOrder(intToBytes(finalsize));
				  byte[] iii=bytesReverseOrder(intToBytes(datalen));
				   for(int i=0;i<4;i++) {
					   head[i+40]=iii[i];
				   }
			   short[] gg=new short[len];
			   for(int i=0;i<len;i++) {
				   gg[i]=(short) byte2short(Arrays.copyOfRange(data, i*2,(i+1)*2));
			   }
			   byte[] ss= encode(gg);
			   for(int i=0;i<5;i++) {
				   if(i<5)
					   System.out.println("ss::"+ss[i]);
			   }
			   in.close();
			   writeencodefile(head,ss,outfile);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  /*}
	  });*/
   }
   public static byte[] intToBytes(long num) {  
       byte[] b = new byte[4];  
       for (int i = 0; i < 4; i++) {  
        b[i] = (byte) (num >>> (24 - i * 8));  
       }  
        
       return b;  
    }  
   public static byte[] bytesReverseOrder(byte[] b) {  
		  int length = b.length;  
		  byte[] result = new byte[length];  
		  for(int i=0; i<length; i++) {  
		    result[length-i-1] = b[i];  
		  }  
		  return result;  
		} 
	public static   long byte2short(byte[] res) {   
		// 涓�涓猙yte鏁版嵁宸︾Щ24浣嶅彉鎴�0x??000000锛屽啀鍙崇Щ8浣嶅彉鎴�0x00??0000   
		long targets;

		  {
	    targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00);  
		  }
		return targets;   
		}
	public static  long bytes2long(byte[] b) {  
	    long temp = 0;  
	    long res = 0;
	    if(b.length==8){
	    for (int i=7;i>=0;i--) {  
	        res <<= 8;  
	        temp = b[i] & 0xff;  
	        res |= temp;  
	    }  
	  }
	    else{
	    	 for (int i=3;i>=0;i--) {  
	 	        res <<= 8;  
	 	        temp = b[i] & 0xff;  
	 	        res |= temp;  
	 	    }  
	 	   
	    	
	    	
	    }
	    return res; 
	} 
   static int  alaw2linear(int a_val)
   {
    
   	int t;
   	int seg;
    
   	a_val ^= 0x55;
    
   	t = (a_val & QUANT_MASK) << 4;
   	seg = (a_val & SEG_MASK) >> SEG_SHIFT;
   	switch (seg) {
   	case 0:
   		t += 8;
   		break;
   	case 1:
   		t += 0x108;
   		break;
   	default:
   		t += 0x108;
   		t <<= seg - 1;
    
   	}
   	return ((a_val & SIGN_BIT)>0? t : -t);
   }
   
   static short[] decode(byte[] a_psrc)
   {
    
   	int i;
    short[] a_pdst;
    System.out.println("input len"+a_psrc.length+(a_psrc[0]));
    
			
   	if (a_psrc == null ) {
   		return null;
   	}
      int len=a_psrc.length;
       a_pdst=new short[len];
   		for (i = 0; i < len; i++) {
   			a_pdst[i] = (short)alaw2linear(a_psrc[i]);
   		}
   		
   		return a_pdst;
   
   }
   
   static byte[] encode(short[] a_psrc)
   {
    
	   int len=a_psrc.length;
	   byte[] a_pdst=new byte[len];
   
   		for (int i = 0; i < len; i++) {
   		 
   			a_pdst[i] = linear2alaw(a_psrc[i]);
   		}
   		return a_pdst;
   	
   }
   
   static int search(int val, int[] data, int size)
   {
   	int m=-1;
   	for (int i = 0; i < size; i++) {
   		if (val <= data[i]) {
   			m=i;
   			break;
   			
   		}
   			
   	}
   	if(m!=-1)
   		return m;
   	else
   	return (size);
   }
   
   static byte linear2alaw(short  pcm_val)/* 2's complement (16-bit range) */
   {
    
   	int mask;
   	int seg;
   	byte aval;
    
   	pcm_val = (short) (pcm_val >> 3);
   		
   	 
		   System.out.println("pcm_val::"+pcm_val);
   	if(pcm_val >= 0) {
   		mask = 0xD5;/* sign (7th) bit = 1 */
   	} else {
   		mask = 0x55;/* sign bit = 0 */
   		pcm_val = (short) (-pcm_val - 1);
   	}
    
   	/* Convert the scaled magnitude to segment number. */
   	seg = search(pcm_val, seg_aend, 8);
    System.out.println("seg::"+seg);
   	/* Combine the sign, segment, and quantization bits. */
    
   	if (seg >= 8)/* out of range, return maximum value. */
   	{
   	 System.out.println("0x7F ^ mask::"+(0x7F ^ mask));
   		return (byte) (0x7F ^ mask);
   	}else {
   		aval = (byte) (seg << SEG_SHIFT);
   		if (seg < 2)
   			aval |= (pcm_val >> 1) & QUANT_MASK;
   		else
   			aval |= (pcm_val >> seg) & QUANT_MASK;
   		return (byte)(aval ^ mask);
   	}
    
   }
   
   static void writefile(byte[] head, short[] decode, String outfile){
	 		FileOutputStream out;
			try {
				out = new FileOutputStream(outfile);
				int headlen=head.length;
				for(int i=0; i<headlen; i++) {
		 				out.write(head[i]);
		 		}
				int len=decode.length;
		 		for(int i=0; i<len; i++) {
		 			short data=decode[i];
		 			byte[] datares=shortToByte(data);
		 			out.write(datares[0]);
		 			out.write(datares[1]);
		 		}
				
		 		out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 		
	 	
	 	}

   public static  byte[] shortToByte(short number) { 
       int temp = number; 
       byte[] b = new byte[2]; 
       for (int i = 0; i < b.length; i++) { 
           b[i] = new Integer(temp & 0xff).byteValue();// 灏嗘渶浣庝綅淇濆瓨鍦ㄦ渶浣庝綅 
           temp = temp >> 8; // 鍚戝彸绉�8浣� 
       } 
       return b; 
   } 
   static void writeencodefile(byte[] head, byte[] decode, String outfile){
	 		FileOutputStream out;
			try {

				out = new FileOutputStream(outfile);
				int headlen=head.length;
				for(int i=0; i<headlen; i++) {
		 				out.write(head[i]);
		 		}
				int len=decode.length;
		 		for(int i=0; i<len; i++) {
		 			out.write(decode[i]);
		 		}
		 		out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 		
	 	
	 	}  
   
 
	
}

