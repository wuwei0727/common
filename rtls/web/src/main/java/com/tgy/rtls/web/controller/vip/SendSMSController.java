package com.tgy.rtls.web.controller.vip;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.pay.SmsQuota;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.vip.VipArea;
import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.SmsQuotaService;
import com.tgy.rtls.data.service.sms.ALiYunSmsService;
import com.tgy.rtls.data.service.vip.VipAreaService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import com.tgy.rtls.data.tool.IpUtil;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.vip
 * @Author: wuwei
 * @CreateTime: 2023-04-06 13:20
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/sms")
@Slf4j
public class SendSMSController {
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private VipAreaService vipAreaService;
    @Autowired
    private ALiYunSmsService aLiYunSmsService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Resource
    private SmsQuotaService smsQuotaService;
//这是一个main方法，程序的入口
public static void main(String[] args){
    if(-1==0){
        System.out.println("true = " + true);
    }
}
    @ResponseBody
    @PostMapping(value = "/sendSMS")
    public CommonResult<Object> sendSms(Integer mapId,Integer vipParkingId, String phone, Integer vipAreaId, String templateCode, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Member member = (Member) SecurityUtils.getSubject().getPrincipal();
        CommonResult<Object> result = new CommonResult<>();
//        String token = AccessTokenUtils.getAccessToken();
        VipParking vipParking = null;
        VipArea vipArea = null;
        SmsQuota smsQuota = smsQuotaService.getSmsQuotaByMap(mapId, null);
        if (NullUtils.isEmpty(smsQuota)||smsQuota.getCount() == 0||smsQuota.getCount()<3) {
            return new CommonResult<>(400, "短信额度不足,请充值!!!");
        }
        Integer count = smsQuota.getCount() - 3;
        smsQuota.setCount(count);
        smsQuota.setUpdateTime(new Date());
        if(smsQuotaService.updateByPrimaryKeySelective(smsQuota)==0){
            return new CommonResult<>(400,"短信发送异常,请联系管理员或稍后再重试");
        }
        if (!NullUtils.isEmpty(vipParkingId)) {
            vipParking = vipParkingService.getVipParingSpaceInfoById(vipParkingId);
        }
        if (!NullUtils.isEmpty(vipAreaId)) {
            vipArea = vipAreaService.getVipAreaInfoInfoById(vipAreaId);
        }
//        String weChatLink = WeChatLinkUtils.getWeChatLink(token, vipParking, vipArea);
//        String linkWithoutProtocol = null;
//        if(!NullUtils.isEmpty(weChatLink) && weChatLink.contains("https://")){
//            int lastIndex = weChatLink.lastIndexOf('/');
//            linkWithoutProtocol = weChatLink.substring(lastIndex + 1);
//
//        }
        // log.info("Sending" + linkWithoutProtocol);
        Map<String, Object> map = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String param =null;
        if (vipParking != null) {
//            String x = vipParking.getX();
//            String y = vipParking.getY();
//            String fid = vipParking.getFid();
//            Long mapId = vipParking.getMap();
//            Integer groupID = vipParking.getFloor();

//            String name = String.valueOf(vipParking.getReservationPerson().charAt(0));
            String startTime = vipParking.getStartTime().format(formatter);
            String endTime = vipParking.getEndTime().format(formatter);

//            String concatenatedStr = String.format("x=%s&y=%s&fid=%s&mapId=%s&name=%s&groupID=%s",
//                    x, y, fid, mapId, name, groupID);
            String concatenatedStr = String.format("placeId=%s",vipParking.getId());


//            map.put("name",name);
            map.put("mapName", vipParking.getMapName());
            map.put("parkingName", vipParking.getName());
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("address",concatenatedStr);
//            map.put("y",vipParking.getY());
//            map.put("fid",vipParking.getFid());
//            map.put("mapId",vipParking.getMap());
//            map.put("groupID",vipParking.getFloor());
//            param = name+"||"+vipParking.getMapName()+"||"+vipParking.getName()+"||"+startTime+"||"+endTime+"||"+weChatLink;

        } else {
//            String x = vipParking.getX();
//            String y = vipParking.getY();
//            String fid = vipParking.getFid();
//            Long mapId = vipParking.getMap();
//            Integer groupID = vipParking.getFloor();
//            String name = String.valueOf(vipArea.getVipCustomers().charAt(0));
            String startTime = vipArea.getStartTime().format(formatter);
            String endTime = vipArea.getEndTime().format(formatter);

//            String concatenatedStr = String.format("x=%s&y=%s&fid=%s&mapId=%s&name=%s&groupID=%s",
//                    x, y, fid, mapId, name, groupID);
            String concatenatedStr = String.format("areaId=%s",vipArea.getId());

//            map.put("name",name);
            map.put("mapName", vipArea.getMapName());
            map.put("areaName", vipArea.getVipArea());
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("address",concatenatedStr);
//            map.put("x",vipParking.getX());
//            map.put("y",vipParking.getY());
//            map.put("fid",vipParking.getFid());
//            map.put("mapId", vipParking.getMap());
//            map.put("groupID",vipParking.getFloor());
//            param = name+"||"+vipArea.getMapName()+"||"+vipArea.getVipArea()+"||"+startTime+"||"+endTime+"||"+weChatLink;
        }

        if(aLiYunSmsService.sendMessage(phone, templateCode, map)){
            result.setCode(200);
            result.setMessage("恭喜！短信已发送，请注意查收。若未收到，请稍后重试!!!");
        } else {
            result.setMessage("获取失败");
            result.setCode(500);
        }
        String ip = IpUtil.getIpAddr(request);
        String address = ip2regionSearcher.getAddressAndIsp(ip);
        operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), LocalUtil.get("调用短信服务"), now);
        return result;
    }

    public static void mai1n(String[] args) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5tCvn38GYwAhBCGRnomd", "3dHJ92VXGMCsoef6Sezm7pkGGY2tYW");
        IAcsClient client = new DefaultAcsClient(profile);

        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("mapName", "香雪公寓停车场");
        map.put("parkingName", "F0112");
        map.put("startTime", new Date());
        map.put("endTime", "2023-04-06 21:50");

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", "17708652512");
        request.putQueryParameter("SignName", "易PARK");
        request.putQueryParameter("TemplateCode", "SMS_276060033");
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(map));
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

}
