package com.tgy.rtls.data.mapper.check;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.check.TagcheckEntity;
import com.tgy.rtls.data.entity.check.TagcheckbsidEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rtls
 * @since 2020-11-05
 */
@Mapper
public interface TagcheckbsidDao extends BaseMapper<TagcheckbsidEntity> {

     List<TagcheckbsidEntity> getByTagcheckid(@Param("tagcheckid") Long tagcheckid,@Param("type") Integer type);
     List<TagcheckbsidEntity> getByTagcheckidAll(@Param("tagcheckid") Long tagcheckid);
     List<TagcheckbsidEntity> getByTagAndBsid(@Param("tagid") Integer tagid,@Param("bsid") Integer bsid );
     List<TagcheckbsidEntity> getNotEnd(@Param("tagid") Integer tagid,@Param("bsid") Integer bsid);
     List<TagcheckEntity> getNotEndTagCheck(@Param("bsid") Integer bsid);
     List<TagcheckbsidEntity> getALLNotEndTagCheckBsid();
     void deleteByTagCheckid(@Param("tagcheckid") Long tagcheckid);
     TagcheckEntity getByCheckid(@Param("tagcheckid") Long tagcheckid);
     List<TagcheckbsidEntity> getLackTagid(String start,String end);
     void updateCheck(@Param("tagcheckbsid") TagcheckbsidEntity tagcheckbsid);


}
