package com.tgy.rtls.data.entity.park;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.park
 * @Author: wuwei
 * @CreateTime: 2022-10-25 14:42
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingCompanyVo{
    @ApiModelProperty(value = "地图")
    private Integer mapId;
    @ApiModelProperty(value = "公司Id")
    private Integer comId;
    @ApiModelProperty(value = "地图名称")
    private String mapName;
    @ApiModelProperty(value = "公司名称")
    private String comName;
    @ApiModelProperty(value = "区域Id")
    private Integer barrierGateId;
    @ApiModelProperty(value = "区域名称")
    private String areaName;
    @ApiModelProperty(value = "道闸名称")
    private String barrierGateName;

    private Integer guideScreenDeviceId;
    private String deviceId;

    @ApiModelProperty(value = "车位ID")
    private Integer placeId;
    @ApiModelProperty(value = "车位名称")
    private String placeName;

    private List<ParkingCompanyVo> comNameList;
    private List<ParkingCompanyVo> areaNameList;
    private List<ParkingCompanyVo> barrierGateList;
    private List<ParkingCompanyVo> placeNameList;
    private List<ParkingCompanyVo> showScreenList;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"mapId\":")
                .append(mapId);
        sb.append(",\"mapName\":\"")
                .append(mapName).append('\"');
        sb.append(",\"comId\":")
                .append(comId);
        sb.append(",\"comName\":")
                .append(comName);
        sb.append(",\"comNameList\":")
                .append(comNameList);
        sb.append(",\"barrierGateId\":")
                .append(barrierGateId);
        sb.append(",\"areaName\":")
                .append(areaName);
        sb.append(",\"areaNameList\":")
                .append(areaNameList);
        sb.append('}');
        return sb.toString();
    }


}
