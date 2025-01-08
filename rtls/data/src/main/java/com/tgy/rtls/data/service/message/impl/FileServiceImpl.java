package com.tgy.rtls.data.service.message.impl;

import com.tgy.rtls.data.algorithm.PercentToPosition;
import com.tgy.rtls.data.algorithm.Range;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.RecoveryUtils;
import com.tgy.rtls.data.entity.location.Recovery_data;
import com.tgy.rtls.data.entity.location.Trailrecord;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.message.FileRecord;
import com.tgy.rtls.data.entity.message.FileSyn;
import com.tgy.rtls.data.entity.message.TextRecord;
import com.tgy.rtls.data.entity.message.VoiceRecord;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.mapper.location.LocationMapper;
import com.tgy.rtls.data.mapper.map.BsConfigMapper;
import com.tgy.rtls.data.mapper.message.FileMapper;
import com.tgy.rtls.data.mapper.user.PersonMapper;
import com.tgy.rtls.data.service.message.FileService;
import com.tgy.rtls.data.snowflake.AutoKey;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.message.impl
 * @date 2020/10/26
 */
@Service
@Transactional
public class FileServiceImpl implements FileService {

    @Autowired(required = false)
    private FileMapper fileMapper;
    @Autowired(required = false)
    private AutoKey autoKey;
    @Autowired(required = false)
    private KafkaTemplate kafkaTemplate;
    @Autowired(required = false)
    private PersonMapper personMapper;
    @Autowired(required = false)
    private LocationMapper locationMapper;
    @Autowired(required = false)
    private BsConfigMapper bsConfigMapper;
    @Autowired
    private LocalUtil localUtil;

     @Autowired
    Executor scheduledExecutorService;
    //ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    @Override
    public List<FileSyn> findByAll(String name, String startTime, String endTime, Integer instanceid, Integer pageIndex, Integer pageSize) {
        return fileMapper.findByAll(name, startTime, endTime, instanceid, pageIndex, pageSize);
    }

    @Override
    public List<FileRecord> findByPersonid(Integer personid, String startTime, String endTime) {
        return fileMapper.findByPersonid(personid, startTime, endTime);
    }

    @Override
    public Boolean addVoice(String personids, VoiceRecord voice, String http, String kafukaFile) {
        String[] split = personids.split(",");
        for (String s : split) {
            Person person = personMapper.findById(Integer.valueOf(s),LocalUtil.get(KafukaTopics.OFFLINE));
            voice.setPersonid(Integer.valueOf(s));
            voice.setRandom(String.valueOf(autoKey.getAutoId(null)));
            //发送语音
            if (!NullUtils.isEmpty(person) && !NullUtils.isEmpty(person.getTagid())) {
                JSONObject obj = new JSONObject();
                obj.put("fileType", 1);
                obj.put("direction", "1");
                obj.put("target", person.getTagName());
                obj.put("bsid", -1);
                obj.put("url", http + kafukaFile);
                obj.put("messageid", voice.getRandom());
                obj.put("time", new Date().getTime());
                obj.put("instanceid", voice.getInstanceid());
                kafkaTemplate.send(KafukaTopics.File_REQ, obj.toString());
            }
            fileMapper.addVoice(voice);
        }
        return true;
    }

    @Override
    public Boolean addVoice(VoiceRecord voice) {
        return fileMapper.addVoice(voice) > 0;
    }

    @Override
    public Boolean addText(String personids, TextRecord text) {
        String[] split = personids.split(",");
        for (String s : split) {
            Person person = personMapper.findById(Integer.valueOf(s),LocalUtil.get(KafukaTopics.OFFLINE));
            text.setPersonid(Integer.valueOf(s));
            text.setRandom(String.valueOf(autoKey.getAutoId(null)));
            //发送文本
            if (!NullUtils.isEmpty(person) && !NullUtils.isEmpty(person.getTagid())) {
                JSONObject obj = new JSONObject();
                obj.put("target", person.getTagName());//标签编号
                obj.put("messageid", text.getRandom());//消息唯一id
                obj.put("type", 1);//普通文本
                obj.put("level", 0);//一般紧急消息
                obj.put("text", text.getFile());
                obj.put("bsid", -1);
                obj.put("time", new Date().getTime());
                obj.put("instanceid", text.getInstanceid());
                kafkaTemplate.send(KafukaTopics.TEXT_REQ, obj.toString());
            }
            fileMapper.addText(text);
        }
        return true;
    }

    @Override
    public Boolean retreatText(Integer instanceid, String title) {
        //获取当前在线人员信息
        List<Person> personList = personMapper.retreatPerson(instanceid);
        TextRecord text = new TextRecord();
        for (Person person : personList) {
            text.setPersonid(person.getId());
            text.setRandom(String.valueOf(autoKey.getAutoId(null)));
            text.setTitle(title);
            text.setFile("撤退！");
            //发送撤退消息
            JSONObject obj = new JSONObject();
            obj.put("target", person.getTagName());//标签编号
            obj.put("messageid", text.getRandom());//消息唯一id
            obj.put("type", 3);//紧急消息
            obj.put("level", 1);//特别紧急消息
            obj.put("text", text.getFile());
            obj.put("bsid", -1);
            obj.put("time", new Date().getTime());
            obj.put("instanceid", instanceid);
            kafkaTemplate.send(KafukaTopics.TEXT_REQ,  obj.toString());
            //存储消息
            fileMapper.addText(text);
        }
        return true;
    }

    /*
     * 数据恢复
     * */
    public void readFileCor(String path) {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                File file = new File(path);
                try {
                    FileInputStream in = new FileInputStream(file);
                    for (int i = 0; i < file.length() / 103; i++) {
                        try {
                            byte[] header = new byte[4];//0x18181818
                            byte[] tag_id = new byte[4];//标签ID
                            byte[] range_id = new byte[4];//测距id
                            byte[] rx3 = new byte[8];//本基站收到tag结果帧的时间戳
                            byte[] bss_id_m = new byte[4];//标签是与哪个基站进行的测距
                            byte[] bss_seq_m = new byte[1];//主基站：0， 从基站 1，
                            byte[] ft1_m = new byte[8];//标签发出的时间戳
                            byte[] ft2_m = new byte[8];//被测基站收到的时间戳
                            byte[] ft3_m = new byte[8];//被测基站发送的时间戳
                            byte[] ft4_m = new byte[8];//标签接收的时间戳
                            byte[] bss_id_s = new byte[4];//标签是与哪个基站进行的测距
                            byte[] bss_seq_s = new byte[1];//主基站：0， 从基站 1，
                            byte[] ft1_s = new byte[8];//标签发出的时间戳
                            byte[] ft2_s = new byte[8];//被测基站收到的时间戳
                            byte[] ft3_s = new byte[8];//被测基站发送的时间戳
                            byte[] ft4_s = new byte[8];//标签接收的时间戳
                            byte[] tag_rssi3=new byte[4];//rssi
                            byte[] tag_fp3=new byte[4];//首达径信号强度
                            byte[] cl=new byte[4];//直达径概率
                            byte[] year = new byte[2];
                            byte[] month = new byte[1];
                            byte[] day = new byte[1];
                            byte[] hour = new byte[1];
                            byte[] min = new byte[1];
                            byte[] sec = new byte[1];
                            byte[] ms = new byte[2];
                            in.read(header);
                            in.read(tag_id);
                            in.read(range_id);
                            in.read(rx3);
                            //第一组数据
                            in.read(bss_id_m);
                            in.read(bss_seq_m);
                            in.read(ft1_m);
                            in.read(ft2_m);
                            in.read(ft3_m);
                            in.read(ft4_m);
                            //第二组数据
                            in.read(bss_id_s);
                            in.read(bss_seq_s);
                            in.read(ft1_s);
                            in.read(ft2_s);
                            in.read(ft3_s);
                            in.read(ft4_s);
                            in.read(tag_rssi3);
                            in.read(tag_fp3);
                            in.read(cl);
                            in.read(year);
                            in.read(month);
                            in.read(day);
                            in.read(hour);
                            in.read(min);
                            in.read(sec);
                            in.read(ms);
                            //存原始数据-->第一组数据
                            String timestamp = dateFormat.format(RecoveryUtils.StringToDate(RecoveryUtils.byte2short(year), month[0], day[0], hour[0], min[0], sec[0], RecoveryUtils.byte2short(ms)));
                            Long rtagid = RecoveryUtils.bytes2long(tag_id);
                            Long rrangeid = RecoveryUtils.bytes2long(range_id);
                            BigDecimal rrx3 = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(rx3));
                            Long rbssid_m = RecoveryUtils.bytes2long(bss_id_m);
                            Long rbsseq_m = (long) bss_seq_m[0];
                            BigDecimal rft1_m = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft1_m));
                            BigDecimal rft2_m = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft2_m));
                            BigDecimal rft3_m = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft3_m));
                            BigDecimal rft4_m = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft4_m));
                            Long rbssid_s = RecoveryUtils.bytes2long(bss_id_s);
                            Long rbsseq_s = (long) bss_seq_s[0];
                            BigDecimal rft1_s = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft1_s));
                            BigDecimal rft2_s = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft2_s));
                            BigDecimal rft3_s = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft3_s));
                            BigDecimal rft4_s = RecoveryUtils.readUnsignedLong(RecoveryUtils.bytes2long(ft4_s));
                            Recovery_data recovery1 = new Recovery_data();
                            recovery1.setTimestamp(timestamp);
                            recovery1.setTagid(rtagid);
                            recovery1.setRangid(rrangeid);
                            recovery1.setBsid(rbssid_m);
                            recovery1.setRx3(rrx3);
                            recovery1.setFt1(rft1_m);
                            recovery1.setFt2(rft2_m);
                            recovery1.setFt3(rft3_m);
                            recovery1.setFt4(rft4_m);
                            recovery1.setLr(rbsseq_m);
                            //第二组数据
                            Recovery_data recovery2 = new Recovery_data();
                            recovery2.setTimestamp(timestamp);
                            recovery2.setTagid(rtagid);
                            recovery2.setRangid(rrangeid);
                            recovery2.setBsid(rbssid_s);
                            recovery2.setRx3(rrx3);
                            recovery2.setFt1(rft1_s);
                            recovery2.setFt2(rft2_s);
                            recovery2.setFt3(rft3_s);
                            recovery2.setFt4(rft4_s);
                            recovery2.setLr(rbsseq_s);
                            //两组数据存储成功后进行下步操作
                            if (locationMapper.insertRecovery(recovery1) > 0 && locationMapper.insertRecovery(recovery2) > 0) {
                                //求出距离---->第一组
                                BigDecimal[] timeStamp1 = {recovery1.getFt1(), recovery1.getFt2(), recovery1.getFt3(), recovery1.getFt4()};
                                Double dis_c1 = Double.valueOf(String.format("%.4f", Range.getDis(timeStamp1, 0, 0)));

                                //第二组
                                BigDecimal[] timeStamp2 = {recovery2.getFt1(), recovery2.getFt2(), recovery2.getFt3(), recovery2.getFt4()};
                                Double dis_c2 = Double.valueOf(String.format("%.4f", Range.getDis(timeStamp2, 0, 0)));
                                Integer bsid2 = null;
                                String rangeinf = "0";//定位点距离基站的距离比例
                                Double[] source = new Double[3];//存储主基站的坐标
                                Double[] target = new Double[3];//存储从基站的坐标
                                //判断该标签是否是和同一个基站测距的
                                //两基站的距离
                                Double dis;
                                if (recovery1.getBsid().equals(recovery2.getBsid())) {
                                    //是
                                    Double dis_c = 0.0;
                                    //查找设备相邻的基站
                                    BsConfig bsConfig = bsConfigMapper.findByNum(String.valueOf(recovery1.getBsid()),localUtil.getLocale());
                                    if (NullUtils.isEmpty(bsConfig)) {
                                        continue;
                                    }
                                    //补偿参数
                                    String[] disfixAll1 = bsConfig.getDisfix().split(":");
                                    //第一组数据的真实距离
                                    String[] disfix1=disfixAll1[Math.toIntExact(recovery1.getLr())].split(",");
                                    dis_c1=dis_c1*Double.parseDouble(disfix1[0])+Double.parseDouble(disfix1[1]);
                                    //第二组数据的真实距离
                                    String[] disfix2=disfixAll1[Math.toIntExact(recovery2.getLr())].split(",");
                                    dis_c2=dis_c2*Double.parseDouble(disfix2[0])+Double.parseDouble(disfix2[1]);
                                    if (dis_c1 < dis_c2) {//距离那个近 就在那边
                                        if (recovery1.getLr() == 0) {//左边
                                            bsid2 = bsConfig.getLeftid();
                                            dis = bsConfig.getLeftdis();
                                        } else {//右边
                                            bsid2 = bsConfig.getRightid();
                                            dis = bsConfig.getRightdis();
                                        }
                                        dis_c = dis_c1;
                                    } else {
                                        if (recovery2.getLr() == 0) {//左边
                                            bsid2 = bsConfig.getLeftid();
                                            dis = bsConfig.getLeftdis();
                                        } else {//右边
                                            bsid2 = bsConfig.getRightid();
                                            dis = bsConfig.getRightdis();
                                        }
                                        dis_c = dis_c2;
                                    }
                                    BsConfig bsConfig2=bsConfigMapper.findByNum(String.valueOf(bsid2),localUtil.getLocale());
                                    if (bsid2 > recovery1.getBsid()) {
                                        rangeinf = String.format("%.4f", Math.abs(dis_c / dis));
                                        source[0]=bsConfig.getX();
                                        source[1]=bsConfig.getY();
                                        source[2]=bsConfig.getZ();
                                        target[0]=bsConfig2.getX();
                                        target[1]=bsConfig2.getY();
                                        target[2]=bsConfig2.getZ();
                                    } else {
                                        rangeinf = String.format("%.4f", Math.abs(1 - (dis / dis)));
                                        target[0]=bsConfig.getX();
                                        target[1]=bsConfig.getY();
                                        target[2]=bsConfig.getZ();
                                        source[0]=bsConfig2.getX();
                                        source[1]=bsConfig2.getY();
                                        source[2]=bsConfig2.getZ();
                                    }
                                } else {
                                    //否-->两个不同的基站测距信息
                                    //两基站的距离
                                    BsConfig bsConfig1=bsConfigMapper.findByNum(String.valueOf(recovery1.getBsid()),localUtil.getLocale());
                                    BsConfig bsConfig2=bsConfigMapper.findByNum(String.valueOf(recovery2.getBsid()),localUtil.getLocale());
                                    if (NullUtils.isEmpty(bsConfig1)||NullUtils.isEmpty(bsConfig2)){
                                        continue;
                                    }
                                    //补偿参数
                                    String[] disfixAll1 = bsConfig1.getDisfix().split(":");
                                    String[] disfixAll2 = bsConfig2.getDisfix().split(":");
                                    //第一组数据的真实距离
                                    String[] disfix1=disfixAll1[Math.toIntExact(recovery1.getLr())].split(",");
                                    dis_c1=dis_c1*Double.parseDouble(disfix1[0])+Double.parseDouble(disfix1[1]);
                                    //第二组数据的真实距离
                                    String[] disfix2=disfixAll2[Math.toIntExact(recovery2.getLr())].split(",");
                                    dis_c2=dis_c2*Double.parseDouble(disfix2[0])+Double.parseDouble(disfix2[1]);
                                    //两基站之间的距离
                                    dis=Math.sqrt((bsConfig1.getX()-bsConfig2.getX())*(bsConfig1.getX()-bsConfig2.getX())+(bsConfig1.getY()-bsConfig2.getY())*(bsConfig1.getY()-bsConfig2.getY()));
                                    //计算标签与基站之间的距离比例并存储基站的坐标
                                    if (recovery2.getBsid() > recovery1.getBsid()) {
                                        rangeinf = String.format("%.4f", Math.abs((dis_c1 / dis) + (1 - (dis_c2 / dis))) / 2);
                                        source[0]=bsConfig1.getX();
                                        source[1]=bsConfig1.getY();
                                        source[2]=bsConfig1.getZ();
                                        target[0]=bsConfig2.getX();
                                        target[1]=bsConfig2.getY();
                                        target[2]=bsConfig2.getZ();
                                    } else {
                                        rangeinf = String.format("%.4f", Math.abs((dis_c2 / dis) + (1 - (dis_c1 / dis))) / 2);
                                        target[0]=bsConfig1.getX();
                                        target[1]=bsConfig1.getY();
                                        target[2]=bsConfig1.getZ();
                                        source[0]=bsConfig2.getX();
                                        source[1]=bsConfig2.getY();
                                        source[2]=bsConfig2.getZ();
                                    }
                                }
                                //通过两基站的坐标和标签与基站比例计算定位点的坐标
                               double[] res= PercentToPosition.percentToPosition(source,target, Double.parseDouble(rangeinf));
                                //存储定位数据
                                //查询标签绑定的人员
                                Person person=personMapper.findByTagNum(String.valueOf(rtagid));
                                if (!NullUtils.isEmpty(person)) {
                                    Trailrecord trailrecord = new Trailrecord();
                                    trailrecord.setX(res[0]);
                                    trailrecord.setY(res[1]);
                                    trailrecord.setZ(res[2]);
                                    trailrecord.setPersonid(person.getId());
                                    trailrecord.setName(person.getName());
                                    trailrecord.setNum(person.getNum());
                                    trailrecord.setMapid(person.getMap());
                                    trailrecord.setTime(dateFormat.parse(timestamp));
                                    trailrecord.setType(0);
                                    locationMapper.addTrailrecord(trailrecord);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void updateVoice(String random, Integer status,String url,String urllocal) {
        fileMapper.updateVoice(random, status,url,urllocal);
    }

    @Override
    public void updateText(String random, Integer status) {
        fileMapper.updateText(random, status);
    }
}
