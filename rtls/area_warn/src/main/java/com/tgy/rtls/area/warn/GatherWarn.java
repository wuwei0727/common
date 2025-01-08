package com.tgy.rtls.area.warn;

import com.tgy.rtls.data.entity.common.Point2d;
import com.tgy.rtls.data.gather.Gather;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public interface GatherWarn {
 public    Gather getGatherInfo(ConcurrentHashMap<String, LinkedBlockingDeque<Point2d>> personLocation);

}
