package com.tgy.rtls.data.entity.userinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author rtls
 * @since 2020-11-13
 */
@Data
public class WechatUserInfo  implements Serializable  {


    private Long id;
    private Integer userid;
    private String nickName="微信用户";
    private String  avatarUrl="https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132";//用户头像，最后一个数值代表正方形头像大小（有 0、46、64、96、132 数值可选，0 代表 132*132 正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像 URL 将失效。
    private Short gender=0;//用户的性别，值为 1 时是男性，值为 2 时是女性，值为 0 时是未知
    private String city;//城市
    private String province;//省份
    private String country;//国家
    private String version="8.0.47";//微信版本
    private String phone;//手机号
    private String cell_phone;//手机型号
    private String brand="Xiaomi";//手机型号
    private String model="12 Pro u";//手机型号
    private String platform="android";//手机型号

    private Integer userCount;
    private Integer man;
    private Integer Female;
    private Integer unknown;
    private Integer num;




}
