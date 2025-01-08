package com.tgy.rtls.check.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.check.Utils.CheckDataProcess;
import com.tgy.rtls.check.entity.TagValidate;
import com.tgy.rtls.check.excel.ExcelUtils;
import com.tgy.rtls.check.excel.FileUtils;
import com.tgy.rtls.check.kafka.KafukaSender;
import com.tgy.rtls.data.common.ByteUtils;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.check.BserrorcodetestrecordEntity;
import com.tgy.rtls.data.entity.check.TagcheckEntity;
import com.tgy.rtls.data.entity.check.TagcheckbsidEntity;
import com.tgy.rtls.data.entity.check.TagchecklocationEntity;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.kafukaentity.BsPara;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.kafukaentity.TagPara;
import com.tgy.rtls.data.mapper.check.*;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.location.LocationMapper;
import com.tgy.rtls.data.snowflake.AutoKey;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin
@RequestMapping("/bstagcheck")
public class TagCheck {


    @Autowired(required = false)
    TagcheckDao tagcheckDao;
    @Autowired(required = false)
    TagcheckbsidDao tagcheckbsidDao;

    @Autowired(required = false)
    LocationMapper locationMapper;

    @Autowired(required = false)
    TagchecklocationDao tagchecklocationDao;
    @Autowired
    CheckDataProcess checkDataProcess;
    @Autowired
    KafukaSender kafukaSender;
    @Autowired(required = false)
    BserrorcodetestDao bserrorcodetestDao;
    @Autowired(required = false)
    BserrorcodetestrecordDao bserrorcodetestrecordDao;
    @Autowired(required = false)
    SubMapper subMapper;
    @Autowired
    AutoKey autoKey;
    public static ConcurrentHashMap<String, TagValidate> tagState = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> bsState = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, TagLocation> filterRes = new ConcurrentHashMap<String, TagLocation>();
    public static ConcurrentHashMap<String, Boolean> tagCapacities = new ConcurrentHashMap<>();
    public static volatile short addFile = 0;
    public static Date sendDate;
    boolean sendMode = true;
    int period_mode = 100;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 开始检卡
     *
     * @param bslist
     * @param file
     * @return
     */
    @RequestMapping(value = "/startcheck")
    @Transactional
    public DeferredResult startCheck(String bslist, Integer type, Integer count, Integer interval, Integer finishType, Integer checkTime, MultipartFile file) {
        CommonResult response = new CommonResult<>();
        DeferredResult defer;
        if(bslist==null|| bslist.trim().isEmpty()){
            response.setMessage("基站不能为空");
            response.setCode(404);
            DeferredResult defer1 = new DeferredResult(2000l);
            defer1.setResult(response);
            return defer1;
        }
        String[] bs=bslist.split(",");
        Boolean bsOffline=true;
        for (String s:bs
             ) {
            if(ByteUtils.isInteger(s)){
                Substation sub = subMapper.findByNum(s,"name");
                if (sub != null && sub.getNetworkstate() == 1){

                }else{
                    bsOffline=false;
                }
            }
        }
        if(!bsOffline){
            response.setMessage("基站离线");
            response.setCode(404);
            DeferredResult defer1 = new DeferredResult(2000l);
            defer1.setResult(response);
            return defer1;
        }

        finishNotEndCheck();
        response.setCode(200);
        response.setMessage(KafukaTopics.ADD_SUCCESS);

        if (addFile != 0) {
            defer = new DeferredResult(5000l);
            ;
            response.setCode(404);
            addFile = 0;
            response.setMessage("已经为您结束上次还有未完成的测试，请再次点击开始按钮开始本次测试");
            defer.setResult(response);
            return defer;
        }

        if (type != 3)
            defer = new DeferredResult(5000l);
        else
            defer = new DeferredResult(5000l);
        filterRes.clear();

        String res = null;

        switch (type) {
            case 5://基站误码率测试
                Substation sub = subMapper.findByNum(bslist,"name");
               if (sub != null && sub.getNetworkstate() == 1) {
                //if(true){
                    bserrorcodetestDao.truncateTest();
                    res = startErrorCodeCheck(bslist, type, count, interval);

                    response.setData(res);
                } else {
                    response.setMessage("基站离线");
                    response.setCode(404);
                }
                defer.setResult(response);
                break;
            default://检卡以及精度测试
                //   addFile=true;
                addFile = 1;
                res = startChecktag(bslist, type, finishType, checkTime, file, response);

                defer.onTimeout(new Runnable() {
                    @Override
                    public void run() {
                        //  addFile=false;
                        System.out.println("返回结果，开始计数" + filterRes.size());
                        defer.setResult(response);
                        addFile = 2;

                    }
                });

                break;
        }

        //   defer.setResult(response);
        return defer;
        // return response;
    }


    /**
     * 结束检卡
     *
     * @param tagcheckid
     */
    @RequestMapping(value = "/endcheck")
    public CommonResult endCheck(Long tagcheckid, Short type) {
        TagCheck.addFile = 0;
        filterRes.clear();
        if (type != 5) {
            List<TagcheckbsidEntity> list = tagcheckbsidDao.getByTagcheckid(tagcheckid, null);
            Long time = (new Date().getTime());

            for (TagcheckbsidEntity tag : list
            ) {
              /*  tag.setEnd(new Date());
                if (tag.getStart() != null && tag.getState() == 1)
                    tag.setPeriod((int) (time - tag.start.getTime()));*/
              //  logger.info("dadas"+tag.getId()+"--"+tag.getLackpercent());
               // tagcheckbsidDao.updateCheck(tag);
                userEndTagCheck(tagcheckid);
            }
        } else {
            BsPara bs = new BsPara();
            bs.setMessageid(tagcheckid.intValue());
            bs.setKeyOrder("errorcodetest");
            bs.setType((short) 0);
            List<BserrorcodetestrecordEntity> listc = bserrorcodetestrecordDao.getByTagCheckid((long) tagcheckid);
            if (listc != null && listc.size() > 0) {
                BserrorcodetestrecordEntity res1 = listc.get(0);
                res1.setEnd(new Date());
            }
            kafukaSender.send(KafukaTopics.BS_ERRORCODETEST, bs.toString());
        }
        CommonResult response = new CommonResult<>();
        response.setCode(200);
        response.setMessage("结束成功");
        response.setData(tagcheckid);
        return response;

    }


    /**
     * 结束检卡
     *
     * @param tagcheckid
     */
    @RequestMapping(value = "/deletetagcheckid")
    public CommonResult delete(Long tagcheckid) {

        tagcheckbsidDao.deleteByTagCheckid(tagcheckid);
        bserrorcodetestrecordDao.deleteByTagCheckid(tagcheckid);
        CommonResult response = new CommonResult<>();
        response.setCode(200);
        response.setMessage(LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        response.setData(tagcheckid);
        return response;
    }


    /**
     * 获取每次检查记录
     *
     * @param tagcheckid
     * @param type
     * @return
     */
    @RequestMapping(value = "/getcheckres")
    public CommonResult getCheckRes(Long tagcheckid, Integer type) {

        CommonResult response = new CommonResult<>();
        response.setCode(200);
        response.setMessage("获取成功");
        switch (type) {

            case 5:
                if (tagcheckid != null) {
                    BserrorcodetestrecordEntity res = bserrorcodetestDao.getByTagCheckId(tagcheckid);
                    if (res != null) {
                        JSONArray array = new JSONArray();
                        JSONArray arrays = new JSONArray();
                        res.setLost(res.getSendnum() - res.getReceivenum());
                        res.setErrornum(res.getErrornum());
                        res.setLostrate(res.getLost() / (double) res.getSendnum());
                        res.setErrorrate(res.getErrornum() / (double) res.getSendnum());
                        array.add(res);
                        JSONObject obj = new JSONObject();
                        obj.put("tagcheckid", tagcheckid);
                        obj.put("list", array);
                        arrays.add(obj);
                        response.setData(arrays);
                    }
                } else {
                    List<BserrorcodetestrecordEntity> list = bserrorcodetestrecordDao.getByTagCheckid(tagcheckid);
                    JSONArray array2 = new JSONArray();
                    for (int i = 0; i < list.size(); i++) {
                        JSONArray array = new JSONArray();
                        BserrorcodetestrecordEntity record = list.get(i);
                        JSONObject obj = new JSONObject();
                        array.add(record);
                        obj.put("tagcheckid", record.getTagcheckid());
                        obj.put("list", array);
                        array2.add(obj);
                    }
                    response.setData(array2);

                }

                break;
            default:
                List<TagcheckbsidEntity> res1 = tagcheckbsidDao.getByTagcheckid(tagcheckid, type);
                Map map = new ListOrderedMap();
                JSONArray array = new JSONArray();
                Date current = new Date();
                Boolean endFlag = false;
                for (TagcheckbsidEntity tag : res1
                ) {
                    Long key = tag.getTagcheckid();
                    JSONArray value = (JSONArray) map.get(key);
                    if (value == null) {
                        value = new JSONArray();
                        map.put(key, value);
                    }
                    int delay = 0;
                    if (tag.start != null)
                        delay = (int) (current.getTime() - tag.start.getTime()) / 1000;
                    if (tag.getChecktime() > 0 && delay >= tag.getChecktime() && tag.getState() == 1) {
                        //tag.setState(0);
                        tag.setPeriod(delay * 1000);
                        tag.setEnd(current);
                        logger.error("sdasd"+tag.getId()+":"+tag.getLackpercent());
                        tagcheckbsidDao.updateById(tag);
                        endFlag = true;
                        addFile = 0;
                        System.out.println("filterRes.clear()");
                        filterRes.clear();

                    }
                    value.add(tag);

                }
                if (endFlag)
                    userEndTagCheck(tagcheckid);
                Iterator iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    long key = (Long) entry.getKey();
                    JSONArray value = (JSONArray) entry.getValue();
                    JSONObject obj = new JSONObject();
                    obj.put("tagcheckid", key);
                    obj.put("list", value);
                    array.add(obj);
                }
                response.setData(array);

                break;

        }


        return response;

    }

    /**
     * 查历史轨迹
     *
     * @param tagcheckid
     * @return
     */
    @RequestMapping(value = "/getalllocation")
    public CommonResult getAllLocation(Integer tagcheckid) {
        List<TagchecklocationEntity> list = tagchecklocationDao.getByTagcheckbsid_All(tagcheckid);
        CommonResult response = new CommonResult<>();
        response.setCode(200);
        response.setMessage(LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        response.setData(list);
        return response;

    }

    /**
     * 查最新的轨迹
     *
     * @param tagcheckid
     * @return
     */
    @RequestMapping(value = "/getrecentlocation")
    public CommonResult getrecentLocation(Integer tagcheckid) {
        List<TagchecklocationEntity> list = tagchecklocationDao.getByTagcheckbsid_Recent(tagcheckid);
        CommonResult response = new CommonResult<>();
        response.setCode(200);
        response.setMessage("获取成功");
        response.setData(list);
        return response;

    }


    @RequestMapping(value = "/test")
    public Long strtCheck() {
        //checkDataProcess.processTagLocation(tag);
        return autoKey.getAutoId("");
    }


    /**
     * 导出 列表
     *
     * @param response
     * @param tagcheckid
     */
    @RequestMapping(value = "/outexcel")
    public CommonResult startCheck(HttpServletResponse response, Integer tagcheckid, Integer type) {
        List<?> list = new ArrayList<>();
        CommonResult commonResult = new CommonResult();
        commonResult.setMessage("下载成功");
        commonResult.setCode(200);
        try {
            String name = "";
            switch (type) {
                case 0:
                    name = "singleBsTest";
                    list = tagcheckbsidDao.getByTagcheckid(tagcheckid.longValue(), type);
                    break;
                case 1:
                    list = tagcheckbsidDao.getByTagcheckid(tagcheckid.longValue(), type);
                    name = "multipleBsTest";
                    break;
                case 2:
                    list = tagcheckbsidDao.getByTagcheckid(tagcheckid.longValue(), type);
                    name = "locationTest";
                    break;
                case 3:
                    list = tagcheckbsidDao.getByTagcheckid(tagcheckid.longValue(), type);
                    name = "inspectTest";
                    break;
                case 4:
                    name = "locationData";
                    list = tagchecklocationDao.getByTagcheckbsid_All(tagcheckid);
                    break;
                case 5:
                    name = "errorCodetest";
                    list = bserrorcodetestrecordDao.getByTagCheckid(tagcheckid.longValue());
                    break;

            }
            FileUtils.createUserExcelFile(response, list, name + "_test_ID:" + tagcheckid);
        } catch (Exception e) {
            e.printStackTrace();
            commonResult.setMessage("下载失败");
            commonResult.setCode(404);
        }
        return commonResult;

    }


    @RequestMapping(value = "/analysisTagcheck")
    @ApiOperation(value = "检卡结果测试", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "startDate", defaultValue = "2021-03-23 17:46:00", required = true, dataType = "string"),
            @ApiImplicitParam(paramType = "query", name = "endDate", defaultValue = "2021-03-23 17:50:00", required = true, dataType = "string"),
            @ApiImplicitParam(paramType = "query", name = "interval", defaultValue = "2", required = true, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "recovery", defaultValue = "0", required = true, dataType = "int")
    })
    public List<String> getS(String startDate, String endDate,int interval,MultipartFile multipartFile,Integer recovery) throws Exception {

        List<String> sda1s = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //开始时间必须小于结束时间
        Date begin = dateFormat1.parse(startDate);
        Date end = dateFormat1.parse(endDate);
        Date date = begin;
        InputStream inputStream = null;

        inputStream = multipartFile.getInputStream();
        List<List<Object>> list = null; list = ExcelUtils.getCourseListByExcel(inputStream, multipartFile.getOriginalFilename());
        String tagids=",";

        for (int i = 0; i < list.size(); i++) {
            List<Object> courseList = list.get(i);
            Cell tagid = (Cell) courseList.get(0);
            tagids = tagids + (Double.valueOf(tagid.toString()).longValue()) + ",";
        }
        String[] totalTaglist = tagids.split(",");




        while (date.getTime()<(end.getTime())) {
            System.out.println(date);
            String startReal=dateFormat1.format(date);
            c.setTime(date);
            c.add(Calendar.SECOND, interval); // 加两秒
            date = c.getTime();
            String endReal=dateFormat1.format(date);
           List<TagcheckbsidEntity> res=null;
            if(recovery==0)
                res= tagcheckbsidDao.getLackTagid(startReal, endReal);
            else
                  res= locationMapper.getLackTagidFromRecovery(startReal, endReal);
            HashSet<String> set = new LinkedHashSet<>();
            for (TagcheckbsidEntity tagcheckbsidEntity: res){
              set.add(tagcheckbsidEntity.getTagcheckid()+"");
            }
            ArrayList<String> arrayList = new ArrayList<String>(totalTaglist.length - 1);
            Collections.addAll(arrayList, Arrays.copyOfRange(totalTaglist, 1, totalTaglist.length));
            HashSet<String> allset = new LinkedHashSet<>(arrayList);
            allset.removeAll(set);
            String lack = ",";
            for (String ee : allset
            ) {
                lack = lack + ee + ",";
            }
            String finalRes=startReal+"---"+endReal+":total:"+res.size()+":lack:"+lack;
            sda1s.add(finalRes);

        }
        return sda1s;
    }


    String startErrorCodeCheck(String bslist, int type, int count, int interval) {

        TagcheckEntity tagcheckEntity = new TagcheckEntity();
        tagcheckEntity.setBslist("," + bslist + ",");
        tagcheckDao.insert(tagcheckEntity);
        Long id = tagcheckEntity.getId();
        String[] bslists = bslist.split(",");
        ;

        BserrorcodetestrecordEntity bserrorcodetestrecordEntity = new BserrorcodetestrecordEntity();
        bserrorcodetestrecordEntity.setBsid(Integer.valueOf(bslists[0]));
        bserrorcodetestrecordEntity.setTagcheckid(id);
        bserrorcodetestrecordEntity.setTestcount(count);
        bserrorcodetestrecordEntity.setTestinterval(interval);
        bserrorcodetestrecordEntity.setState((short) 1);
        bserrorcodetestrecordEntity.setStart(new Date());
        bserrorcodetestrecordDao.insert(bserrorcodetestrecordEntity);


        BsPara bs = new BsPara();
        bs.setBsid(Integer.valueOf(bslists[0]));
        bs.setMessageid(id.intValue());
        bs.setKeyOrder("errorcodetest");
        bs.setType((short) 1);
        bs.setCount(count);
        bs.setSendInterval(interval);
        kafukaSender.send(KafukaTopics.BS_ERRORCODETEST, bs.toString());
        return id + "";

    }

    String startChecktag(String bslist, int type, int finishType, int checkTime, MultipartFile file, CommonResult res) {
        if (file.isEmpty()) {
            return "请导入文件";
        }
        List<List<Object>> list = null;
        String tagids = ",";
        try {
            InputStream inputStream = null;

            inputStream = file.getInputStream();

            list = ExcelUtils.getCourseListByExcel(inputStream, file.getOriginalFilename());
            //tagState.clear();
            String[] bslists = ("," + bslist).split(",");
            Date now = new Date();
            for (int i = 0; i < list.size(); i++) {
                List<Object> courseList = list.get(i);
                Cell tagid = (Cell) courseList.get(0);
                tagids = tagids + (Double.valueOf(tagid.toString()).longValue()) + ",";
                for (int j = 0; j < 2; j++) {
                    if (sendMode) {
                        for (int k = 1; k < bslists.length; k++)
                            try {
                                tagState.putIfAbsent(Double.valueOf(tagid.toString()).longValue() + "", null);
                                TagValidate sendflag = tagState.get(Double.valueOf(tagid.toString()).longValue() + "");
                                if (sendflag == null || (now.getTime() - sendflag.startTime.getTime()) / 500000 >= period_mode) {
                                    long tagidd = Double.valueOf(tagid.toString()).longValue();
                                    TagPara tagPara = new TagPara();
                                    tagPara.setMode((short) 1);
                                    tagPara.setPeriod((short) period_mode);
                                    tagPara.setBsid(Long.valueOf(bslists[k]));
                                    tagPara.setTagid(tagidd + "");
                                    tagPara.setKeyOrder("mode");
                                    kafukaSender.send(KafukaTopics.TAG_CONTROLREQ, tagPara.toString());
                                }
                            } catch (Exception e) {

                            }
                    }
                }
            }
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TagcheckEntity tagcheckEntity = new TagcheckEntity();
        tagcheckEntity.setBslist("," + bslist + ",");
        tagcheckEntity.setTaglist(tagids);
        String[] bslists = bslist.split(",");
        int taglen = list.size();
        Date current = (new Date());
        tagcheckDao.insert(tagcheckEntity);
        Long id = tagcheckEntity.getId();
        for (int i = 0; i < bslists.length; i++) {
            TagcheckbsidEntity bsentity = new TagcheckbsidEntity();
            bsentity.setBsid(Integer.valueOf(bslists[i]));
            bsentity.setState(1);
            bsentity.setType(type);
            //  bsentity.setStart(current);
            bsentity.setTagcheckid(Long.valueOf(id));
            bsentity.setTotal(taglen);
            bsentity.setChecktime(checkTime);
            bsentity.setFinishtype(finishType);
            tagcheckbsidDao.insert(bsentity);
            if (sendMode) {
                for (int j = 0; j < 2; j++) {
                    try {
                        BsPara bsPara = new BsPara();
                        bsPara.setMode((byte) 1);
                        if (type != 3)
                            bsPara.setSuperFrame_interval((short) 6030);
                        else
                            bsPara.setSuperFrame_interval((short) 2000);
                        bsPara.setSlot_duration((short) 20);
                        bsPara.setKeyOrder("bsslotinf");
                        bsPara.setBsid(Integer.valueOf(bslists[i]));
                        kafukaSender.send(KafukaTopics.BS_CONTROLREQ, bsPara.toString());

                        bsState.putIfAbsent(Integer.valueOf(bslists[i]).toString(), false);

                    } catch (Exception e) {

                    }
                }
            }
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Boolean tagready = true;
        String tagNotReady = "";
        for (Map.Entry<String, TagValidate> tagstateentity : tagState.entrySet()
        ) {
            String tagid = tagstateentity.getKey();
            TagValidate value = tagstateentity.getValue();
            if (value == null) {
                tagNotReady = tagNotReady + "," + tagid;
                tagready = false;
            }
        }
        Boolean bsready = true;
        String bsNotReady = "";
        for (Map.Entry<String, Boolean> bsstateentity : bsState.entrySet()
        ) {
            String bsid = bsstateentity.getKey();
            Boolean value = bsstateentity.getValue();
            if (!value) {
                bsNotReady = bsNotReady + "," + bsid;
                bsready = false;
            }
        }

        if (bsready && tagready) {
            res.setCode(200);
            res.setData(id + "");
            return id + "";
        } else {
            // tagcheckbsidDao.deleteByTagCheckid(id);
            String lack = "未就绪" + "标签" + tagNotReady + "基站" + bsNotReady;
            res.setCode(200);
            res.setData(id + "");
            res.setMessage("未就绪" + "标签" + tagNotReady + "基站" + bsNotReady);
            return "未就绪";
        }

    }


    void finishNotEndCheck() {
        List<TagcheckbsidEntity> list = tagcheckbsidDao.getALLNotEndTagCheckBsid();
        Date date = new Date();

        for (TagcheckbsidEntity tag : list
        ) {
            tag.setEnd(date);
            tag.setState(0);
            logger.info("end"+tag.getId()+":"+tag.getLackpercent());
            tagcheckbsidDao.updateById(tag);
        }
    }

    void userEndTagCheck(Long tagCheckid) {
     /*   try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        List<TagcheckbsidEntity> totaltag = tagcheckbsidDao.getByTagcheckid(tagCheckid, null);
        if (totaltag != null && totaltag.size() > 0 && totaltag.get(0).getState() == 0) {
            return;
        }
        HashSet<String> set = new LinkedHashSet<>();
        for (TagcheckbsidEntity eachtag : totaltag
        ) {
            String ss = eachtag.getCurrentdetail();
            if (ss != null && ss.length() > 0) {
                String[] c = ss.split(",");
                int c_len = c.length;
                for (int i = 1; i < c_len; i++
                ) {
                    set.add(c[i]);
                }

            }
        }

        int totaldistinct = set.size();
        TagcheckEntity tagcheck = tagcheckDao.selectById(tagCheckid);
        String[] totalTaglist = tagcheck.getTaglist().split(",");
        ArrayList<String> arrayList = new ArrayList<String>(totalTaglist.length - 1);
        Collections.addAll(arrayList, Arrays.copyOfRange(totalTaglist, 1, totalTaglist.length));
        HashSet<String> allset = new LinkedHashSet<>(arrayList);
        allset.removeAll(set);
        String lack = ",";
        for (String ee : allset
        ) {
            lack = lack + ee + ",";
        }
        Timestamp time = new Timestamp(new Date().getTime());

        for (TagcheckbsidEntity eachtag : totaltag
        ) {
            eachtag.setEnd(new Date());
            if (eachtag.getStart() != null && eachtag.getState() == 1)
                eachtag.setPeriod((int) (new Date().getTime() - eachtag.getStart().getTime()));
            eachtag.setTotaldistinct(totaldistinct);
            logger.info("11ss"+eachtag.getId()+":"+totaldistinct+":"+eachtag.getTotal());
            eachtag.setLackpercent(1 - (totaldistinct) / (double) eachtag.getTotal());
            eachtag.setLackedetail(lack);
            eachtag.setState(0);
            logger.info ("11ss"+eachtag.getId()+"----"+eachtag.getLackpercent());
            tagcheckbsidDao.updateById(eachtag);

        }
    }

  String getSnotFind(String all,List<TagcheckbsidEntity> contain){
     return null;

  }


    public static void main(String[] args) throws ParseException {
      /*   ConcurrentHashMap<String,Boolean> sdsd=new ConcurrentHashMap<>();
         sdsd.putIfAbsent("1",true);
        sdsd.putIfAbsent("2",false);
        sdsd.putIfAbsent("3",true);
        Object[] arr = sdsd.values().toArray();
        Arrays.sort(arr);
        System.out.println("ss="+arr[0]);*/
        Calendar c = Calendar.getInstance();
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //开始时间必须小于结束时间
        Date beginDate = dateFormat1.parse("2021-03-23 17:46:10");
        Date endDate = dateFormat1.parse("2021-03-23 17:50:52");
        Date date = beginDate;
        while (!date.equals(endDate)) {
            System.out.println(date);
            c.setTime(date);
            c.add(Calendar.SECOND, 2); // 加两秒
            date = c.getTime();
        }
    }
}
