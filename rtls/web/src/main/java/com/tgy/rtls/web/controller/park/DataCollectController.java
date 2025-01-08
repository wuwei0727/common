package com.tgy.rtls.web.controller.park;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.WeChatUser;
import com.tgy.rtls.data.entity.park.WeChatUserMark;
import com.tgy.rtls.data.entity.userinfo.*;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.CollectMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.tool.Constant;
import com.tgy.rtls.data.websocket.WebSocketLocation;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/collect")
public class DataCollectController {
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private BookMapper bookMapper;
    @Autowired(required = false)
    private CollectMapper collectMapper;
    @Autowired(required = false)
    private WebSocketLocation webSocketLocation;
    @Autowired
    private BsConfigService bsConfigService;
    @Autowired
    private Map2dService map2dService;
    private static final String appId="wxf0f25ad3fc36365e";

    @RequestMapping(value = "/userInfo")
    @ApiOperation(value = "收集用户微信账户信息",notes = "111")
    public CommonResult<Object> userInfo(WechatUserInfo wechatUserInfo) {
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String uid="12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            String  openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            if(openid==null){
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if(user==null){
                user=new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            wechatUserInfo.setUserid(user.getId());

            WechatUserInfo data = collectMapper.findUserInfo(user.getId());
            if(data==null){
                collectMapper.addWechatUserInfo(wechatUserInfo);
            }
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

}

    @RequestMapping(value = "/getuserInfo")
    @ApiOperation(value = "收集用户微信账户信息",notes = "111")
    public CommonResult<Object> getuserInfo() {
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String uid="12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics. QUERY_SUCCESS));
            String  openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            if(openid==null){
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if(user==null){
                user=new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            WechatUserInfo data = collectMapper.findUserInfo(user.getId());
            res.setData(data);
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/userPosition")
    @ApiOperation(value = "收集用户位置信息",notes = "111")
    public CommonResult<Object> userPosition(WechatUserPosition wechatUserPosition,Integer userId){
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String uid="12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            String  openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            WeChatUser user = bookMapper.findWeChatUserByUserid(userId,openid);
            if(openid == null&& NullUtils.isEmpty(user)){
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            if(NullUtils.isEmpty(user)){
                user=new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            wechatUserPosition.setUserid(user.getId());
             collectMapper.addWechatUserPosition(wechatUserPosition);
            WechatUserInfo data = collectMapper.findUserInfo(user.getId());
            String nickName=null;
            if(data!=null){
               nickName=data.getNickName();
                wechatUserPosition.setNiceName(nickName);
            }
            JSONObject jsonArea = new JSONObject();
            jsonArea.put("type", 1);
            jsonArea.put("data", wechatUserPosition);
            jsonArea.put("map", wechatUserPosition.getMap());
            webSocketLocation.sendAll(jsonArea.toString());
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    @RequestMapping(value = "/userAdvice")
    @ApiOperation(value = "用户意见",notes = "111")
    @Transactional
    public CommonResult<Object> userAdvice(String list){
        try {
           // Json



            Session session = SecurityUtils.getSubject().getSession();
            String uid="12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            String  openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            if(openid==null){
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if(user==null){
                user=new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }

            JSONArray arrays = JSONArray.fromObject(list);
            List<WechatUserAdvice> userAdvices=new ArrayList();
            for (Object object:arrays
            ) {
                WechatUserAdvice advice=(WechatUserAdvice)JSONObject.toBean((JSONObject)object, WechatUserAdvice.class);
                userAdvices.add(advice);
                advice.setUserid(user.getId());
            }


           collectMapper.addWechatUseradvice(userAdvices);
            res.setMessage("提交成功");
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,"提交失败");
        }

    }

    @RequestMapping(value = "/userOperation")
    @ApiOperation(value = "添加用户操作记录",notes = "111")
    public CommonResult<Object> addWechatUserOperation(WechatUserOperation wechatUserOperation){
        try {


            Session session = SecurityUtils.getSubject().getSession();
            String uid="12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
           String  openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            if(openid==null){
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if(user==null){
                user=new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            wechatUserOperation.setUserid(user.getId());
            collectMapper.addWechatUserOperation(wechatUserOperation);
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/findUserFunction")
    @ApiOperation(value = "获取用户评价功能列表",notes = "111")
    public CommonResult<Object> findUserFunction(){
        try {

            List<WechatUserFunction> data = collectMapper.findUserFunction();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_MESSAGE));
            res.setData(data);
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }




    @RequestMapping(value = "/addMark")
    @ApiOperation(value = "添加用户标记点",notes = "111")
    public CommonResult<Object> addMark(WeChatUserMark weChatUserMark){
        try {

            Session session = SecurityUtils.getSubject().getSession();
            String uid="12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            String  openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            if(openid==null){
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if(user==null){
                user=new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            weChatUserMark.setUserid(user.getId());
           collectMapper.addWechatUsermark(weChatUserMark);
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/findMarks")
    @ApiOperation(value = "查询标记点",notes = "111")
    public CommonResult<Object> findMarks(){
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String uid="12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            String  openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            if(openid==null){
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if(user==null){
                user=new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            List<WeChatUserMark> data = collectMapper.findUserMark(user.getId());
            res.setData(data);
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getTrailRecord")
    @ApiOperation(value = "查询轨迹点",notes = "111")
    public CommonResult<Object> findTrailRecord(Integer uid,Integer map ,String start ,String end){
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if(uid==null|| map ==null||start==null|| end ==null){
                res.setCode(400);
                res.setMessage("参数不完整");
                return res;
            }
            List<WechatUserPosition> trailRecord = collectMapper.getWechatPosition(uid, map, start, end);
            res.setData(trailRecord);
            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }


    @RequestMapping(value = "/getbeaconInfo")
    @ApiOperation(value = "获取信标",notes = "111")
    public CommonResult<Object> getPlace(String name) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
           {
                BsConfig beacon = bsConfigService.findByNum(name);
                if(beacon==null||beacon.getMap()==null){
                    res.setCode(401);
                    res.setMessage("未获取到信标的地图信息");
                }else {
                    Map_2d map = map2dService.findById(beacon.getMap());
                    res.setData(map);
                }
            }

            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }






}
