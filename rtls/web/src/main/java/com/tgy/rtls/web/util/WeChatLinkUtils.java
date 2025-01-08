package com.tgy.rtls.web.util;

import com.alibaba.fastjson.JSONObject;
import com.seepine.http.util.HttpUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.vip.VipArea;
import com.tgy.rtls.data.entity.vip.VipParking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2023-04-06 13:12
 * @Description: TODO
 * @Version: 1.0
 * 4067.45-100
 */
@Slf4j
@Component
public class WeChatLinkUtils {
    private static final String WX_CODE_URL = "https://api.weixin.qq.com/wxa/generate_urllink?access_token=ACCESS_TOKEN";
    private static final String VIP_PLACE = "place";
    private static final String VIP_AREA = "area";
    private static String MINIPROGRAM_STATE="release";
    @Value("${wechat.shortChain.miniprogram.state}")
    private String miniProgramState;

    @PostConstruct
    public void init() {
        MINIPROGRAM_STATE = miniProgramState;
    }
    public static String getWeChatLink(String accessToken, VipParking vipParking, VipArea vipArea) {
        try {
            StringBuilder urlBuilder = new StringBuilder();

            if (!NullUtils.isEmpty(vipParking)) {
                urlBuilder.append("fid=").append(vipParking.getFid()).append("&");
                urlBuilder.append("name=").append(URLEncoder.encode(vipParking.getName(),StandardCharsets.UTF_8.toString())).append("&");
                urlBuilder.append("mapId=").append(vipParking.getMap()).append("&");
                urlBuilder.append("vipPlaceId=").append(vipParking.getId()).append("&");
                urlBuilder.append("deviceId=").append(vipParking.getDeviceId()).append("&");
                urlBuilder.append("start=").append(vipParking.getStartTime()).append("&");
                urlBuilder.append("end=").append(vipParking.getEndTime()).append("&");
                urlBuilder.append("vipType=").append(VIP_PLACE);
                if(!NullUtils.isEmpty(vipParking.getPlaceElevatorId())){
                    urlBuilder.append("&placeElevatorId=").append(vipParking.getPlaceElevatorId());
                }
            } else {
                urlBuilder.append("fid=").append(vipArea.getFid()).append("&");
                urlBuilder.append("name=").append(URLEncoder.encode(vipArea.getBarrierGateArea(), StandardCharsets.UTF_8.toString())).append("&");
                urlBuilder.append("mapId=").append(vipArea.getMap()).append("&");
                // urlBuilder.append("vipPlaceId=").append(vipArea.getId()).append("&");
                urlBuilder.append("deviceId=").append(vipArea.getBarrierGateId()).append("&");
                urlBuilder.append("start=").append(vipArea.getStartTime()).append("&");
                urlBuilder.append("end=").append(vipArea.getEndTime()).append("&");
                urlBuilder.append("vipType=").append(VIP_AREA);
                log.info("调用getWeChatLink方法---->"+urlBuilder);
            }
            JSONObject paramMap = new JSONObject();
            //接口调用凭证
            //通过 URLLink 进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query 。path 为空时会跳转小程序主页
            paramMap.put("path", "pages/map/map");
            //生成的 URL Link 类型，到期失效：true，永久有效：false
            paramMap.put("is_expire", true);
            paramMap.put("query", urlBuilder.toString());
            //正式版为 "release"，体验版为"trial"，开发版为"develop"
            // if("172.0.1.12".equals(Inet4Address.getLocalHost().getHostAddress())){
               paramMap.put("env_version",MINIPROGRAM_STATE);
            // }else {
            //      paramMap.put("env_version","develop");
            // }
            // paramMap.put("env_version","trial");
            // 小程序 URL Link 失效类型，失效时间：0，失效间隔天数：1
            //paramMap.put("expire_type", 0);
            //到期失效的URL Link的失效间隔天数。生成的到期失效URL Link在该间隔时间到达前有效。最长间隔天数为365天。expire_type 为 1 必填
            //paramMap.put("expire_time",System.currentTimeMillis()/1000+1000);
            String postUrl = WX_CODE_URL.replace("ACCESS_TOKEN", accessToken);
            String result = HttpUtil.post(postUrl, paramMap.toString());
            JSONObject jsonObject = JSONObject.parseObject(result);
            String wxUrl = jsonObject.getString("url_link");
            log.info(wxUrl);
            return wxUrl;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    //这是一个main方法，程序的入口
    public static void main(String[] args){
        VipParking vipParking = new VipParking();
        vipParking.setFid("6876093339473678337");
        vipParking.setName("1-137");
        vipParking.setMap(74L);
        vipParking.setDeviceId("64:72:D9:00:9F:75");
        vipParking.setStartTime(LocalDateTime.now());
        vipParking.setEndTime(LocalDateTime.now());
        vipParking.setMapName("香雪国际公寓停车场");
        vipParking.setPhone("18674122859");
        vipParking.setPlaceElevatorId(1);

        getWeChatLink(AccessTokenUtils.getAccessToken(),vipParking,null);
    }
}
