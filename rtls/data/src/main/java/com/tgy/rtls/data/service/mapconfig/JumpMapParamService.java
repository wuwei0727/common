package com.tgy.rtls.data.service.mapconfig;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.mapconfig.JumpMapParam;

import java.util.List;
public interface JumpMapParamService extends IService<JumpMapParam>{


    int updateBatch(List<JumpMapParam> list);

    int batchInsert(List<JumpMapParam> list);

    List<JumpMapParam> getMapconfig(String keyword, String desc,Integer mapID,String[] maps);
}
