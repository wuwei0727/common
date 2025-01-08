package com.tgy.rtls.data.mapper.type;

import com.tgy.rtls.data.entity.type.Status;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.type
 * @date 2020/11/4
 */
public interface StatusMapper {
    /*
    * 查询相关的系统类型
    * */
    List<Status> findByAll(@Param("type")String type,String name);

}
