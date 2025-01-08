package com.tgy.rtls.data.service.message;

import com.tgy.rtls.data.entity.message.FileRecord;
import com.tgy.rtls.data.entity.message.FileSyn;
import com.tgy.rtls.data.entity.message.TextRecord;
import com.tgy.rtls.data.entity.message.VoiceRecord;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.message
 * @date 2020/10/26
 */
public interface FileService {

    /*
     * 通讯信息查询 name-->人员名称 startTime-->开始时间  endTime-->结束时间  instanceid-->实例id
     * */
    List<FileSyn> findByAll(String name,String startTime,String endTime,Integer instanceid,Integer pageIndex,Integer pageSize);

    /*
     * 查看该人员的通讯信息 personid-->人员id
     * */
    List<FileRecord> findByPersonid(Integer personid,String startTime,String endTime);

    /*
     * 添加语音 personids-->人员id集 file-->语音文件 title-->语音标题
     * */
    Boolean addVoice(String personids,VoiceRecord voice,String http,String kafukaFile);

    Boolean addVoice(VoiceRecord voice);

    /*
     * 添加文字
     * */
    Boolean addText(String personids,TextRecord text);

    /*
    * 撤退命令
    * */
    Boolean retreatText(Integer instanceid,String title);
    /*
    * 数据恢复
    * */
    void readFileCor(String path);

    /*
     * 接收语音后 修改语音状态
     * */
    void updateVoice(String random, Integer status,String url,String urllocal);

    /*
     * 接收文本后 修改文本状态
     * */
    void updateText(String random, Integer status);
}
