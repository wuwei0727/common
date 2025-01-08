package com.tgy.rtls.location.dataprocess;

import com.tgy.rtls.data.algorithm.DisSort;
import com.tgy.rtls.data.kafukaentity.BsPara;
import com.tgy.rtls.data.kafukaentity.TagPara;
import com.tgy.rtls.location.struct.RailWay2D;
import com.tgy.rtls.location.struct.RailWay2D_8bs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public interface ProcessInterface {

    public    void processCoalHeartData(Long bsid, ByteBuffer data);
    public    void process2D_4bs(Long bsid,ByteBuffer data) throws IOException;
    public    void process2D_8bs(Long bsid,ByteBuffer data)throws IOException;

    DisSort calculPos(ArrayList<Double[]> bsposList, ArrayList<Double> bsposDis, int calculateBs);
   // public static   DisSort calculPos_weight( ArrayList<Double[]> bsposList, ArrayList<Double> bsposDis,int calculateBs,int strict);
    RailWay2D readData_4bs(ByteBuffer data);
    RailWay2D_8bs readData_8bs(ByteBuffer data);
    public    void processSingleBsRange(Long bsid,ByteBuffer data) throws IOException;
    public void processBsError(Long bsid,ByteBuffer data);
    public    void processTagDataUplink(Long bsid,int tagid,ByteBuffer data,short len)throws IOException;
    public    void processFileSendRes(Long bsid,ByteBuffer data);
    public void processFilePushStatus(Long bsid,ByteBuffer data,int type);
    public void processFileUpload(Long bsid,ByteBuffer data,int type);
    void processFileUpResponse(long bsid,long tagid,int pkgid,int type,int fileType,int finishFileId);
    public void processBsPowerRes(Long bsid,ByteBuffer data);
    public void processBsRange(Long bsid,ByteBuffer data);
    public void processBsparaConfig(BsPara bsPara);
    public void processBsBackGround(Long bsid,ByteBuffer data);
    public void processBsOldTimeAndDis(Long bsid,ByteBuffer data);
    public void processBsRelay(Long bsid,ByteBuffer data);
    public void processBsErrorCodeTest(Long bsid,ByteBuffer data);
    public void processBsWord(Long bsid,ByteBuffer data);
    public void processBsLocationWord(Long bsid,ByteBuffer data);
    public void processBsLocpara(Long bsid,ByteBuffer data);
    public void processBsWarning(Long bsid,ByteBuffer data);
    public void processBsBeep(Long bsid,ByteBuffer data);
    public void processTagparaConfig(TagPara tagPara);
    void tagCheck(long tagid,long bsid);
    /*
    滤波器
     */
   public double[]	getWeightRes(double[] former,double[] current);

   public DisSort chooseTwoBsFromMutipleBs( ArrayList<Double[]> bsPos, ArrayList<Double> Dis,boolean onlyTwoBs);







}
