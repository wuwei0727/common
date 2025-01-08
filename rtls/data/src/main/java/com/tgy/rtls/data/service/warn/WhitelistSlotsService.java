package com.tgy.rtls.data.service.warn;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.warn.WhitelistSlots;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn.impl
*@Author: wuwei
*@CreateTime: 2024-10-25 09:23
*@Description: TODO
*@Version: 1.0
*/
public interface WhitelistSlotsService extends IService<WhitelistSlots>{
    List<WhitelistSlots> getAllOrFilteredWhitelist(String configName, String status, String map,String mapId, String desc, String[] mapids);

    WhitelistSlots getWhitelistById(int id);


}
