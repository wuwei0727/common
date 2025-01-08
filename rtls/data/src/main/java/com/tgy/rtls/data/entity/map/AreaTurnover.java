package com.tgy.rtls.data.entity.map;

import com.tgy.rtls.data.entity.user.PersonVO;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/21
 *区域进出报警规则
 */
@Data
@ToString
public class AreaTurnover implements Serializable {
    private Integer id;
    private Integer type;//类型 1进入报警 0离开报警
    private Integer enable;//是否启用 0否 1是
    private String startTime;
    private String endTime;
    private Short Floor;
    private Integer instanceid;    private Integer area;//规则所绑定的区域id
    //白名单的人员id集
    private String personids;
    private List<PersonVO> personSynList;
}
