package com.tgy.rtls.data.algorithm;




import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FloorJudgeLevel {

    int count=4;
    List<Floor> floor_list;
    ConcurrentHashMap<String,Integer> floor_count=new ConcurrentHashMap();



   public  void setFloorInf(List<Floor> list){
      this.floor_list=list;
   }
  public   String getFloor( float z,String formerFloor){


        String res=null;

      Collections.sort(this.floor_list);
      if(formerFloor==null)
          formerFloor=this.floor_list.get(0).getName();
          int k=-1;
      for (int i=0;i<floor_list.size();i++
           ) {
         Floor f=floor_list.get(i);
          if(f.getName().equals(formerFloor)){
                k=i;
          }

      }

        for (int i=0;i<floor_list.size();i++
             ) {
            Floor floor = floor_list.get(i);
            double height= floor.getHeight();
            double lower= floor.getLower();
            double higher= floor.getUpper();
            String name=floor.getName();
            if(z>=(height-lower)&&z<=(height+higher)){
                if(floor_count.containsKey(name)){
                    Integer  f_count= floor_count.get(name);
                    f_count++;
                    floor_count.replace(name,f_count);
                    if(f_count>count){
                        res=name;
                        floor_count.clear();
                        break;
                    }

                }else{
                    floor_count.put(name,1);
                }
            }else{
                String   name1=null;
                if(i<floor_list.size()-1) {
                    Floor upFloor = floor_list.get(i + 1);
                    double down = floor.getHeight() + floor.getUpper();
                    double up = upFloor.getHeight() - upFloor.getLower();

                    if (z > down && z < up) {
                        if(k==i){
                            name1=floor.getName();
                        }
                        if (k < i) {
                            name1 = floor.getName();
                        }
                        if (k > i) {
                            name1 = upFloor.getName();
                        }

                    }
                    if(i==0&&z<(floor.getHeight()-floor.getLower())){
                        name1=floor.getName();
                    }

                }else{
                    if(z>(floor.getHeight()+floor.getUpper())) {
                        name1 = floor.getName();
                    }

                }

                    if(name1!=null) {

                        if (floor_count.containsKey(name1)) {
                            Integer f_count = floor_count.get(name1);
                            f_count++;
                            floor_count.replace(name1, f_count);
                            if (f_count > count) {
                                res = name1;
                                floor_count.clear();
                                break;
                            }

                        } else {
                            floor_count.put(name1, 1);
                        }
                    }
                }
            }




        return res;

    }


    public static void main(String[] args) {

     /*   FloorJudgeLevel s=new FloorJudgeLevel();
        Floor floor1=new Floor();
        floor1.setHeight(1d);
        floor1.setUpper(0.1);
        floor1.setLower(0.1);
        floor1.setName("一楼");
        Floor floor2=new Floor();
        floor2.setHeight(2d);
        floor2.setUpper(0.1);
        floor2.setLower(0.1);
        floor2.setName("二楼");
        Floor floor3=new Floor();
        floor3.setHeight(3d);
        floor3.setUpper(0.1);
        floor3.setLower(0.1);
        floor3.setName("三楼");
        List<Floor> list=new ArrayList<>();
        list.add(floor3);
        list.add(floor1);
        list.add(floor2);

        s.setFloorInf(list);
        for(int i=0;i<100;i++){
            String res = s.getFloor(1.3f,"三楼");
            if(res!=null)
                System.out.println("楼层："+res);
        }*/
    }

}
