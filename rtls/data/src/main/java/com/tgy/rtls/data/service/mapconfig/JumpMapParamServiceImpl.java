package com.tgy.rtls.data.service.mapconfig;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.mapconfig.JumpMapParam;
import com.tgy.rtls.data.mapper.mapconfig.JumpMapParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class JumpMapParamServiceImpl extends ServiceImpl<JumpMapParamMapper, JumpMapParam> implements JumpMapParamService{
    @Autowired
    private JumpMapParamMapper jumpMapParamMapper;
    @Override
    public int updateBatch(List<JumpMapParam> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int batchInsert(List<JumpMapParam> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public List<JumpMapParam> getMapconfig(String keyword, String desc,Integer mapId,String[] maps) {
        return jumpMapParamMapper.getMapconfig(keyword, desc,mapId,maps);
    }
}
