package com.tgy.rtls.web.controller.pay;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.tool.Constant;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.service.WxOAuth2Service;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.pay
 * @Author: wuwei
 * @CreateTime: 2023-12-01 18:17
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/wxOAuth2")
@Slf4j
@RequiredArgsConstructor
public class WeChatOAuth2Controller {
    private final WxMpService wxMpService;
    private final RedisTemplate<String,Object> redisTemplate;


    @GetMapping("/getAuthorizationUrl")
    public String getAuthorizationUrl(@RequestParam(defaultValue = Constant.ServiceNumber_APP_ID) String appId,String url) {
        return this.wxMpService.switchoverTo(appId)
                .getOAuth2Service()
                .buildAuthorizationUrl("https://ac71-112-94-22-123.ngrok-free.app" + "/wxOAuth2/webPageAuth", WxConsts.OAuth2Scope.SNSAPI_USERINFO, "STATE");
        // return this.wxMpService.switchoverTo(appId)
        //             .getOAuth2Service()
        //             .buildAuthorizationUrl("https://ac71-112-94-22-123.ngrok-free.app" + "/wechat/auth", WxConsts.OAuth2Scope.SNSAPI_BASE, "STATE");
    }

    @SneakyThrows(Exception.class)
    @GetMapping("/webPageAuth")
    public Object webPageAuth(@RequestParam("code") String code) {
        WxOAuth2Service oAuth2Service = this.wxMpService.switchoverTo(Constant.ServiceNumber_APP_ID).getOAuth2Service();
        WxOAuth2AccessToken wxOAuth2AccessToken = oAuth2Service.getAccessToken(code);
        String accessToken = wxOAuth2AccessToken.getAccessToken();
        String openId = wxOAuth2AccessToken.getOpenId();
        log.error("accessToken：[{}]", accessToken);
        log.error("openId：[{}]", openId);
        // redisTemplate.opsForValue().set("accessToken",accessToken, Duration.ofSeconds(7200));
        // WxOAuth2UserInfo userInfo = oAuth2Service.getUserInfo(wxOAuth2AccessToken, "zh_CN");
        // log.error("用户信息：[{}]", JSONUtil.toJsonStr(userInfo));
        return openId;

    }



    @RequestMapping("/verifyJsSdk")
    public Object verifyJsSdk(String signature, String timestamp, String nonce, String echostr, String url, HttpServletRequest request, HttpServletResponse response) throws WxErrorException {
        request.getAttribute("signature");
        // String[] arr = {"lrr", timestamp, nonce};
        // Arrays.sort(arr);
        //
        // // 将三个参数拼接成一个字符串
        // StringBuilder sb = new StringBuilder();
        // for (String s : arr) {
        //     sb.append(s);
        // }
        //
        // // 对拼接后的字符串进行SHA1加密
        // String encryptedStr = DigestUtils.sha1DigestAsHex(sb.toString());
        //
        // // 将加密后的字符串与微信发送的签名进行对比
        // if (encryptedStr.equals(signature)) {
        //     return echostr; // 校验通过，返回echostr给微信服务器
        // }
        if(NullUtils.isEmpty(url)){
            log.info(signature);
            log.info(timestamp);
            log.info(nonce);
            log.info(echostr);
            if(Boolean.TRUE.equals(wxMpService.checkSignature(timestamp,nonce,signature))){
                return echostr;
            }
            return false;
        }
        return wxMpService.createJsapiSignature(url);
    }
}
