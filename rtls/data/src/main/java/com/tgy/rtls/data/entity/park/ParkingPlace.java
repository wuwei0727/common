package com.tgy.rtls.data.entity.park;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@ToString
@Accessors(chain = true)
public class ParkingPlace implements Comparable<ParkingPlace>{
    private Integer id;
    private String name;
    private String x;
    private String y;
    private  String z;
    private  Integer map;
    private  Integer configWay=1;
    private  String mapName;
    private  Integer company;
    private  String companyName;
    private  String floor;
    private String floorName;
    private  Short state=3;
    private  String license;
    private String instanceid;
    private String fid;
    private Short type;//0:普通车位  1充电车位
    private String carbittype;//0:普通车位  1充电车位
    private Short charge=0;//0:无  1:充电中
    private String user;
    private String phone;
    private double dis;
    private Integer detectionException;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime exceptionTime;
    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime thirdPartyUpdateTime;//创建时间

    private Date lastRecommand;
    private int weight=0;
    private String parkingType;
    private int placeLevel;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime entryTime;//进入时间
    private Integer elevatorId;
    private String elevatorIsNull;
    private String building;
    private Integer elevatorFloor;
    private String elevatorName;
    private String sid;
    private String englishName;
    private Integer isReservable;
    @Override
    public int compareTo(ParkingPlace o) {
        if(this.weight>o.getWeight()){
            return 1;
        }else if(this.weight==o.getWeight()){
            if(this.dis>o.getDis()) {
                return 1;
            } else if(this.dis<o.getDis()) {
                return -1;
            } else {
                return 0;
            }
        }
        else {
            return -1;
        }
    }
}


