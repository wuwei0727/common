package com.tgy.rtls.location.config.deviceconfig;

import com.tgy.rtls.data.kafukaentity.BsPara;
import com.tgy.rtls.location.struct.UwbRawInfAll;

import java.nio.ByteBuffer;
import java.text.ParseException;


public interface ScreenConfig {
        /*
        发送剩余车位数到显示屏
         */
    public boolean sendEmpty_placeToScreen(Long bsid, Integer empty_place,byte[] CMD, byte addr);



    /**
     * 处理4G_485心跳数据
     * @param bsid
     */

    public Boolean  process4G_485Heart(Long bsid, Integer msgid);



    /*
 发送剩余车位数到显示屏
  */
    public boolean sendEmpty_placeToScreen_S(Long bsid, Integer empty_place, String addr);



    /*
发送数据到网关
*/
    public boolean sendTestToScreen_S(Long bsid);

    /*
发送剩余车位数到显示屏
*/



}
