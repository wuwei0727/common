package com.tgy.rtls.web.controller.user;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.user.MenuVO;
import com.tgy.rtls.data.service.user.impl.PermissionService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * 用于登录
 * */

@Controller
public class HomeController {
    @Autowired
    private PermissionService permissionService;
    @RequestMapping("/page/home")
    public String home(Model model) {
        List<MenuVO> menus = permissionService.getAllMenusWithPermissions();
        model.addAttribute("menus", menus);
        return "page/home";
    }

    @RequestMapping("/page/view")
    public String view() {
        return "page/view";
    }

    @RequestMapping(value = "/page/tologin")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        // 登录失败从request中获取shiro处理的异常信息。
        // shiroLoginFailure:就是shiro异常类的全类名.
        try {
            response.setContentType("text/html; charset=UTF-8");
            response.setHeader("Cache-Control", "max-age=18000");
            String exception = (String) request.getAttribute("shiroLoginFailure");
            // System.out.println("shiroExceptions-------" + exception);
            String msg = "";
            if (exception != null) {
                if (UnknownAccountException.class.getName().equals(exception)) {
                    msg = LocalUtil.get(KafukaTopics.userNameError);
                } else if (AuthenticationException.class.getName().equals(exception)) {
                    msg = LocalUtil.get(KafukaTopics.userNamePasswordError);
                } else if (DisabledAccountException.class.getName().equals(exception)) {
                    msg = LocalUtil.get(KafukaTopics.AccountNotEna);
                } else if (IncorrectCredentialsException.class.getName().equals(exception)) {
                    msg = LocalUtil.get(KafukaTopics.PASSWORD_ERROR1);
                } else if ("kaptchaValidateFailed".equals(exception)) {
                    msg = LocalUtil.get(KafukaTopics.VCODE_ERROR);
                } else {
                    msg = exception;
                }
                PrintWriter out = response.getWriter();
                out.flush();
                out.println("<script>alert('" + msg + "')</script>");
                return "/page/login";
            } else {
                String sdas = "/page/login";
                Subject subject = SecurityUtils.getSubject();
                boolean authed = subject.isAuthenticated();
                if (authed) {
                    sdas = "/page/home";

                } else {

                }

                return sdas;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "/page/login";
        }
        // 此方法不处理登录成功,由shiro进行处理
    }

    @RequestMapping("/403")
    public String unauthorizedRole() {
        System.out.println("------没有权限-------");
        return "403";
    }

    /**
     * 权限不足
     *
     * @return
     */

    //@RequestMapping("/page/unauth")
    //@ResponseBody
    //public Object unauth() {
    //    return new CommonResult(403, "您没有访问权限！请联系管理员！！！");
    //}
    @RequestMapping("/page/unauth")
    public String toUnAuth(HttpServletRequest request, HttpServletResponse response) {
        return "/page/unauth";
    }

    @ResponseBody
    @RequestMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        try {
            Subject subject = SecurityUtils.getSubject();
            subject.logout();

            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies
            ) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }

            map.put("status", true);
            map.put("msg", "注销成功");
            map.put("path", "page/tologin");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", false);
            map.put("msg", "出现错误");
            return map;
        }
    }


    @RequestMapping("/page/videoDataList")
    public String videoDataList() {
        return "/page/videoDataList";
    }
    @RequestMapping("/page/videoDataDetail")
    public String videoDataDetail() {
        return "/page/videoDataDetails";
    }
    @RequestMapping("/page/originalVideoData")
    public String vehicleDataList() {
        return "/page/originalVideoData";
    }
    @RequestMapping("/page/licensePlateBind")
    public String licensePlateBind() {
        return "/page/licensePlateBind";
    }

    @RequestMapping("/page/licensePlateBindDetail")
    public String licensePlateBindDetail() {
        return "/page/licensePlateBindDetail";
    }

    @RequestMapping("/page/timeManagement")
    public String timeManagement() {
        return "/page/timeManagement";
    }

    @RequestMapping("/page/timeManagementDetail")
    public String timeManagementDetail() {
        return "/page/timeManagementDetail";
    }

    @RequestMapping("/page/unlockInfo")
    public String unlockInfo() {
        return "/page/unlockInfo";
    }

//    @RequestMapping("/page/unlockInfoDetails")
//    public String unlockInfoDetails() {
//        return "/page/unlockInfoDetails";
//    }

    @RequestMapping("/page/exclusiveUser")
    public String exclusiveUser() {
        return "/page/exclusiveUser";
    }
//    @RequestMapping("/page/exclusiveUserDetails")
//    public String exclusiveUserDetails() {
//        return "/page/exclusiveUserDetails";
//    }

    @RequestMapping("/page/floorLockConfig")
    public String floorLockConfig() {
        return "/page/floorLockConfig";
    }

    @RequestMapping("/page/floorLockConfigDetails")
    public String floorLockConfigDetails() {
        return "/page/floorLockConfigDetails";
    }
    @RequestMapping("/page/promotionQRcodelist")
    public String promotionQRcodelist() {
        return "/page/promotionQRcodelist";
    }

    @RequestMapping("/page/promotionQRcodeDetail")
    public String promotionQRcodeDetail() {
        return "/page/promotionQRcodeDetail";
    }

    @RequestMapping("/page/promotion")
    public String promotion() {
        return "/page/promotion";
    }

    @RequestMapping("/page/promotionDetail")
    public String promotionDetail() {
        return "/page/promotionDetail";
    }

    @RequestMapping("/page/textmessageRecharge")
    @RequiresPermissions("sqa:rer")
    public String textmessageRecharge() {
        return "/page/textmessageRecharge";
    }

 @RequestMapping("/page/cpConfigDetails")
    public String cpConfigDetails() {
        return "/page/cpConfigDetails";
    }

    @RequestMapping("/page/callPolice")
    public String callPolice() {
        return "/page/callPolice";
    }
    @RequestMapping("/page/callPoliceConfig")
    public String callPoliceConfig() {
        return "/page/callPoliceConfig";
    }

    @RequestMapping("/page/textmessage")
    public String textmessage() {
        return "/page/textmessage";
    }
    @RequestMapping("/page/textmessageDetails")
    public String textmessageDetails() {
        return "/page/textmessageDetails";
    }
    @RequestMapping("/page/outdoorObjects")
    public String outdoorObjects() {
        return "/page/outdoorObjects";
    }
    @RequestMapping("/page/outdoorObjectsDetails")
    public String outdoorObjectsDetails() {
        return "/page/outdoorObjectsDetails";
    }
    @RequestMapping("/page/serviceFacilities")
    public String serviceFacilities() {
        return "/page/serviceFacilities";
    }
    @RequestMapping("/page/serviceFacilitiesDetails")
    public String serviceFacilitiesDetails() {
        return "/page/serviceFacilitiesDetails";
    }
    @RequestMapping("/page/accessMouthList2")
    public String accessMouthList2() {
        return "/page/accessMouthList2";
    }
    @RequestMapping("/page/accessMouthListDetail2")
    public String accessMouthListDetail2() {
        return "/page/accessMouthListDetail2";
    }
    @RequestMapping("/page/accessMouthList")
    public String accessMouthList() {
        return "/page/accessMouthList";
    }
    @RequestMapping("/page/accessMouthListDetail")
    public String accessMouthListDetail() {
        return "/page/accessMouthListDetail";
    }
    @RequestMapping("/page/park/map")
    public String parkMap() {
        return "/page/park/map";
    }
    @RequestMapping("/page/elevator")
    public String elevator() {
        return "/page/elevator";
    }
    @RequestMapping("/page/elevatorDetails")
    public String elevatorDetails() {
        return "/page/elevatorDetails";
    }

    /**
     * 去车位规划页面
     *
     * @return
     */
    @RequestMapping("/page/place")
    public String place() {
        return "page/place";
    }

    /**
     * 去车位规划编辑页面
     *
     * @return
     */
    @RequestMapping("/page/placeDetails")
    public String placeDetails() {
        return "page/placeDetails";
    }

    /**
     * 去vip车位管理页面
     */
    @RequestMapping("/page/vipPlace")
    public String vipPlace() {
        return "page/vipPlace";
    }

    /**
     * 去vip车位编辑页面
     */
    @RequestMapping("/page/vipPlaceDetails")
    public String vipPlaceDetails() {
        return "page/vipPlaceDetails";
    }


    @RequestMapping("/page/vipArea")
    public String vipArea() {
        return "page/vipArea";
    }

    @RequestMapping("/page/vipAreaDetails")
    public String vipAreaDetails() {
        return "page/vipAreaDetails";
    }

    @RequestMapping("/page/placeVideo")
    public String placeVideo() {
        return "page/placeVideo";
    }

    @RequestMapping("/page/placeVideoDetail")
    public String placeVideoDetail() {
        return "page/placeVideoDetail";
    }

    @RequestMapping("/page/recommend")
    public String recommend() {
        return "page/recommend";
    }

    @RequestMapping("/page/recommendDetail")
    public String recommendDetail() {
        return "page/recommendDetail";
    }

    @RequestMapping("/page/floorLock")
    public String floorLock() {
        return "page/floorLock";
    }

    @RequestMapping("/page/floorLockDetails")
    public String floorLockDetails() {
        return "page/floorLockDetails";
    }

    @RequestMapping("/page/barrier")
    public String barrier() {
        return "page/barrier";
    }

    @RequestMapping("/page/barrierDetails")
    public String barrierDetails() {
        return "page/barrierDetails";
    }

    @RequestMapping("/page/guideScreen")
    public String guideScreen() {
        return "page/guideScreen";
    }

    @RequestMapping("/page/guideScreenDetail")
    public String guideScreenDetail() {
        return "page/guideScreenDetail";
    }
    @RequestMapping("/page/displayScreen")
    public String displayScreen() {
        return "page/displayScreen";
    }

    @RequestMapping("/page/displayScreenDetail")
    public String displayScreenDetail() {
        return "page/displayScreenDetail";
    }

    /**
     * @return 去地图路径标注
     */
    @RequestMapping("/page/mapPathLabels")
    public String mapPathLabels() {
        return "page/mapPathLabels";
    }
    @RequestMapping("/page/mapPathLabelsDetails")
    public String mapPathLabelsDetails() {
        return "page/mapPathLabelsDetails";
    }

    /**
     * 去公司管理页面
     *
     * @return
     */
    @RequestMapping("/page/company")
    public String company() {
        return "page/company";
    }

    /**
     * 去公司管理编辑页面
     *
     * @return
     */
    @RequestMapping("/page/companyDetails")
    public String companyDetails() {
        return "page/companyDetails";
    }

    /**
     * 去通道违停页面
     *
     * @return
     */
    @RequestMapping("/page/violation")
    public String violation() {
        return "page/violation";
    }

    /**
     * 去通道违停详情页面
     *
     * @return
     */
    @RequestMapping("/page/violationDetails")
    public String violationDetails() {
        return "page/violationDetails";
    }

    /**
     * 去商家信息页面
     *
     * @return
     */
    @RequestMapping("/page/business")
    public String business() {
        return "page/business";
    }

    /**
     * 去商家信息编辑页面
     *
     * @return
     */
    @RequestMapping("/page/businessDetails")
    public String businessDetails() {
        return "page/businessDetails";
    }

    /**
     * 去蓝牙信标页面
     *
     * @return
     */
    @RequestMapping("/page/baseStation")
    public String baseStation() {
        return "page/baseStation";
    }

    /**
     * 去蓝牙信标详情页面
     *
     * @return
     */
    @RequestMapping("/page/baseStationDetails")
    public String baseStationDetails() {
        return "page/baseStationDetails";
    }

    /**
     * 去网关页面
     *
     * @return
     */
    @RequestMapping("/page/gateway")
    public String gateway() {
        return "page/gateway";
    }

    /**
     * 去网关详情页面
     *
     * @return
     */
    @RequestMapping("/page/gatewayDetails")
    public String gatewayDetails() {
        return "page/gatewayDetails";
    }

    /**
     * 去车位检测器页面
     *
     * @return
     */
    @RequestMapping("/page/detector")
    public String detector() {
        return "page/detector";
    }

    /**
     * 去车位检测器详情页面
     *
     * @return
     */
    @RequestMapping("/page/detectorDetails")
    public String detectorDetails() {
        return "page/detectorDetails";
    }

    /**
     * 去地图管理页面
     *
     * @return
     */
    @RequestMapping("/page/map")
    public String map() {
        return "page/map";
    }

    /**
     * 去地图管理编辑页面
     *
     * @return
     */
    @RequestMapping("/page/mapDetails")
    public String mapDetails() {
        return "page/mapDetails";
    }
    @RequestMapping("/page/parkingLotCost")
    public String parkingLotCost() {
        return "page/parkingLotCost";
    }
    @RequestMapping("/page/parkingLotCostDetails.html")
    public String parkingLotCostDetails() {
        return "page/parkingLotCostDetails";
    }

    /**
     * 去人员账户管理页面
     *
     * @return
     */
    @RequestMapping("/page/member")
    public String member() {
        return "page/member";
    }

    /**
     * 去人员账户管理权限设置页面
     *
     * @return
     */
    @RequestMapping("/page/permissSet")
    public String permissSet() {
        return "page/permissSet";
    }

    /**
     * 去人员账户管理编辑页面
     *
     * @return
     */
    @RequestMapping("/page/memberDetails")
    public String memberDetails() {
        return "page/memberDetails";
    }

    /**
     * 去账户权限管理页面
     *
     * @return
     */
    @RequestMapping("/page/permiss")
    public String permiss() {
        return "page/permiss";
    }
    @RequestMapping("/page/permissDetail")
    public String permissDetail() {
        return "page/permissDetail";
    }

    /**
     * 去移动端用户管理
     *
     * @return
     */
    @RequestMapping("/page/mapmember")
    public String mapmember() {
        return "page/mapmember";
    }
    @RequestMapping("/page/mapmemberDetails")
    public String mapmemberDetails() {
        return "page/mapmemberDetails";
    }

    /**
     * 去登录日志页面
     *
     * @return
     */
    @RequestMapping("/page/loginLog")
    public String loginLog() {
        return "page/loginLog";
    }

    /**
     * 中石化页面跳转
     *
     * @param info
     * @param url
     * @param resp
     * @return
     */
    @RequestMapping("/page/page")
    public String sinopecPage(String info, String url, HttpServletResponse resp) {
        System.out.println("info:" + info);
        System.out.println("url:" + url);
        /* if(info.indexOf("login")!=-1) */
        {
            try {
                resp.sendRedirect(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info;
    }
}