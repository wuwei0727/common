package com.tgy.rtls.location.tdoa;

import java.math.BigDecimal;

public class BsTimestamp {
    public String bid;            //基站ID
    public String host0;        //基站0ID
    public long sync_id0;        //同步ID
    public BigDecimal sync_tx0;        //主基站发送时间戳
    public BigDecimal sync_rx0;        //从基站接收同步时间戳
    //  public long sync_ts0;        //同步时间戳
      /*  public String host1;        //主基站1ID
        public long sync_id1;        //同步ID
        public long sync_ts1;        //同步时间戳*/
    public BigDecimal ping_ts;        //定位时间戳
    public float signal; //信号强度
    public float signal_f; //信号强度
    public BsTimestamp() {
        // TODO Auto-generated constructor stub
    }
}
