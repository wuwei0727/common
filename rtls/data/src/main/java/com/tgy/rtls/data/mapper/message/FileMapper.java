package com.tgy.rtls.data.mapper.message;

import com.tgy.rtls.data.entity.message.FileRecord;
import com.tgy.rtls.data.entity.message.FileSyn;
import com.tgy.rtls.data.entity.message.TextRecord;
import com.tgy.rtls.data.entity.message.VoiceRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.message
 * @date 2020/10/26
 * 语音/文字发送
 */
public interface FileMapper {

    /*
    * 通讯信息查询 name-->人员名称 startTime-->开始时间  endTime-->结束时间  instanceid-->实例id
    * */
    List<FileSyn> findByAll(@Param("name")String name,@Param("startTime")String startTime,@Param("endTime")String endTime,
                            @Param("instanceid")Integer instanceid,@Param("pageIndex")Integer pageIndex,@Param("pageSize")Integer pageSize);

    /*
    * 查看人员的通讯信息 personid-->人员id
    * */
    List<FileRecord> findByPersonid(@Param("personid")Integer personid,@Param("startTime")String startTime,
                                    @Param("endTime")String endTime);

    /*
    * 添加语音
    * */
    int addVoice(@Param("voice")VoiceRecord voice);

    /*
    * 添加文字
    * */
    int addText(@Param("text")TextRecord text);

    /*
    * 接收语音后 修改语音状态
    * */
    int updateVoice(@Param("random")String random,@Param("status")Integer status,@Param("url")String url,@Param("urllocal")String urllocal);

    /*
     * 接收文本后 修改文本状态
     * */
    int updateText(@Param("random")String random,@Param("status")Integer status);

}
