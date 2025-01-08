package com.tgy.rtls.gateway.fft;


import com.tgy.rtls.gateway.fft.count.CountStep;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

class FFT{
    public static void main (String[] args)
    {
        System.out.println("demoFFT");
       List list = ReadTxt.readTxt("D:/fft.txt");
     //   System.out.println(list.size());
        // The length of the data.
        int lengthFFT =list.size();
        // The length of the data.

        double[] realArray = new double[lengthFFT];
        double[] imagArray = new double[lengthFFT];
       double amp = 100.0;

        /* Experiment 1: cos function. */
        for (int i = 0; i < lengthFFT; i++) {
            realArray[i] = (double)list.get(i);
            imagArray[i] = 0.0;
        }

      //   Random rand=new Random();
        /* Experiment 1: cos function. */

        System.out.println("peak num"+ReadTxt.getPeakNum3(realArray));
        // Take the forward FFT.
        for(int i=0;i<lengthFFT;i++){
     //System.out.println("before:"+i+":"+(float)realArray[i]);
        //    realArray[i]=(double)(list.get(i));
      //    float[] acc={  (float)realArray[i],0,0};
         //   countStep.refreshAcc(acc,0l,0.2f);
        }


       // System.out.println("peak num"+ReadTxt.getPeakNum3(realArray));
       FastFourierTransform.fastFT(realArray, imagArray, true);


       for(int i=0;i<lengthFFT;i++){
      //System.out.println("filter:"+i+":"+realArray[i]);
         /* if(i>10)
          realArray[i]=0;*/
            // System.out.println("filter:"+i+":"+realArray[i]);

        }
       // System.out.println("peak num"+ReadTxt.getPeakNum3(realArray));
        // Take the inverse FFT.
        FastFourierTransform.fastFT(realArray, imagArray, false);

        for(int i=0;i<lengthFFT;i++){
         //System.out.println("inverse:"+i+":"+(float)+realArray[i]);
        }

      /*  *//* Experiment 2: sin function. *//*
        for (int i = 0; i < lengthFFT; i++) {
            realArray[i] = amp * Math.sin(20 * i * Math.PI / lengthFFT);
            imagArray[i] = 0.0;
        }
        // Take the forward FFT.
        FastFourierTransform.fastFT(realArray, imagArray, true);
        // Take the inverse FFT.
        FastFourierTransform.fastFT(realArray, imagArray, false);

        *//* Experiment 3: box function. *//*
        int range1 = lengthFFT / 4;
        int range2 = 3 * lengthFFT / 4;
        for (int i = 0; i < lengthFFT; i++) {
            if ((i >= range1) && (i < range2)) {
                realArray[i] = amp;
            }
            else {
                realArray[i] = 0.0;
            }
            imagArray[i] = 0.0;
        }
        // Take the forward FFT.
        FastFourierTransform.fastFT(realArray, imagArray, true);
        // Take the inverse FFT.
        FastFourierTransform.fastFT(realArray, imagArray, false);*/
    }
}



