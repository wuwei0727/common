package com.tgy.rtls.data.entity.park.floorLock;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.park.floorLock
 * @Author: wuwei
 * @CreateTime: 2024-07-17 15:49
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "car_plate")
public class CarPlate extends BaseEntitys implements Serializable {
    @TableField(value = "plate_number")
    private String plateNumber;

    @TableField(value = "company_id", updateStrategy = FieldStrategy.IGNORED)
    private Long companyId;

    @TableField(value = "company_name", updateStrategy = FieldStrategy.IGNORED)
    private String companyName;

    @TableField(value = "map_id", updateStrategy = FieldStrategy.IGNORED)
    private Long mapId;

    @TableField(value = "map_name", updateStrategy = FieldStrategy.IGNORED)
    private String mapName;

    @TableField(value = "phone_number")
    private String phoneNumber;

    private static final long serialVersionUID = 1L;
}