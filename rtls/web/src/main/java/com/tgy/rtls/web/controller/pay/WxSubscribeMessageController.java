package com.tgy.rtls.web.controller.pay;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.tool.Constant;
import com.tgy.rtls.web.util.AccessTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-11-21 10:19
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/wxSubscribeMessage")
@Slf4j
@RequiredArgsConstructor
public class WxSubscribeMessageController {
    private static final String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=";

    @Value("${wechat.miniprogram.state}")
    private String MINIPROGRAM_STATE;

    @RequestMapping("/send")
    public String send(String templateId,String mapId, String fid, String deviceId, String start, String end, String license, String place,String ParkingLot) {
        String openId = (String) SecurityUtils.getSubject().getSession().getAttribute(Constant.USER_WXSESSION_ID);
        JSONObject body = new JSONObject();
        body.set("touser", openId);
        body.set("template_id", templateId);
        String page = "pages/map/map?mapId=" + mapId + "&fid=" + fid + "&deviceId=" + deviceId + "&start=" + start + "&end=" + end + "&license=" + license + "&place=" + place;
        body.set("page", page);
        body.set("miniprogram_state", MINIPROGRAM_STATE);
        JSONObject json = new JSONObject();
        json.set("thing4", new JSONObject().set("value", ParkingLot));
        json.set("car_number3", new JSONObject().set("value", license));
        json.set("thing12", new JSONObject().set("value", place));
        json.set("time2", new JSONObject().set("value", String.join("~",start,end)));
        json.set("thing8", new JSONObject().set("value", "祝您在人生的旅途中，一路顺风，一帆风顺！"));

        // json.set("car_number1", new JSONObject().set("value", "鄂FU89x1"));
        // json.set("thing2", new JSONObject().set("value", "VIP9"));
        // json.set("time3", new JSONObject().set("value", "2023/02/03 15:15:15"));
        // json.set("thing4", new JSONObject().set("value", "您是VIP9，每日不限时停车"));
        // json.set("thing5", new JSONObject().set("value", "祝您购物愉快"));

        body.set("data", json);
        String accessToken = AccessTokenUtils.getAccessToken();
        return HttpUtil.post(url + accessToken, body.toString());
    }

    @RequestMapping("/send1")
    public CommonResult<Object> send1(String templateId, String mapId, String mapName, String markId) {
        try {
            String openId = (String) SecurityUtils.getSubject().getSession().getAttribute(Constant.USER_WXSESSION_ID);
            String page = "pages/map/map?m=" + mapId + "&mk=" + markId;
            JSONObject json = new JSONObject()
                    .set("thing5", new JSONObject().set("value", mapName))
                    .set("thing8", new JSONObject().set("value", "祝您在人生的旅途中，一路顺风，一帆风顺！"));
            JSONObject body = new JSONObject()
                    .set("touser", openId)
                    .set("template_id", templateId)
    //                .set("miniprogram_state", MINIPROGRAM_STATE)
                    .set("miniprogram_state", "trial")
                    .set("page", page).set("data", json);
            String accessToken = AccessTokenUtils.getAccessToken();
            HttpUtil.post(url + accessToken, body.toString());
            return new CommonResult<>(200, "发送成功");
        } catch (Exception e) {
            return new CommonResult<>(500, "发送失败",e);
        }
    }
}
