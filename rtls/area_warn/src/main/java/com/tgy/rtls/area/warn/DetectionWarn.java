package com.tgy.rtls.area.warn;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.user.Person;

import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn
 * @date 2020/10/27
 * 出入口检测判断
 */
public interface DetectionWarn {
    /*
    * 入口判断 state 状态（true在区域内 false不在）  area-->区域信息
    * */
    void entranceWarn(Boolean state, Person person, Area area);

    /*
     * 出口判断 state 状态（true在区域内 false不在）  area-->区域信息
     * */
    void exitWarn(Boolean state, Person person, Area area);
}
