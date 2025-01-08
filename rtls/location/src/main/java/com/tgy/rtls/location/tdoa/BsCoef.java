package com.tgy.rtls.location.tdoa;

import java.util.ArrayList;
import java.util.List;

public class BsCoef {
    public volatile   long formerSynid;
    public volatile   long currentSynid;
    public volatile   long formerTimestamp;
    public volatile   long currentTimestamp;
    public volatile  double coef=0.00469176397861579;
    public volatile  double timediff=0;
    public List timeDiff=new ArrayList();//存储时钟偏差信息

    public void reFreshTimestamp(long rangeid,long timestamp){
        formerSynid=currentSynid;
        formerTimestamp=currentTimestamp;
        currentSynid=rangeid;
        currentTimestamp=timestamp;
    }

}
