package com.tgy.rtls.web.controller.park;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.MonthActiveUserRecord;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.WeChatUser;
import com.tgy.rtls.data.entity.userinfo.WechatUserInfo;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.CollectMapper;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.park.MonthActiveUserRecordService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.tool.Constant;
import com.tgy.rtls.web.controller.view.AppletsWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;


/**
 * 微信公共控制器，控制微信登录
 *
 */

@RestController
@RequestMapping("/wx")
@Slf4j
public class WxPublicController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private ParkingService parkingService;
	@Autowired(required = false)
	private BookMapper bookMapper;
	@Autowired(required = false)
	private MonthActiveUserRecordService monthActiveUserRecordService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private AppletsWebSocket appletsWebSocket;
	@Autowired(required = false)
	private CollectMapper collectMapper;


	/**
	 * 微信小程序登录，如果已绑定用户就会自动登录
	 * @param jsCode 微信code
	 * @param request 客户端请求
	 * @return 登录结果
	 */
	@ResponseBody
	@RequestMapping(value="/login")
	public CommonResult<Object> login(String jsCode,String mapId,HttpServletRequest request,String appID) {
		ViewMapper viewMapper = SpringContextHolder.getBean(ViewMapper.class);
		CommonResult<Object> result = new CommonResult<>();
		result.setCode(200);
		result.setMessage("登录成功");
		if(jsCode == null) {
			result.setCode(500);
			result.setMessage("code不能为空");
			return result;
		}
		String openid = getOpenid(jsCode,appID);
		if(openid == null) {
			result.setCode(500);
			result.setMessage("获取不到用户唯一标识");
			return result;
		}
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername(openid);
		token.setPassword("wechat".toCharArray());
		WeChatUser userInfo = new WeChatUser();
		WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
		JSONObject jsonArea = new JSONObject();
		if (user != null) {
			userInfo.setId(user.getId());
			MonthActiveUserRecord monthUser = new MonthActiveUserRecord();
			monthUser.setUserid(user.getId());
			monthUser.setLogintime(new Date());
			monthActiveUserRecordService.add(monthUser);
			if(!"undefined".equals(mapId)&& !NullUtils.isEmpty(mapId)){
				monthUser.setMap(mapId);
				monthActiveUserRecordService.insertmap(monthUser);
			}
			List<ViewVo> useFrequency = viewMapper.getCumulativeUseFrequency(null,null,null);
			jsonArea.put("uid", "-1");
			jsonArea.put("type", 24);
			jsonArea.put("data",useFrequency);
			appletsWebSocket.sendAll(jsonArea.toString());
			List<ViewVo> userTotalNumByMonth = viewMapper.getAllUserTotalNumByMonth();
			jsonArea.put("map", "-1");
			jsonArea.put("type", 21);
			jsonArea.put("data",userTotalNumByMonth);//月活用户数
			appletsWebSocket.sendAll(jsonArea.toString());
		}
		else {
			userInfo.setUserid(openid);
			bookMapper.addWechat(userInfo);
		}
		WechatUserInfo data = collectMapper.findUserInfo(userInfo.getId());
		if(data==null){
			WechatUserInfo wechatUserInfo = new WechatUserInfo();
			wechatUserInfo.setUserid(userInfo.getId());
			collectMapper.addWechatUserInfo(wechatUserInfo);
		}
		try {
            SecurityUtils.getSubject().login(token);//登陆
			Session session = SecurityUtils.getSubject().getSession();
			session.setAttribute(Constant.USER_WXSESSION_ID,openid );
			session.setAttribute(Constant.USER_LOGIN_TIME, System.currentTimeMillis());
			//Map<String,Object> map = new HashMap<>();
            String sessionId = request.getSession().getId();
			redisTemplate.opsForValue().set("sessionId", sessionId);
			Object sessionId1 = redisTemplate.opsForValue().get("sessionId");
			if(!sessionId.equals(sessionId1)){
				redisTemplate.delete("sessionId");
				redisTemplate.opsForValue().set("sessionId", sessionId);
			}

			List<ViewVo> userAllInfo = viewMapper.getAllUserInfo(null,null,null);
			jsonArea.put("uid", "-1");
			jsonArea.put("type", 20);
			jsonArea.put("data",userAllInfo);//用户统计信息：用户总数
			appletsWebSocket.sendAll(jsonArea.toString());
			logger.info("用户登录，openid：" + openid + ", 会话ID：" + sessionId);
			//map.put("sessionId",sessionId);
			//map.put("userId",userInfo.getId ());
			result.setData(sessionId);
			result.setUserId(userInfo.getId());
			logger.info("调用login方法---->"+ user);

        } catch (AuthenticationException e) {
			result.setCode(400);
			result.setMessage("微信小程序没有绑定用户");
        } catch(Exception e) {
			result.setCode(500);
			result.setMessage("登录失败");
        }

		return result;
	}

	@ResponseBody
	@RequestMapping(value="/getLink")
	public CommonResult<Object> getLink(Integer placeId) {
		CommonResult result = new CommonResult();
		result.setCode(200);
		result.setMessage("获取成功");
		String token=getAccessToken();
		String  openlink=getOpenLink(token,placeId);
		if(openlink!=null)
			result.setData(openlink);
		else {
			result.setMessage("获取失败");
			result.setCode(500);
		}

         return result;
	}


	@ResponseBody
	@RequestMapping(value="/geBarCode")
	public CommonResult<Object> getLink( ) {
		CommonResult result = new CommonResult();
		result.setCode(200);
		result.setMessage("获取成功");
		String token=getAccessToken();
		//String  openlink=getOpenLink(token,placeId);
        /*if(openlink!=null)
			result.setData(openlink);
		else {
			result.setMessage("获取失败");
			result.setCode(500);
		}*/

		return result;
	}


	/**
	 * 短信生成地址1
	 */
	private String getAccessToken() {
		// 获取连接客户端工具
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String entityStr = null;
		try {
			URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/token");
			uriBuilder.addParameter(Constant.KEY_APP_GRANT_TYPE, "client_credential");
			uriBuilder.addParameter(Constant.KEY_APP_ID, Constant.APP_ID);
			uriBuilder.addParameter(Constant.KEY_APP_SECRET, Constant.APP_SECRET);


			HttpGet httpGet = new HttpGet(uriBuilder.build());
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			entityStr = EntityUtils.toString(entity, "UTF-8");

			logger.info("jsCode: "  + ", " + entityStr);
			JSONObject entityJSONObject = JSON.parseObject(entityStr);
			String access_token = null;
			//String sessionKey = null;
			if(entityJSONObject.get("errcode") != null) {
				return null;
			}

			if(entityJSONObject.get("access_token") != null)
				access_token = (String) entityJSONObject.get("access_token");
			//if(entityJSONObject.get("session_key") != null)
			//	sessionKey = (String) entityJSONObject.get("session_key");
			logger.info("access_token:"+access_token);

			return access_token;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 根据地质生成二维码入口
	 */
	private String getQrcode(String accessToken,String  para) {
		// 获取连接客户端工具
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String entityStr = null;
		try {
			URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/wxa/getwxacodeunlimit");
            /*uriBuilder.addParameter("access_token", accessToken);
			uriBuilder.addParameter("is_expire", "true");
			uriBuilder.addParameter("expire_type", "0");
			uriBuilder.addParameter("expire_time", System.currentTimeMillis()/1000+1000+"");
			uriBuilder.addParameter("expire_interval", accessToken);*/
    /*		JSONObject dasd=new JSONObject();
			dasd.put("path",null);
			if(placeId!=null) {
				List<ParkingPlace> data = parkingService.findByAllPlace(placeId, null, null, null, null, null, null, null, null, null, null, null, null, null);
				dasd.put("query", data.size() == 0 ? null : data.get(0));
			}else {
				dasd.put("query", null);
			}
			dasd.put("env_version","develop");*/
			//uriBuilder.addParameter("jump_wxa", dasd.toString());
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("access_token", accessToken));
			formparams.add(new BasicNameValuePair("scene", "map=32"));
			formparams.add(new BasicNameValuePair("env_version", "release"));
    /*		formparams.add(new BasicNameValuePair("expire_time", System.currentTimeMillis()/1000+1000+""));
			formparams.add(new BasicNameValuePair("jump_wxa",  dasd.toString()));*/
			uriBuilder.addParameters(formparams);


			HttpPost httpGet = new HttpPost(uriBuilder.build());
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();

			entityStr = EntityUtils.toString(entity, "UTF-8");

			logger.info("jsCode: "  + ", " + entityStr);
			JSONObject entityJSONObject = JSON.parseObject(entityStr);
			String openlink = null;
			//String sessionKey = null;


			if(entityJSONObject.get("openlink") != null)
				openlink = (String) entityJSONObject.get("openlink");
			//if(entityJSONObject.get("session_key") != null)
			//	sessionKey = (String) entityJSONObject.get("session_key");
			logger.info("openlink:"+openlink);

			return openlink;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 短信生成地址2
	 */
	private String getOpenLink(String accessToken,Integer placeId) {
		// 获取连接客户端工具
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String entityStr = null;
		try {
			URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/wxa/generatescheme");
            /*uriBuilder.addParameter("access_token", accessToken);
			uriBuilder.addParameter("is_expire", "true");
			uriBuilder.addParameter("expire_type", "0");
			uriBuilder.addParameter("expire_time", System.currentTimeMillis()/1000+1000+"");
			uriBuilder.addParameter("expire_interval", accessToken);*/
			JSONObject dasd=new JSONObject();
			dasd.put("path",null);
			if(placeId!=null) {
				List<ParkingPlace> data = parkingService.findByAllPlace(placeId, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
				dasd.put("query", data.size() == 0 ? null : data.get(0));
			}else {
				dasd.put("query", null);
			}
			dasd.put("env_version","release");
			//uriBuilder.addParameter("jump_wxa", dasd.toString());
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("access_token", accessToken));
			formparams.add(new BasicNameValuePair("is_expire", "true"));
			formparams.add(new BasicNameValuePair("expire_type", "0"));
			formparams.add(new BasicNameValuePair("expire_time", String.valueOf(System.currentTimeMillis()/1000+1000)));
			formparams.add(new BasicNameValuePair("jump_wxa",  dasd.toString()));
			uriBuilder.addParameters(formparams);


			HttpPost httpGet = new HttpPost(uriBuilder.build());
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			entityStr = EntityUtils.toString(entity, "UTF-8");

			logger.info("jsCode: "  + ", " + entityStr);
			JSONObject entityJSONObject = JSON.parseObject(entityStr);
			String openlink = null;
			//String sessionKey = null;


			if(entityJSONObject.get("openlink") != null)
				openlink = (String) entityJSONObject.get("openlink");
			//if(entityJSONObject.get("session_key") != null)
			//	sessionKey = (String) entityJSONObject.get("session_key");
			logger.info("openlink:"+openlink);

			return openlink;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}


	/**
	 * 获取微信小程序openid，每个微信用户的唯一id，openid千万不要在公网中传递
	 *
	 * @param jsCode 微信登陆后获取的code
	 * @return 微信用户openid
	 */
	private String getOpenid(String jsCode,String appID) {
		// 获取连接客户端工具
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String entityStr = null;
		try {
			URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/sns/jscode2session");
			if(NullUtils.isEmpty(appID)){
				uriBuilder.addParameter(Constant.KEY_APP_ID,Constant.APP_ID);
				uriBuilder.addParameter(Constant.KEY_APP_SECRET, Constant.APP_SECRET);
			}else {
				uriBuilder.addParameter(Constant.KEY_APP_ID, appID);
				uriBuilder.addParameter(Constant.KEY_APP_SECRET, Constant.LOCATE_APP_SECRET);
			}

			uriBuilder.addParameter(Constant.KEY_APP_GRANT_JS_CODE, jsCode);
			uriBuilder.addParameter(Constant.KEY_APP_GRANT_TYPE, Constant.APP_GRANT_TYPE);

			HttpGet httpGet = new HttpGet(uriBuilder.build());
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			entityStr = EntityUtils.toString(entity, "UTF-8");

			logger.info("jsCode: " + jsCode + ", " + entityStr);
			JSONObject entityJSONObject = JSON.parseObject(entityStr);
			String openid = null;
			//String sessionKey = null;
			if(entityJSONObject.get("errcode") != null) {
				return null;
			}

			if(entityJSONObject.get("openid") != null)
				openid = (String) entityJSONObject.get("openid");
			//if(entityJSONObject.get("session_key") != null)
			//	sessionKey = (String) entityJSONObject.get("session_key");
			logger.info("openid:"+openid);

			return openid;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	//这是一个main方法，程序的入口
	public static void main(String[] args){
		Map<String,Object> map = new HashMap<>();
		map.put("a",1);
		map.put("b",2);
		map.put("c",4);
		System.out.println("map.get(\"a\") = " + map.get("a"));
	}

	/**
	 * 微信小程序退出
	 * @return 退出结果
	 */
	@ResponseBody
	@RequestMapping(value="/logout")
	public Map<String, Object> logout() {
		Map<String, Object> result = new HashMap<>();

		Subject subject = SecurityUtils.getSubject();
		if(subject.getPrincipal() != null ) {
			String wxOpenid = (String) subject.getPrincipal();
			//User currentUser = userService.findUserByWxOpenid(wxOpenid);
			if(wxOpenid != null) {
				//userService.updateUserWxOpenid(currentUser.getId(), null, 0);

				result.put(Constant.KEY_SUCCESS, true);
				result.put(Constant.KEY_MESSAGE, "退出登录成功");
				SecurityUtils.getSubject().logout();
			} else {
				result.put(Constant.KEY_SUCCESS, false);
				result.put(Constant.KEY_ERROR_CODE, Constant.ERROR_CODE_NO_LOGIN);
				result.put(Constant.KEY_ERROR, "用户没有登录");
			}
		}

		return result;
	}

	//@Bean(name = "masterTransactionManager")
	//public PlatformTransactionManager masterTransactionManager (LazyConnectionDataSourceProxy dataSource) {
	//log.info("[获取数据源) LazyConnectionDataSourceProxy=[]",dataSource);
	//DataSourceTransactionManager dataSourceTransactionManager =new DataSourceTransactionManager();
	//dataSourceTransactionManager.setDataSource(dataSource);
	//return dataSourceTransactionManager;
	//}

}
