package com.tgy.rtls.area.warn;

import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.user.Person;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn
 * @date 2020/11/3
 * 设备报警相关
 */
public interface EquipWarn {
    /*
    * sos报警 sos-->0不报警 1报警
    * */
    void sosWarn(Person person, Integer sos);
    /*
    * 低电量报警 power-->电量
    * */
    void powerWarn(Person person, double power);

    /*
    * 分站离线报警 state-->true报警  false结束报警
    * */
    void subNetworkstate(Boolean state,Substation substation);

    /*
     * 分站主供电异常报警 state-->true报警  false结束报警
     * */
    void subPowerstate(Boolean state,Substation substation);

    /*
    * 分站超员报警 人员进出分站时触发
    * */
    void subOverload(String num, Integer map);
}
