package com.tgy.rtls.location.dataprocess;

import com.tgy.rtls.location.model.TagInf;
import com.tgy.rtls.location.tdoa.BsTimestamp;
import com.tgy.rtls.location.tdoa.BsTimestamps;

import java.math.BigDecimal;

public interface TdoaInterface {

    void tdoaDataProcess(TagInf tagInf);
    void storageTimestamp(BsTimestamps bsTimestamps, BsTimestamp bsTimestamp);
    void tdoaLocation(TagInf tagInf);
    void tdoaDataFilter(TagInf tagInf, String[] bsnames, BigDecimal[][] bsT_t , int[] state);



}
