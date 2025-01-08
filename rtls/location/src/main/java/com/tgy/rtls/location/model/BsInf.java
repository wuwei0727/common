package com.tgy.rtls.location.model;

import com.tgy.rtls.data.algorithm.Location_highway;
import com.tgy.rtls.location.struct.Beacon;
import com.tgy.rtls.location.tdoa.BsCoef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BsInf {

    public short netState=1;
    public  double x,y,z;
    public  double x1,y1,z1;
    public  double x2,y2,z2;
    public int ping_number,Crystal_msg_number;
    public  boolean xupdate_flag=false;
    public  ConcurrentHashMap<String,BsRangeInfo> bsrange=new ConcurrentHashMap<String , BsRangeInfo>(); //bsid-dis
    public  ConcurrentHashMap<String,BsTimediff> bsTimeDiff=new ConcurrentHashMap<String , BsTimediff>(); //bsid-timediff

    public  ConcurrentHashMap<String, BsCoef> bsCoef=new ConcurrentHashMap<String , BsCoef>(); //bs coef
    public volatile Beacon beacon=null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public short msg_id=0;
    public String sharebs="";
    public String disfix="";
    public String disfixl="";
    public String disfixr="";
    public double antenna_delay=77.82;
    public double antenna_delayl=0;
    public double antenna_delayr=0;
    public double coef=0.00469176397861579;
    public int count;
    public long initialid;
    public long current;
    public String region = "";   //
    public String percent="";
    public int heartid=0;

    public void CalculateCoef(String src,String target){
        BsCoef beaconSrc=null;
        BsCoef beaconTarget=null;
        beaconSrc = bsCoef.get(src);
        if(!src.equals(target)) {
            beaconTarget = bsCoef.get(target);
            calculateCoef(src,target,beaconSrc,beaconTarget);
        }else {
            Set<Map.Entry<String, BsCoef>> set = bsCoef.entrySet();
            for (Map.Entry entry : set
            ) {
                String bsname = ((String) entry.getKey());
                if (!bsname.equals(src)) {
                    beaconTarget = ((BsCoef) entry.getValue());
                }
                calculateCoef(src, bsname, beaconSrc, beaconTarget);
            }
        }
    }

    void calculateCoef(String src,String target,BsCoef beaconSrc,BsCoef beaconTarget){
        if(beaconSrc==null|| beaconTarget==null)
            return;
        double coef = 0.00469176397861579;
        if(beaconSrc.formerSynid==beaconTarget.formerSynid){
            BsTimediff ti = bsTimeDiff.get(src);
            if(ti==null){
                ti=new BsTimediff();
                bsTimeDiff.put(src,ti);
            }
            ti.addTxTimestamp(beaconSrc.formerTimestamp,beaconTarget.formerTimestamp,src,target);
        }
        if(beaconSrc.formerSynid==beaconTarget.formerSynid&&beaconTarget.currentSynid==beaconSrc.currentSynid){
            Double diffSrc= Location_highway.getdiff(beaconSrc.currentTimestamp,beaconSrc.formerTimestamp);
            Double diffTarget= Location_highway.getdiff(beaconTarget.currentTimestamp,beaconTarget.formerTimestamp);
            Double abs_dif_former=Location_highway.getdiff(beaconSrc.formerTimestamp,beaconTarget.formerTimestamp);
            double abs_dif_current=Location_highway.getdiff(beaconSrc.currentTimestamp,beaconTarget.currentTimestamp);
        //    logger.error( src+":src beaconid:"+beaconTarget.formerSynid+ ":formertime:" +beaconSrc.formerTimestamp+":"+beaconSrc.currentTimestamp );
        //    logger.error( target+":target beaconid:"+beaconTarget.formerSynid+ ":formertime:" +beaconTarget.formerTimestamp+":"+beaconTarget.currentTimestamp );
           /* logger.error( src+":src beaconid:"+beaconTarget.formerSynid+ ":diff ticount:" +diffSrc.longValue()+"" );
            logger.error( src+":src beaconid:"+beaconTarget.formerSynid+ ":diff ticount:" +diffSrc.longValue()+"" );*/
         //   logger.error( target+":target beaconid:"+beaconTarget.formerSynid+ ":diff ticount:" +diffTarget.longValue()+"" );
           // logger.error( src+":diff:"+target+ ":diff ticount:" +(diffSrc.longValue()-diffTarget.longValue()));
          //  logger.error( src+":diff:"+target+ ":diff ticount:" +(diffSrc.longValue()-diffTarget.longValue())/diffSrc);

            //  logger.error( src+":"+target+":beaconid:"+beaconTarget.formerSynid+ ":diff ticount:" +(diffSrc-diffTarget)+":percent:"+(diffSrc/diffTarget) );
            // logger.error( src+":"+target+":beaconid:"+beaconTarget.formerSynid+ ":diff tic:" +(diffSrc));
            beaconTarget.timediff=abs_dif_former;
            if(diffTarget > 1)
            {
                double slaveCoef = diffSrc / diffTarget * coef;
                if(!(slaveCoef > 0.00469178d || slaveCoef < 0.00469175d)) {
                    beaconTarget.coef = slaveCoef;
                  //  logger.error(target + ":coef:" + slaveCoef);
                }
            }
        }
    }
}
