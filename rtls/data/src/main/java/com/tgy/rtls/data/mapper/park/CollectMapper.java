package com.tgy.rtls.data.mapper.park;

import com.tgy.rtls.data.entity.park.WeChatUserMark;
import com.tgy.rtls.data.entity.userinfo.WechatUserAdvice;
import com.tgy.rtls.data.entity.userinfo.WechatUserFunction;
import com.tgy.rtls.data.entity.userinfo.WechatUserInfo;
import com.tgy.rtls.data.entity.userinfo.WechatUserOperation;
import com.tgy.rtls.data.entity.userinfo.WechatUserPosition;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.message
 * @date 2020/10/26
 * 车位查询
 */
public interface CollectMapper {


    Integer addWechatUserInfo(@Param("wechatUserInfo") WechatUserInfo wechatUserInfo);
    Integer addWechatUserPosition(@Param("wechatUserPosition") WechatUserPosition wechatUserPosition);
    Integer addWechatUserOperation(@Param("wechatUserOperation") WechatUserOperation wechatUserOperation);
    Integer addWechatUseradvice(@Param("list") List<WechatUserAdvice> list);
    Integer addWechatUsermark(@Param("mark") WeChatUserMark mark);

    List<WechatUserFunction> findUserFunction();
    List<WeChatUserMark> findUserMark(@Param("userid")Integer userid );
    WechatUserInfo findUserInfo(@Param("userid")Integer userid);
    void updateUserInfo(@Param("wechatUserInfo")WechatUserInfo wechatUserInfo);

    List<WechatUserPosition> getWechatPosition(Integer uid,Integer map,String start ,String end);

    List<WechatUserPosition> getHeatmap(@Param ("map") Integer map, @Param ("level") Integer level, @Param ("start") String start, @Param ("end") String end,@Param ("userId") Integer userId);

    @Select("select id, userid, map, x, y, floor from wechat_position " +
            "where map=#{mapId} and userid=#{userId} and time between #{startTimestamp} and #{endTimestamp}")
    List<WechatUserPosition> findByUserIdAndMapIdAndTimeBetween(@Param("userId") int userId, @Param("mapId") int mapId, @Param("startTimestamp") Timestamp startTimestamp, @Param("endTimestamp") Timestamp endTimestamp);
}
