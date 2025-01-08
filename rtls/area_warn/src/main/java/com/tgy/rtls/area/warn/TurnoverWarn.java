package com.tgy.rtls.area.warn;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.user.Person;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn
 * @date 2020/10/27
 * 进出报警判断
 */
public interface TurnoverWarn {
    /*
    * 进入区域规则判断 state-->是否在区域  person-->人员信息 area-->区域信息 turnoverid-->进出区域规则id
    * */
    void intoWarn(Boolean state, Person person, Area area,Integer turnoverid);

    /*
    * 离开区域规则判断  state-->是否在区域  person-->人员信息 area-->区域信息 turnoverid-->进出区域规则id
    * */
    void outWarn(Boolean state, Person person,Area area,Integer turnoverid);
}
