package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;

@Data
@ToString
public class ParkingPlaceSort implements Comparable<ParkingPlaceSort> {
    private Integer id;
    private String fid;
    private String name;
    private String x;
    private String y;
    private String floor1;
    private int exclusive;
    private double floor;
    private int level;
    private int preferenceType;//偏好车位
    private double dis;
    private  Integer company;
    private  String companyName;


    public static void main(String[] args) {
        //------------------------------------------------
        //用户在B1,车位同楼层，先根据级别（level）,级别一样就按距离（dis）推荐，不一样就按优先级。
        //------------------------------------------------
        ParkingPlaceSort place1 = new ParkingPlaceSort();
        place1.setId(2372299);
        place1.setExclusive(1);
        place1.setFloor(0);
        place1.setLevel(0);
        place1.setPreferenceType(0);
        place1.setDis(38.082575763686144);

        ParkingPlaceSort place2 = new ParkingPlaceSort();
        place2.setId(23838);
        place2.setExclusive(1);
        place2.setFloor(0);
        place2.setLevel(0);
        place2.setPreferenceType(0);
        place2.setDis(78.26362465488123);

        ParkingPlaceSort place3 = new ParkingPlaceSort();
        place3.setId(2372326);
        place3.setExclusive(1);
        place3.setFloor(0);
        place3.setLevel(0);
        place3.setPreferenceType(0);
        place3.setDis(43.509906228541716);

        ParkingPlaceSort place4 = new ParkingPlaceSort();
        place4.setId(23839);
        place4.setExclusive(1);
        place4.setFloor(0);
        place4.setLevel(0);
        place4.setPreferenceType(0);
        place4.setDis(81.99831652482239);

        //------------------------------------------------
        //------------------------------------------------


        //------------------------------------------------
        //如果不同floor，level一样按floor推荐，不一样按照level推荐
        //------------------------------------------------
        ParkingPlaceSort place5 = new ParkingPlaceSort();
        place5.setId(2372299);
        place5.setExclusive(1);
        place5.setFloor(0);
        place5.setLevel(0);
        place5.setPreferenceType(0);
        place5.setDis(38.082575763686144);

        ParkingPlaceSort place6 = new ParkingPlaceSort();
        place6.setId(23838);
        place6.setExclusive(1);
        place6.setFloor(0);
        place6.setLevel(0);
        place6.setPreferenceType(0);
        place6.setDis(78.26362465488123);

        ParkingPlaceSort place7 = new ParkingPlaceSort();
        place7.setId(2372326);
        place7.setExclusive(1);
        place7.setFloor(0);
        place7.setLevel(-3);
        place7.setPreferenceType(0);
        place7.setDis(43.509906228541716);

        ArrayList sortList = new ArrayList();
        sortList.add(place3);
        sortList.add(place2);
        sortList.add(place4);
        sortList.add(place1);


        Object[] array = sortList.toArray();
        Arrays.sort(array);
        System.out.println(Arrays.toString(array));
    }

    //针对管理方
    @Override
    public int compareTo(ParkingPlaceSort o) {
        if (this.preferenceType > o.getPreferenceType()) {//偏好车位
            return 1;
        }else if (this.preferenceType == o.getPreferenceType()) {//偏好
            // 按照level排序
            if(this.level > o.getLevel()) {//优先级
                return 1;
            } else if (this.level == o.getLevel()) {
                if (this.floor > o.getFloor()) {//楼层
                    return 1;
                }else if (this.floor == o.getFloor()) {
                    // 按照距离dis排序
                    if(this.dis > o.getDis()) {
                        return 1;
                    }  else {
                        return -1;
                    }
                }
            }
            return -1;
        }
        return -1;
    }

    //针对用户
    // public int compareTo(ParkingPlaceSort o) {
    //     if (this.preferenceType > o.getPreferenceType()) {//偏好车位
    //         return 1;
    //     }
    //     else if (this.preferenceType == o.getPreferenceType()) {
    //         if(this.floor > o.getFloor()) {
    //             return 1;
    //         }
    //         else if(this.floor == o.getFloor()) {
    //             if(this.dis > o.getDis()) {
    //                 return 1;
    //             }
    //             else if(this.dis == o.getDis()) {
    //                 if(this.level > o.getLevel()) {
    //                     return 1;
    //                 } else if(this.level < o.getLevel()) {
    //                     return -1;
    //                 } else {
    //                     return 0;
    //                 }
    //             }else{
    //                 return -1;
    //             }
    //         }
    //         else {
    //             return -1;
    //         }
    //     }
    //     else {
    //         return -1;
    //     }

}


