package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.common.ByteUtils;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.ImportUsersException;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.equip.TagBeacon;
import com.tgy.rtls.data.entity.equip.TagFirmware;
import com.tgy.rtls.data.entity.equip.TagVolt;
import com.tgy.rtls.data.entity.nb_device.Nb_device;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.kafukaentity.TagPara;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.user.PersonService;
import com.tgy.rtls.data.snowflake.AutoKey;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2020/10/16
 */
@Service
public class TagServiceImpl implements TagService{
    private Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);
    @Autowired(required = false)
    private TagMapper tagMapper;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private AutoKey autoKey;
    @Autowired
    private PersonService personService;
    @Autowired
    LocalUtil localUtil;

    @Override
    public List<Tag> findByAll(String num,Integer binding,String desc,Integer instanceid) {
        return tagMapper.findByAll(num,binding,desc,instanceid,localUtil.getLocale());
    }

    @Override
    public List<Tag> findByTagOnLine(Integer map) {
        return tagMapper.findByTagOnLine(map);
    }

    @Override
    public String findByNameId(String ids) {
        String[] split=ids.split(",");
        return tagMapper.findByNameId(split);
    }

    @Override
    public Tag findById(Integer id) {
        Tag tag=tagMapper.findById(id,localUtil.getLocale());
        Person person=personService.findByTagNum(tag.getNum());
        if (person!=null){
            tag.setPerson(person);
        }
        return tag;
    }

    @Override
    @Cacheable(value = "tagNum",key = "#num")
    public Tag findByNum(String num) {
        return tagMapper.findByNum(num);
    }

    @Override
    public Boolean addTag(Tag tag){
        if (tagMapper.addTag(tag)>0){
            //修改定位卡的频率
            if (!NullUtils.isEmpty(tag.getFrequency())){
                TagPara tagPara=new TagPara();
                tagPara.setKeyOrder("locpara");
                tagPara.setBsid(-1);
                tagPara.setLoc_inval(1000/tag.getFrequency());
                tagPara.setRx_inval(1);
                tagPara.setTagid(tag.getNum());
                kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,tagPara.toString());
                logger.info(tag.getNum()+"标签发送修改频率请求："+tagPara);
            }
            if (!NullUtils.isEmpty(tag.getPower())){
                power(String.valueOf(tag.getPower()),1,tag.getNum(),"-1");
            }
            return true;
        }
        return false;
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "tagNum",key = "#tag.num"),
                    @CacheEvict(value = "personnum",key = "#tag.num")
            }
    )
    public Boolean updateTag(Tag tag) {
        if (tagMapper.updateTag(tag)>0) {
            if (!NullUtils.isEmpty(tag.getFrequency())) {
                TagPara tagPara = new TagPara();
                tagPara.setKeyOrder("locpara");
                tagPara.setBsid(-1);
                tagPara.setLoc_inval(1000/tag.getFrequency());
                tagPara.setRx_inval(1);
                tagPara.setTagid(tag.getNum());
                kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ, tagPara.toString());
                logger.info(tag.getNum() + "标签发送修改频率请求：" + tagPara);
            }
            if (!NullUtils.isEmpty(tag.getPower())){
                power(String.valueOf(tag.getPower()),1,tag.getNum(),"-1");
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean delTag(String ids) {
        String[] split=ids.split(",");
        return tagMapper.delTag(split)>0;
    }

    @Override
    @CacheEvict(value = "tagNum",key = "#num")
    public void updateTagStatus(String num, Integer status) {
        tagMapper.updateTagStatus(num, status);
    }

    @Override
    @CacheEvict(value = "tagnum",key = "#num")
    public void updateTagBatteryTime(String num, Double batteryVolt, String batteryTime) {
        tagMapper.updateTagBatteryTime(num,batteryVolt,batteryTime);
    }

    @Override
    public int importLabelFromExcel(MultipartFile excelFile,Integer instanceid)throws Exception{
        int count=0;
        HSSFWorkbook book=null;
        try {
            //使用poi解析Excel文件
            book=new HSSFWorkbook(excelFile.getInputStream());
            //根据名称获得指定Sheet对象
            HSSFSheet hssfSheet=book.getSheetAt(0);
            if (hssfSheet!=null){
                List<HSSFPictureData> pictures=book.getAllPictures();
                Map<Integer,HSSFPictureData> picDataMap=new HashMap<>();
                if (hssfSheet.getDrawingPatriarch()!=null){
                    for (HSSFShape shape:hssfSheet.getDrawingPatriarch().getChildren()){
                        HSSFClientAnchor anchor=(HSSFClientAnchor) shape.getAnchor();
                        if (shape instanceof HSSFPicture){
                            HSSFPicture pic=(HSSFPicture)shape;
                            int row=anchor.getRow1();
                            int pictureIndex=pic.getPictureIndex()-1;
                            try {
                                HSSFPictureData picData=pictures.get(pictureIndex);
                                picDataMap.put(row,picData);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
                int rows=hssfSheet.getPhysicalNumberOfRows();
                if (rows<2){
                    return 0;
                }
                String[] titles={"卡号","定位频率","功率"};
                HSSFRow headerRow=hssfSheet.getRow(0);
                int cells=headerRow.getPhysicalNumberOfCells();
                List<String> cellList=new LinkedList<>();
                for (int i=0;i<cells;i++){
                    String cellName=headerRow.getCell(i).getStringCellValue();
                    cellList.add(cellName);
                }
                if (cellList.size()<titles.length){
                    throw new ImportUsersException("导入数据模板格式错误");
                }
                for (int i=0;i<titles.length;i++){
                    if (!titles[i].equals(cellList.get(i))){
                        throw new ImportUsersException("导入数据模板格式错误");
                    }
                }
                Map<Integer,String> numNumberExist=new LinkedHashMap<>();//ID重名错误
                Map<Integer,String> nullExist=new LinkedHashMap<>();//空指针判断
                for (int i=1;i<rows;i++){
                    HSSFRow row=hssfSheet.getRow(i);
                    if (row!=null){
                        for (int j=0;j<cells;j++){
                            if (row.getCell(j)!=null)
                                row.getCell(j).setCellType(CellType.STRING);
                        }
                        String[] val=new String[3];
                        for (int j=0;j<cells;j++){
                            HSSFCell cell=row.getCell(j);
                            if (cell!=null){
                                switch (cell.getCellTypeEnum()){
                                    case FORMULA:
                                        break;
                                    case NUMERIC:
                                        val[j]= String.valueOf(cell.getNumericCellValue());
                                        break;
                                    case STRING:
                                        val[j]=cell.getStringCellValue();
                                        break;
                                    default:
                                        val[j]="";
                                }
                            }
                        }
                        try {
                            Tag tag=new Tag();
                            //判断卡号重名
                            if (!NullUtils.isEmpty(val[0])){
                                Tag tag1=tagMapper.findByNum(val[0]);
                                try {
                                    if (!NullUtils.isEmpty(tag1) || !ByteUtils.isInteger(val[0])|| ByteUtils.isInteger(val[0])&&Integer.valueOf(val[0]) <= 0) {
                                        numNumberExist.put(i, val[0]);//重名或者名称异常
                                        continue;
                                    } else {
                                        tag.setNum(val[0]);
                                    }
                                }catch (Exception e){
                                    numNumberExist.put(i, val[0]);//重名或者名称异常
                                    continue;
                                }
                            }else {
                                nullExist.put(i,null);
                                continue;
                            }
                            tag.setFrequency(10);
                            /*if (!NullUtils.isEmpty(val[1])&&Integer.valueOf(val[1])>0) {
                               // tag.setFrequency(Integer.parseInt(val[1]));
                            }else {//为空给其填写默认值 10hz
                                tag.setFrequency(10);
                            }*//*
                            if (!NullUtils.isEmpty(val[2])) {
                                tag.setPower(Integer.parseInt(val[2]));
                            }else {//为空给其填写默认值 33w
                                tag.setPower(33);
                            }*/
                            tag.setPower(33);
                            tag.setType(1);
                            tag.setInstanceid(instanceid);
                            if (tagMapper.addTag(tag)>0){
                                count++;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }
                int failNum=numNumberExist.size()+nullExist.size();
                if (failNum>0) {
                    String errorStr = count + "条数据导入成功," + failNum + "条数据导入失败。<br/>";

                    if (nullExist.size() > 0) {
                        errorStr += nullExist.size() + "条信息填写不完整:<br/>";
                        Integer[] keys = new Integer[nullExist.size()];
                        nullExist.keySet().toArray(keys);
                        for (int key : keys) {
                            String id = nullExist.get(key);
                            errorStr += "第" + key + "行：" + id + "<br/>";
                        }
                    }
                    if (numNumberExist.size()>0){
                        errorStr+=numNumberExist.size()+"条信息的卡号已存在或者编号异常:<br/>";
                        Integer[] keys=new Integer[numNumberExist.size()];
                        numNumberExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=numNumberExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }
                    }
                    throw new ImportUsersException(errorStr);
                }
            }
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public void upgradeTag(String num,String path) {
        long id=autoKey.getAutoId("");
        String[] split=num.split(",");
        for (String s:split){
            JSONObject object=new JSONObject();
            object.put("keyOrder","update");
            object.put("tagid",s);
            object.put("bsid",-1);
            object.put("firmwareUrl",path);
            object.put("firmwareVersion",id);
            object.put("pkglen",512);
            kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
            logger.info(s+"标签发送升级请求："+object.toString());
        }
    }

    @Override
    public void debugTag(TagFirmware tagFirmware) {
        //根据不同keyOrder走不同分支
        //1蜂鸣器控制
        if (tagFirmware.getKeyOrder().equals("beep")){
            beep(tagFirmware.getBeepInterval(),tagFirmware.getBeepState(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("reboot")){//2.标签重启
            reboot(tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("text")){//3.文字输入
            addText(tagFirmware.getText(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("voice")){//4.音频
            addVoice(tagFirmware.getUrl(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("id")){//5.修改标签ID
            id(tagFirmware.getNewId(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("power")){//6.功率控制
            power(tagFirmware.getPowerLevel(),tagFirmware.getPa(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("lowpower")){//7.低功耗模式
            lowpower(tagFirmware.getLowPowerMode(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("sensorperiod")){//8.传感器上传间隔修改
            sensorperiod(tagFirmware.getSensorInterval(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("movelevel")){//9.运动阈值
            movelevel(tagFirmware.getMoveLevel(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("heartperiod")){//10.心率检测周期
            heartperiod(tagFirmware.getHeartInterval(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if(tagFirmware.getKeyOrder().equals("locpara")){//11.定位间隔 接收窗口时间
            locpara(tagFirmware.getLocaInval(),tagFirmware.getRxInval(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if (tagFirmware.getKeyOrder().equals("groupbslist")){//12.读写目标基站地址
            groupbslist(tagFirmware.getType(),tagFirmware.getGroupbslist(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }else if (tagFirmware.getKeyOrder().equals("grouprangetime")){//13.写入组测距周期
            grouprangetime(tagFirmware.getType(),tagFirmware.getGrouprangetime(),tagFirmware.getTagid(),tagFirmware.getBsid());
        }
    }

    @Override
    public void delTagInstance(Integer instanceid) {
        tagMapper.delTagInstance(instanceid);
    }

    @Override
    public void addTagVolt(TagVolt tagVolt) {
          tagMapper.addSub1GVolt(tagVolt);
    }

    @Override
    public void addBeaconRssi(TagBeacon tagBeacon) {
        tagMapper.addBleRssi(tagBeacon);
    }

    @Override
    @Cacheable(value = "tagpara",key = "#tagid")
    public com.tgy.rtls.data.entity.equip.TagPara findTagid(String tagid) {
        return tagMapper.findByTagid(tagid);
    }
    @Override
    @Cacheable(value = "tagmac",key = "#mac")
    public com.tgy.rtls.data.entity.equip.TagPara findTagMac(String mac) {
        return tagMapper.findByTagMac(mac);
    }

    @Override
    public List findByNbMac(String mac,String license) {
        return tagMapper.findByNbMac(mac,license,null);
    }

    @Override
    public void addNb(Nb_device nb_device) {
        tagMapper.addNb(nb_device);

    }

    @Override
    public void updateNb(Nb_device nb_device) {
        tagMapper.updateNb(nb_device);
    }

    //蜂鸣器控制 beepState 状态 0关闭 1打开
    private void beep(Integer beepInterval,Integer beepState,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","beep");
        object.put("beepInterval",beepInterval);//蜂鸣器鸣叫间隔
        object.put("beepState",beepState);//蜂鸣器状态 0关闭 1打开
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送蜂鸣器控制命令:"+object);
    }
    //标签重启
    private void reboot(String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","reboot");
        object.put("reboot",1);//1重启
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送重启命令:"+object);
    }
    //文字输入
    private void addText(String text,String tagid,String bsid){
        JSONObject obj = new JSONObject();
        obj.put("target",tagid);//标签编号
        obj.put("messageid","-1");//消息唯一id
        obj.put("type",1);//普通文本
        obj.put("level",0);//一般紧急消息
        obj.put("text",text);
        obj.put("bsid",bsid);
        obj.put("time",new Date().getTime());
        kafkaTemplate.send(KafukaTopics.TEXT_REQ, obj.toString());
        logger.info(tagid+"标签发送文字输入命令:"+obj);
    }
    //语音输入
    private void addVoice(String url,String tagid,String bsid){
        JSONObject obj = new JSONObject();
        obj.put("fileType",1);
        obj.put("direction", "1");
        obj.put("target",tagid);
        obj.put("bsid",bsid);
        obj.put("url", url);
        obj.put("messageid","-1");
        obj.put("time",new Date().getTime());
        kafkaTemplate.send(KafukaTopics.File_REQ, obj.toString());
        logger.info(tagid+"标签发送语音传输命令:"+obj);
    }
    //修改标签ID
    private void id(String newId,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","id");
        object.put("newId",newId);//新标签编号
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送修改标签ID命令:"+object);
    }
    //功率控制
    private void power(String powerLevel,Integer pa,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","power");
        object.put("pa",pa);//0 关闭 1打开
        object.put("powerLevel",powerLevel);//功率
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送功率控制命令:"+object);
    }
    //低功耗模式
    private void lowpower(Integer lowPowerMode,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","lowpower");
        object.put("lowPowerMode",lowPowerMode);//0退出低功耗模式 1开启低功耗模式
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送低功耗模式命令:"+object);
    }
    //传感器上传间隔修改
    private void sensorperiod(Integer sensorInterval,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","sensorperiod");
        object.put("sensorInterval",sensorInterval);//传感器上传周期
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送传感器上传间隔修改命令:"+object);
    }
    //运动阈值
    private void movelevel(Integer moveLevel,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","movelevel");
        object.put("moveLevel",moveLevel);//运动阈值
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送运动阈值命令:"+object);
    }
    //心率检测周期
    private void heartperiod(Integer heartInterval,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","heartperiod");
        object.put("heartInterval",heartInterval);//心率监测间隔
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送心率监测周期命令:"+object);
    }
    //定位间隔 接收窗口时间
    private void locpara(Integer loca_inval,Integer rx_inval,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","locpara");
        object.put("loca_inval",loca_inval);//定位间隔
        object.put("rx_inval",rx_inval);//接收窗口时间
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送定位间隔命令:"+object);
    }
    //目标基站地址
    private void groupbslist(Integer type,String groupbslist,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","groupbslist");
        object.put("type",type);//读写
        object.put("groupbslist",groupbslist);//目标基站地址
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送目标基站地址命令:"+object);
    }
    //组测距周期
    private void grouprangetime(Integer type,Integer grouprangetime,String tagid,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","grouprangetime");
        object.put("type",type);//读写
        object.put("grouprangetime",grouprangetime);//目标基站地址
        object.put("tagid",tagid);
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.TAG_CONTROLREQ,object.toString());
        logger.info(tagid+"标签发送组测距周期命令:"+object);
    }
}
