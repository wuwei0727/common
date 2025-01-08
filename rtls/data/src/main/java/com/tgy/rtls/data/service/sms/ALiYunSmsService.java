package com.tgy.rtls.data.service.sms;

import java.util.Map;

public interface ALiYunSmsService{
    /**
     *
     * @param phone 手机号
     * @param templateCode 模版code
     * @param templateParamJson 内容
     * @return
     */
    Boolean sendMessage(String phone, String templateCode, Map<String,Object> templateParamJson);
}
