package com.tgy.rtls.data.algorithm;

import java.util.ArrayList;
import java.util.Collections;

public class BsSelfLocation {


   double[] BsSelfLocationMethod(Double[] dis,Double[][] originalBs){
       double[][] pos = Hilen.location1D(dis, originalBs);
       double y=Hilen.getVerticalDis(dis[0],dis[1],PercentToPosition.getDis(originalBs[0],originalBs[1]));
       double[] bsPos={pos[0][0],y};
       return bsPos;
   }

    public static void main(String[] args) {
       Double[] bs1={0d,0d,0d};
       Double bs1_dis=3d;
        Double[] bs2={0d,2d,0d};
        Double bs2_dis=1d;
        Double[] bs3={0d,4d,0d};
        Double bs3_dis=1d;
        Double[] bs4={0d,6d,0d};
        Double bs4_dis=3d;
        Double[] bs5={10d,0d,0d};
        Double bs5_dis=10.44d;
/*        Double[] bs6={2d,4d,0d};
        Double bs6_dis=4.12d;
        Double[] bs7={1d,4d,0d};
        Double bs7_dis=4d;*/

        ArrayList<Double[]> bsposList=new ArrayList();
        ArrayList<Double> bsposDis=new ArrayList();
        bsposList.add(bs1);
        bsposList.add(bs2);
        bsposList.add(bs3);
        bsposList.add(bs4);
        bsposList.add(bs5);
/*        bsposList.add(bs6);
        bsposList.add(bs7);*/
        bsposDis.add(bs1_dis);
        bsposDis.add(bs2_dis);
        bsposDis.add(bs3_dis);
        bsposDis.add(bs4_dis);
        bsposDis.add(bs5_dis);
/*        bsposDis.add(bs6_dis);
        bsposDis.add(bs7_dis);*/
        calcut(bsposList,bsposDis);


    }
    public static void  calcut(ArrayList<Double[]> bsposList ,ArrayList<Double> bsposDis) {
        int calculateBs = 4;
        if (bsposList.size() >= 4) {
            Double[][] bsPos = bsposList.toArray(new Double[0][0]);
            Double[] bsDis = bsposDis.toArray(new Double[0]);
            ArrayList<DisSort> list = new ArrayList();
            int bsCount = bsDis.length;
            for (int i = 0; i < bsCount; i++) {
                DisSort disSort = new DisSort(bsPos[i][0], bsPos[i][1], bsPos[i][2],bsPos[i][3]+"", bsDis[i]);
                list.add(disSort);
            }
            Collections.sort(list);
            bsPos = new Double[calculateBs][3];
            bsDis = new Double[calculateBs];
            ArrayList<DisSort> calcul_list = new ArrayList();


            calcul_list.add(list.get(0));
            calcul_list.add(list.get(1));
            calcul_list.add(list.get(2));


            for (int i = 3; i < list.size(); i++) {
                DisSort newObject = list.get(i);
                calcul_list.add(newObject);
                double[][] same_linearray = new double[calcul_list.size()][3];
                int k = 0;
                for (DisSort obj : calcul_list
                ) {
                    same_linearray[k][0] = obj.getX();
                    same_linearray[k][1] = obj.getY();
                    same_linearray[k][2] = obj.getZ();
                    k++;
                }

                Boolean sameLine = M3D.sameLine(same_linearray);
                if (sameLine) {
                    calcul_list.remove(newObject);
                }
                if (calcul_list.size() >= calculateBs)
                    break;
            }
            double[] res = null;
            bsPos=new Double[calcul_list.size()][3];
            bsDis=new Double[calcul_list.size()];
            if (calcul_list.size() >= calculateBs) {
                int k = 0;
                for (DisSort obj : calcul_list
                ) {
                    bsPos[k][0] = obj.getX();
                    bsPos[k][1] = obj.getY();
                    bsPos[k][2] = obj.getZ();
                    bsDis[k] = obj.getDis();
                    k++;
                }
                res = M3D.location_Minum(bsPos, bsDis);
                System.out.println(res);
            }
        }
    }



}
