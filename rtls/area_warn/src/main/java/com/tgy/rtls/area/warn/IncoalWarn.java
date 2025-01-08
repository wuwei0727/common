package com.tgy.rtls.area.warn;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn
 * @date 2020/10/30
 * 井下报警判断
 */
public interface IncoalWarn {
    /*
     * 井下超员报警判断
     * */
    void incoalOverload(Integer map);

    /*
     * 井下超时报警判断 personid-->人员id
     * */
    void incoalOvertime(Integer personid,Integer map);

    /*
    * 人员离线报警判断
    * */
    void personOffLine(Integer personid,Integer map);
}
