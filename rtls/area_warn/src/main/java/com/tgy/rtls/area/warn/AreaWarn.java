package com.tgy.rtls.area.warn;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.user.Person;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn
 * @date 2020/10/30
 * 区域报警
 */
public interface AreaWarn {
    /*
     * 区域超员报警判断 state-->是否在区域内 true 是 false否  area-->区域信息 maxnum-->人数上限 person-->人员信息
     * */
    void overlodaWarn(Boolean state, Area area, Integer maxnum, Person person);

    /*
    * 进出区域记录生成  state-->是否在区域内 true 是 false否  area-->区域信息 person-->人员信息
    * */
    void inAreaRecord(Boolean state,Area area,Integer personid);

    /*
     * 进出分站记录统计 state-->当前人员所在分站和传输数据显示的分站是否为同一个 是true 否false  personid-->人员id  bsid-->分站信息
     * */
     void inSubRecord(boolean state,Integer personid,String bsid,String oldBsid);
}
