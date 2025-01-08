package com.tgy.rtls.location.controller;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.tgy.rtls.data.algorithm.CalculateAb;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.InfraredOrigin;
import com.tgy.rtls.data.entity.equip.InfraredOriginCount;
import com.tgy.rtls.data.entity.es.ESInfraredOriginal;
import com.tgy.rtls.data.entity.es.ESMag;
import com.tgy.rtls.data.entity.location.Originaldata;
import com.tgy.rtls.data.kafukaentity.*;
import com.tgy.rtls.data.mapper.location.LocationMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.user.PersonMapper;
import com.tgy.rtls.data.service.check.AutoService;
import com.tgy.rtls.data.service.common.EventlogService;
import com.tgy.rtls.data.service.common.MailService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.es.impl.ESInfraredOriginalImpl;
import com.tgy.rtls.data.service.es.impl.ESMagServiceImpl;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.user.PersonService;
import com.tgy.rtls.data.snowflake.AutoKey;
import com.tgy.rtls.location.config.FastdfsUtils;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.TagParaConfig;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.model.Ned;
import com.tgy.rtls.location.model.NedData;
import com.tgy.rtls.location.netty.*;
import com.tgy.rtls.location.test.MutilClient;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static com.tgy.rtls.location.Utils.Constant.CMD_NED_CONTROL;

@RestController
@RequestMapping("/kk")
public class Test {

    @Autowired
    private AutoKey autoKey;
    @Autowired
    DataProcess dataProcess;
    @Autowired
    BsParaConfig bsParaConfig;
    @Autowired
    SubService subService;
    @Autowired
    TagParaConfig tagParaConfig;
    @Autowired(required = false)
    LocationMapper locationMapper;
    @Autowired
    EventlogService eventlogService;
    @Autowired
    MapContainer mapContainer;
    @Autowired(required = false)
    GatewayService gatewayService;
    @Autowired
    SendData sendData;

/*    @Autowired
    DruidConfiguration druidConfiguration;*/
@Autowired
BsConfigService bsConfigService;
@Autowired(required = false)
    PersonMapper personMapper;
@Autowired
    PersonService personService;
@Autowired(required = false)
AutoService autoidService;
    @Autowired(required = false)
    JavaMailSender jsm;
    @Value("${testip}")
    private String ip;

    @Value("${spring.mail.username}")
    private String username;
    @Autowired(required = false)
    MailService mailService;
    @Autowired(required = false)
    RedisService redisService;
    @Autowired
    FastdfsUtils fastdfsUtils;
    @Autowired(required = false)
    ParkMapper parkMapper;
    public static volatile String res="no res";
    @Autowired
    private KafukaSender kafukaSender;
    private static final DateTimeFormatter time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    @Autowired
    private ESInfraredOriginalImpl esInfraredOriginalImpl;
    @Autowired
    private ESMagServiceImpl magService;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @RequestMapping(value = "/getMag")
    public CommonResult<Object> getMag(String num, Integer state,String startTime, String endTime) throws ParseException {
        List<ESMag> mags = magService.searchMag(num, state, startTime, endTime);
        return new CommonResult<>(200, "success", mags);
    }
    @ResponseBody
    @RequestMapping(value ="/loraUpdate", method= RequestMethod.POST)
    @ApiOperation(value = "433M网关升级",notes = "无")
    public List loraUpdate(Long id,String url ){
   return    parkMapper.findAllFee();
 //return // bsParaConfig.processGatewayLora_update(id,url);

    }
    @ResponseBody
    @RequestMapping(value ="/infraredUpdate", method= RequestMethod.POST)
    @ApiOperation(value = "车位检测器升级",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "id",value = "测距缓存长度"),
            @ApiImplicitParam(name = "parkid",value = "定位基站数",defaultValue = "4294967295"),
            @ApiImplicitParam(name = "url",value = "文件地址" )})
    public Boolean infraredUpdate(Long id,Long parkid,String url ){
        return  bsParaConfig.processInfrared_update(id,parkid,url);
    }


    @ResponseBody
    @RequestMapping(value ="/sadasdsa", method= RequestMethod.POST)
    @ApiOperation(value = "kafka",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "id",value = "测距缓存长度"),
            @ApiImplicitParam(name = "parkid",value = "定位基站数",defaultValue = "4294967295"),
            @ApiImplicitParam(name = "url",value = "文件地址" )})
    public String sadas(Long id,Long parkid,String url ){
        kafukaSender.send(KafukaTopics.BS_RANGE_RES,"dadsad");
        return  "sdasd";
    }

  /*  public static void main(String[] args) {
        byte[] ds={(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
        System.out.println(ByteUtils.bytes2long(ds));
    }*/

    @ResponseBody
    @RequestMapping(value ="/infraredPara", method= RequestMethod.POST)
    @ApiOperation(value = "车位检测器参数配置",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "id",value = "下发网关id"),
            @ApiImplicitParam(name = "parkid",value = "车位检测器id",defaultValue = "4294967295"),
            @ApiImplicitParam(name = "check_interval",value = "检测周期"),
            @ApiImplicitParam(name = "heart_interval",value = "上传周期" )})
    public Boolean infraredPara(Long id,Long parkid,byte  check_interval,byte heart_interval  ){
        return  bsParaConfig.processInfrared_para(id,parkid,check_interval,heart_interval);
    }
    @ResponseBody
    @RequestMapping(value ="/loraPara", method= RequestMethod.POST)
    @ApiOperation(value = "433M网关参数配置",notes = "无")
    public Boolean loraPara(Long id,String doamin,String port ){
      return   bsParaConfig.processGatewayLora_para(id,doamin,port);

    }

    public static ConcurrentHashMap<String,Short> tagstate=new ConcurrentHashMap<>();
    @ResponseBody
    @RequestMapping(value ="/controlbspara", method= RequestMethod.POST)
    @ApiOperation(value = "基站参数配置",notes = "无")
    public String test1(@ModelAttribute BsPara bsPara) throws InterruptedException {
       // BsConfig bs = bsConfigService.findByNum(5000 + "");
         res="no res";
       dataProcess.processBsparaConfig(bsPara);
      //  personMapper.findByAll(1,null,null,null,null,null,null);
     //  personService.findByAll(1,null,null,null,null,null,null);
         Thread.sleep(3000);
        return res+"";

    }

/*    @ResponseBody
    @RequestMapping(value ="/tagtext", method= RequestMethod.POST)
    @ApiOperation(value = "标签文本配置",notes = "无")*/
    public String test1(@ModelAttribute TextPara textPara) throws UnsupportedEncodingException {

             short  type=textPara.getType();
        short   level=textPara.getLevel();
        switch (type){
            case 1:
                tagParaConfig.setTagCommonText(textPara.getBsid(),textPara.getTarget(),textPara.getMessageid(),textPara.getText(),textPara.getTime());
                break;
            case 2:
                tagParaConfig.setTagPosition(textPara.getBsid(),textPara.getTarget(),textPara.getMessageid(),(byte) level,textPara.getText(),textPara.getTime());
                break;
            case 3:
                tagParaConfig.setTagWarningText(textPara.getBsid(),textPara.getTarget(),textPara.getMessageid(),textPara.getText(),level,textPara.getTime());
                break;
        }
        return autoKey.getAutoId("random")+"";

    }


/*    @ResponseBody
    @ApiOperation(value = "基站测距",notes = "无")
    @RequestMapping(value ="/range", method= RequestMethod.POST)*/

    public String range(@ModelAttribute BsRange bsRange){
        bsParaConfig.startBsRange(bsRange.getSource(),100,bsRange.getType(),bsRange.getTarget());
        return autoKey.getAutoId("random")+"";

    }

 /*   @ResponseBody
    @RequestMapping(value ="/file", method= RequestMethod.POST)
    @ApiOperation(value = "文件传输配置",notes = "无")*/
    public String range(@ModelAttribute FilePara filePara){
        bsParaConfig.sendBsFile((int) filePara.getInstanceid(),filePara.getBsid(),(int)filePara.getTarget().longValue(),(byte)filePara.getFileType(),filePara.getMessageid(),filePara.getUrl(),""+(int)(filePara.getTime()/1000));
        return autoKey.getAutoId("random")+"";

    }


/*    @ResponseBody
    @RequestMapping(value ="/test", method= RequestMethod.POST)
    @ApiOperation(value = "文件传输配置",notes = "无")*/
    public String range(MultipartFile file,@ModelAttribute TestPara testPara,HttpServletResponse response){
       try {
           if(file.isEmpty()) {
               response.setStatus(404);
               return "empty file";
           }

           if(testPara.testId==null){
               response.setStatus(404);
               return "id is empty";
           }
           file.transferTo(new File("E:/upload/rtls/",file.getName()));
        return "success";
       }catch (Exception e){
           response.setStatus(500);
         return   "failure";
       }

    }

    @ResponseBody
    @RequestMapping(value ="/controltagpara", method= RequestMethod.POST)
    @ApiOperation(value = "标签参数配置",notes = "无")
    public String test1(@ModelAttribute TagPara tagPara){
        res="no res";
      dataProcess.processTagparaConfig(tagPara);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res+"";
      //  tagParaConfig.setTagTimestamp(-1,521, time);


    }

    @ResponseBody
    @RequestMapping(value ="/bledebug", method= RequestMethod.POST)
    @ApiOperation(value = "蓝牙定位调试",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "onePoint",value = "单点定位距离"),
            @ApiImplicitParam(name = "d1",value = "一维定位距离门限"),
            @ApiImplicitParam(name = "over",value = "超过距离门限" )})
    public String ble(Double onePoint,Double d1,Double over){
        if(onePoint!=null)
            mapContainer.blesingledis=onePoint;
        if(d1!=null)
            mapContainer.ble1d=d1;
        if(over!=null)
            mapContainer.bleover=over;

        return autoKey.getAutoId("random")+"";
        //  tagParaConfig.setTagTimestamp(-1,521, time);


    }

    @ResponseBody
    @RequestMapping(value ="/getorigindata", method= RequestMethod.POST)
    @ApiOperation(value = "获取原始测距数据",notes = "无")
  /*  @ApiResponse(code=200,message = "ok",response = Originaldata.class)*/
    public List<Originaldata> test2(Long bsid, Integer tagid){
        List list;
        Originaldata originaldata=new Originaldata();
        originaldata.setRangebsid(bsid+"");
        originaldata.setTagid(tagid+"");
        locationMapper.addOriginaldata(originaldata);
       list=locationMapper.getOriginalDataByBsidAndTagid(bsid,tagid);

       return list;
    }

    @ResponseBody
    @RequestMapping(value ="/fix", method= RequestMethod.POST)
    @ApiOperation(value = "计算基站校正值",notes = "无")
    /*  @ApiResponse(code=200,message = "ok",response = Originaldata.class)*/
    public String tagFix(String original, String real){
        String[] original_str=original.split(",");
        String[] real_str=real.split(",");
        if(original_str.length!=real_str.length){
            return "输入数据错误，长度不匹配";
        }
        int len=original_str.length;
        double[] origin_dis=new double[len];
        double[] real_dis=new double[len];
        for(int i=0;i<len;i++){
            origin_dis[i]=Double.valueOf(original_str[i]);
            real_dis[i]=Double.valueOf(real_str[i]);
        }
        double[] res= CalculateAb.getAb(origin_dis,real_dis);
        return res[0]+","+res[1]+","+res[2];
    }

    @ResponseBody
    @RequestMapping(value ="/setCache", method= RequestMethod.POST)
    @ApiOperation(value = "设置距离和定位数据判据缓存长度",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "dis_len",value = "测距缓存长度"),
            @ApiImplicitParam(name = "location_len",value = "定位缓存长度"),
            @ApiImplicitParam(name = "two_module1D",value = "1D两模块距离差"),
            @ApiImplicitParam(name = "two_module2D",value = "2D两模块距离差"),
            @ApiImplicitParam(name = "delay",value = "缓存清理时间"),
            @ApiImplicitParam(name = "bsnum",value = "定位基站数"),
            @ApiImplicitParam(name = "mode",value = "宽泛定位，0关闭 1开启" )
    })
    public String test1(Integer dis_len, Integer location_len, Float two_module1D,Float two_module2D, Integer delay, Integer bsnum,Integer mode ){
        if(dis_len!=null)
            mapContainer.locationcachelen_highfreq=dis_len;
        if(location_len!=null)
            mapContainer.locationcachelen_highfreq=location_len;
        if(two_module1D!=null)
            mapContainer.two_module_dis1D=two_module1D;
        if(two_module2D!=null)
            mapContainer.two_module_dis2D=two_module2D;
        if(mode!=null)
            mapContainer.location_strictmode=mode;
        if(delay!=null)
            mapContainer.timedelay_highfreq=delay;
        if(bsnum!=null)
            mapContainer.location_bsnum=bsnum;
        if(mode!=null)
            mapContainer.location_strictmode=mode;
        return "设置成功";

    }
    @ResponseBody
    @RequestMapping(value ="/setChannelPara", method= RequestMethod.POST)
    @ApiOperation(value = "设置地沟参数",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "channeldis",value = "一维定位长度，默认50"),
            @ApiImplicitParam(name = "percent",value = "一维定位比例 默认0.1" )
    })
    public String test2(Double channeldis, Double percent ){
        if(channeldis!=null)
            mapContainer.channeldis=channeldis;
        if(percent!=null)
            mapContainer.channelpercent=percent;
        return "设置成功";

    }

    @ResponseBody
    @RequestMapping(value ="/setNedMode", method= RequestMethod.POST)
    @ApiOperation(value = "设置地锁状态",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "bsid",value = "网关id"),
            @ApiImplicitParam(name = "nedid",value = "地锁id"),
            @ApiImplicitParam(name = "mode",value = "0：降锁  1：升锁 2：进入正常模式 3：持续升锁  4：持续降锁   6：进入测试模式" )
    })
    public DeferredResult changeNed_mode(Long bsid ,int nedid , byte mode ){
        DeferredResult deferredResult = new DeferredResult<>(10000l);
        // 设置超时处理
        deferredResult.onTimeout(() -> deferredResult.setErrorResult(new CommonResult<>(400, "操作超時")));

        try {
      Executor executor = SpringContextHolder.getBean("threadPool1");;
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     setNedMode(deferredResult, bsid, nedid, mode);
                 }
             });

         return deferredResult;

       }catch (Exception e){
        e.printStackTrace();
         new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
         return deferredResult;
        }


    }

    void setNedMode(DeferredResult deferredResult,Long bsid ,int nedid , byte mode){
        ByteBuffer buffer= ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(mode);
        buffer.putInt(nedid);
        buffer.putInt(0);
        byte[] msgid={0x00,0x00,0x00,0x00};
        Ned ned=mapContainer.device_Ned.get((long)nedid);
        logger.error("xxxxxxned:"+nedid+":ned.cmd:");//切换到正常模式指令执行结果
        if(ned==null|| (new Date().getTime()-ned.getDate().getTime())/1000>1500){
            deferredResult.setResult(new CommonResult<>(203, "未与设备通信成功，请检查附近网关和地锁状态"));
        }else{
            ned.cmd=(int)mode;
            ned.state=-1;
            getSendData().sendDataToLora(ned.bsid,CMD_NED_CONTROL,msgid,buffer.array());//通过网关发送控制指令
            sendData.sendCat1CmdToNed((long)nedid,mode,0);//cat1发送指令
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
          sendData.sendDataToLora(ned.bsid,CMD_NED_CONTROL,msgid,buffer.array());
            sendData.sendCat1CmdToNed((long)nedid,mode,0);//cat1发送指令
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        /*      sendData.sendDataToLora(ned.bsid,CMD_NED_CONTROL,msgid,buffer.array());
            sendData.sendCat1CmdToNed((long)nedid,mode,0);//cat1发送指令*/
            int k=0;
            while(ned.state==-1&&k<15){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                k++;

            }
            String notice="指令执行成功";
            Integer code=200;
            if(ned.state==-1){
                code=201;
                 notice="指令执行超时";
            }else if(ned.state==1){
                notice="指令执行失败";
                logger.error("ned:"+nedid+":ned.cmd:"+ned.cmd+"ned.state"+ned.state);//切换到正常模式指令执行结果
                code=202;
            }else if(ned.state==0){
                notice="指令执行成功";

            }
            deferredResult.setResult(new CommonResult<>(code, notice,ned.toJsonString()));
        }


    }

    private SendData getSendData() {
        return sendData;
    }

    /*    @ResponseBody
        @RequestMapping(value ="/mode", method= RequestMethod.POST)
        @ApiOperation(value = "mode",notes = "批量测试模式")*/
    public DeferredResult test1(int start,int end,long bsid,short mode,int period){
        CommonResult response= new CommonResult<>();
        tagstate.clear();
        DeferredResult defer=new DeferredResult(15000l) ;
        for(int i=start;i<=end;i++){
            tagParaConfig.setTagMode(bsid,i,(byte)mode,(byte)period);
            tagstate.putIfAbsent(i+"",mode);
        }
        defer.onTimeout(new Runnable() {
            @Override
            public void run() {
                String tagNotReady="";
                Boolean tagready=true;
                for (Map.Entry<String,Short> tagstateentity:tagstate.entrySet()
                ) {
                    String tagid=tagstateentity.getKey();
                    Short value=tagstateentity.getValue();
                    if(value.shortValue()!=10){
                        tagNotReady=tagNotReady+","+tagid;
                        tagready=false;
                    }
                }
                if(tagready){
                 defer.setResult("success");
                }else{
                    defer.setResult("failure"+tagNotReady);
                }
            }
        });


        return defer;

    }

    @GetMapping("/send")
    public String send(){
        //建立邮箱消息
        SimpleMailMessage message = new SimpleMailMessage();
        //发送者
        message.setFrom(username);
        //接收者
        message.setTo("mayh@tuguiyao-gd.com");
        //发送标题
        message.setSubject("测试");
        //发送内容
        message.setText("测试数据");
        jsm.send(message);
        File file = new File("E:/upload/web");
        // get the folder list
        File[] array = file.listFiles();
        String[] path=new String[array.length];
        for (int i=0;i<array.length;i++){
            path[i]=array[i].getAbsolutePath();
        }
        mailService.sendAttachmentsMail(username,"mayh@tuguiyao-gd.com","test","测试",path);
        return "1";
    }

    @GetMapping("/bstest")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "基站开始编号"),
            @ApiImplicitParam(name = "end",value = "基站结束编号"),
            @ApiImplicitParam(name = "stopFlag",value = "停止测试标志位，结束测试时填0")
    })
    @ApiOperation(value = "基站模拟测试",notes = "无")
    public String addBs(Integer start,Integer end,Integer stopFlag) throws InterruptedException {
        MutilClient.FLAG=false;
        Thread.sleep(5000);
        if(stopFlag==null) {
            MutilClient.FLAG = true;
            MutilClient.mutilClient(ip, start, end);
        }
        return "success";
    }
    @GetMapping("/tagtest")
    @ApiOperation(value = "标签模拟测试",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "tagStart",value = "标签开始编号"),
            @ApiImplicitParam(name = "tagEnd",value = "标签结束编号"),
            @ApiImplicitParam(name = "bsStart",value = "测距基站结束标志位"),
            @ApiImplicitParam(name = "bsEnd",value = "测距基站结束标志位，可不填"),
            @ApiImplicitParam(name = "stopFlag",value = "停止测试标志位，结束测试时填0")
    })
    public String addBs(Integer tagStart,Integer tagEnd, Integer bsStart, Integer bsEnd,Integer stopFlag) throws InterruptedException {
        UdpClientHandler.FLAG=false;
        Thread.sleep(7000);
        if(stopFlag==null) {
            UdpClientHandler.FLAG = true;
            NettyServer.startUdpClient(ip, tagStart, tagEnd, bsStart, bsEnd);
        }
        return "success";
    }

    @RequestMapping("/file")
    public void addBs(MultipartFile file, HttpServletResponse response) throws InterruptedException {
        try {
           StorePath sadsa = fastdfsUtils.upload(file);
           System.out.println(sadsa.getFullPath());
       //     fastdfsUtils.download("http://172.19.0.1/group1/M00/00/00/rBMAA2BJ5yyAC0tnAAxRyGjmOpw8130_big.so","1212",response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping("/inFrared")
    @ApiOperation(value = "车位检测器数据分析",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "开始时间"),
            @ApiImplicitParam(name = "end",value = "结束时间"),
            @ApiImplicitParam(name = "gatewaynum",value = "网关编号"),
            @ApiImplicitParam(name = "infrarednum",value = "车位检测器编号"),
            @ApiImplicitParam(name = "state",value = "状态，0空闲  1占用"),
            @ApiImplicitParam(name = "count",value = "时间戳"),
            @ApiImplicitParam(name = "warning",value = "两条时间间隔异常值，默认1000秒"),
            @ApiImplicitParam(name = "offTime",value = "两条时间间隔异常值，默认1000秒")
    })
    public Map test(String start ,String end ,String gatewaynum,String infrarednum,Integer state ,Integer count,@RequestParam(defaultValue = "1000") Integer warning,
                    @RequestParam(defaultValue = "1000") Integer offTime,@RequestParam(defaultValue = "0") Integer data_type) throws InterruptedException {
        boolean gate_boolean=isNumeric(gatewaynum);
        boolean inf_boolean=isNumeric(infrarednum);
        int gate=gate_boolean?Integer.valueOf(gatewaynum):Integer.decode(gatewaynum).intValue();
        int infrared=inf_boolean?Integer.valueOf(infrarednum):Integer.decode(infrarednum).intValue();
        List<InfraredOrigin> list = gatewayService.findInfraredOrigin(start, end, gate, infrared, state, count);
        List<InfraredOrigin> overTime=new ArrayList<>();
        List<InfraredOrigin> offLineTime=new ArrayList<>();
        List<InfraredOrigin> lost=new ArrayList<>();
        int len=list.size()-1;
        for(int i=0;i<len;i++){
            InfraredOrigin inf0 = list.get(i);
            InfraredOrigin inf1 = list.get(i + 1);
            switch (data_type){
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    inf0.setOt(0l);
                    inf0.setRssi(0);
                    inf0.setState((short)0);
                    break;
            }
            Long ot= ChronoUnit.MILLIS.between(inf1.getTimestamp(),inf0.getTimestamp())/1000;
            inf1.setOt(ot);
            if(ot>warning){
                overTime.add(inf0);
                overTime.add(inf1);
            }
            if(ot>offTime){
                offLineTime.add(inf0);
                offLineTime.add(inf1);
            }
            if(inf0.getCount()==inf1.getCount()&&ot<70){
               inf1.setTime_count(inf0.getTime_count()+1);
                inf0.setTime_count(0);
            }else{
                lost.add(inf0);
            }
        }

        Map data=new HashMap();
        //data.put("errorList",overTime);
        switch (data_type){
            case 0:
                data.put("allList",list);
                break;
            case 1:
                data.put("allList",offLineTime);
                break;
            case 2:
                data.put("allList",lost);
                break;
        }

            return data;

    }


    @RequestMapping(value = "/infraredDataAnalyze",method = RequestMethod.POST)
    @ApiOperation(value = "车位检测器数据分析",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "开始时间"),
            @ApiImplicitParam(name = "end",value = "结束时间"),
            @ApiImplicitParam(name = "gatewaynum",value = "网关编号"),
            @ApiImplicitParam(name = "infrarednum",value = "车位检测器编号"),
            @ApiImplicitParam(name = "state",value = "状态，0空闲  1占用")
    })
    public Object infraredDataAnalyze(String start ,String end ,String gatewaynum,String infrarednum,Integer state,@RequestParam(defaultValue = "1000") Integer warning,
                                      @RequestParam(defaultValue = "1000") Integer offTime,@RequestParam(defaultValue = "0") Integer data_type){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        boolean gate_boolean=isNumeric(gatewaynum);
        boolean inf_boolean=isNumeric(infrarednum);
        gatewaynum= String.valueOf(gate_boolean?Integer.parseInt(gatewaynum): Integer.decode(gatewaynum));
        infrarednum= String.valueOf(inf_boolean?Integer.parseInt(infrarednum): Integer.decode(infrarednum));
        List<ESInfraredOriginal> esInfraredOriginals = esInfraredOriginalImpl.searchInfraredOriginal(gatewaynum, infrarednum, state, start, end);
        List<ESInfraredOriginal> overTime=new ArrayList<>();
        List<ESInfraredOriginal> offLineTime=new ArrayList<>();
        List<ESInfraredOriginal> lost=new ArrayList<>();
        int len=esInfraredOriginals.size()-1;
        for(int i=0;i<len;i++){
            ESInfraredOriginal inf0 = esInfraredOriginals.get(i);
            ESInfraredOriginal inf1 = esInfraredOriginals.get(i + 1);
            switch (data_type){
                case 0:
                case 1:
                    break;
                case 2:
                    inf0.setOt(0L);
                    inf0.setRssi(0);
                    inf0.setState(0);
                    break;
            }
            long ot= ChronoUnit.MILLIS.between(LocalDateTime.parse(inf1.getTimestamp(),formatter),LocalDateTime.parse(inf0.getTimestamp(),formatter))/1000;

            inf1.setOt(ot);
            if(ot>warning){
                overTime.add(inf0);
                overTime.add(inf1);
            }
            if(ot>offTime){
                offLineTime.add(inf0);
                offLineTime.add(inf1);
            }
            if(inf0.getCount().equals(inf1.getCount()) &&ot<70){
                inf1.setTime_count(inf0.getTime_count()+1);
                inf0.setTime_count(0);
            }else{
                lost.add(inf0);
            }
        }

        Map<String,Object> data=new HashMap<>();
        //data.put("errorList",overTime);
        switch (data_type){
            case 0:
                data.put("allList",esInfraredOriginals);
                break;
            case 1:
                data.put("allList",offLineTime);
                break;
            case 2:
                data.put("allList",lost);
                break;
        }
        return data;
    }


    @RequestMapping("/exportFrared2")
    @ApiOperation(value = "车位检测器数据分析",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "开始时间"),
            @ApiImplicitParam(name = "end",value = "结束时间"),
            @ApiImplicitParam(name = "gatewaynum",value = "网关编号"),
            @ApiImplicitParam(name = "infrarednum",value = "车位检测器编号"),
            @ApiImplicitParam(name = "state",value = "状态，0空闲  1占用"),
            @ApiImplicitParam(name = "count",value = "时间戳"),
            @ApiImplicitParam(name = "warning",value = "两条时间间隔异常值，默认1000秒"),
            @ApiImplicitParam(name = "offTime",value = "两条时间间隔异常值，默认1000秒")
    })
    public void export2(String start , String end , String gatewaynum, String infrarednum,@RequestParam(defaultValue = "1000") Integer warning,
                       @RequestParam(defaultValue = "1000") Integer offTime, @RequestParam(defaultValue = "0") Integer data_type,  HttpServletResponse response) {

        response.setContentType("application/binary;charset=UTF-8");
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            List<ESInfraredOriginal> esInfraredOriginals = esInfraredOriginalImpl.searchInfraredOriginal(gatewaynum, infrarednum, null, start, end);

            List<ESInfraredOriginal> overTime=new ArrayList<>();
            List<ESInfraredOriginal> offLineTime=new ArrayList<>();
            List<ESInfraredOriginal> lost=new ArrayList<>();
            int len=esInfraredOriginals.size()-1;
            String typeText="";
            for(int i=0;i<len;i++){
                ESInfraredOriginal inf0 = esInfraredOriginals.get(i);
                ESInfraredOriginal inf1 = esInfraredOriginals.get(i + 1);
                switch (data_type){
                    case 0:
                        typeText="原始";
                        break;
                    case 1:
                        typeText="离线";
                        break;
                    case 2:
                        typeText="丢包";
                        inf0.setOt(null);
                        inf0.setRssi(null);
                        inf0.setState(null);
                        break;
                }

                Long ot= ChronoUnit.MILLIS.between(LocalDateTime.parse(inf1.getTimestamp(),formatter),LocalDateTime.parse(inf0.getTimestamp(),formatter))/1000;
                inf1.setOt(ot);
                if(ot>warning){
                    overTime.add(inf0);
                    overTime.add(inf1);
                }
                if(ot>offTime){
                    offLineTime.add(inf0);
                    offLineTime.add(inf1);
                }
                if(inf0.getCount()==inf1.getCount()&&ot<70){
                    inf1.setTime_count(inf0.getTime_count()+1);
                    inf0.setTime_count(0);
                }else{
                    lost.add(inf0);
                }
            }

            String fileName = new String(("网关-"+gatewaynum+":车位检测器-"+infrarednum+"-"+start+"-"+end+typeText+"统计").getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            personService.exportInfrered2(response.getOutputStream(),esInfraredOriginals,"网关-"+gatewaynum+"-车位检测器-"+infrarednum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping("/exportFrared")
    @ApiOperation(value = "车位检测器数据分析",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "开始时间"),
            @ApiImplicitParam(name = "end",value = "结束时间"),
            @ApiImplicitParam(name = "gatewaynum",value = "网关编号"),
            @ApiImplicitParam(name = "infrarednum",value = "车位检测器编号"),
            @ApiImplicitParam(name = "state",value = "状态，0空闲  1占用"),
            @ApiImplicitParam(name = "count",value = "时间戳"),
            @ApiImplicitParam(name = "warning",value = "两条时间间隔异常值，默认1000秒"),
            @ApiImplicitParam(name = "offTime",value = "两条时间间隔异常值，默认1000秒")
    })
    public void export(String start , String end , String gatewaynum, String infrarednum, Integer state , Integer count, @RequestParam(defaultValue = "1000") Integer warning,
                       @RequestParam(defaultValue = "1000") Integer offTime, @RequestParam(defaultValue = "0") Integer data_type,  HttpServletResponse response) throws InterruptedException {

        response.setContentType("application/binary;charset=UTF-8");
        try {
            boolean gate_boolean=isNumeric(gatewaynum);
            boolean inf_boolean=isNumeric(infrarednum);
            int gate=gate_boolean?Integer.valueOf(gatewaynum):Integer.decode(gatewaynum).intValue();
            int infrared=inf_boolean?Integer.valueOf(infrarednum):Integer.decode(infrarednum).intValue();
            List<InfraredOrigin> list = gatewayService.findInfraredOrigin(start, end, gate, infrared, state, count);

            List<InfraredOrigin> overTime=new ArrayList<>();
            List<InfraredOrigin> offLineTime=new ArrayList<>();
            List<InfraredOrigin> lost=new ArrayList<>();
            int len=list.size()-1;
            String typeText="";
            for(int i=0;i<len;i++){
                InfraredOrigin inf0 = list.get(i);
                InfraredOrigin inf1 = list.get(i + 1);
                switch (data_type){
                    case 0:
                        typeText="原始";
                        break;
                    case 1:
                        typeText="离线";
                        break;
                    case 2:
                        typeText="丢包";
                        inf0.setOt(null);
                        inf0.setRssi(null);
                        inf0.setState(null);
                        break;
                }
                Long ot= ChronoUnit.MILLIS.between(inf1.getTimestamp(),inf0.getTimestamp())/1000;
                inf1.setOt(ot);
                if(ot>warning){
                    overTime.add(inf0);
                    overTime.add(inf1);
                }
                if(ot>offTime){
                    offLineTime.add(inf0);
                    offLineTime.add(inf1);
                }
                if(inf0.getCount()==inf1.getCount()&&ot<70){
                    inf1.setTime_count(inf0.getTime_count()+1);
                    inf0.setTime_count(0);
                }else{
                    lost.add(inf0);
                }
            }

            Map data=new HashMap();
            //data.put("errorList",overTime);
            switch (data_type){
                case 0:
                    data.put("allList",list);
                    break;
                case 1:
                    data.put("allList",offLineTime);
                    break;
                case 2:
                    data.put("allList",lost);
                    break;
            }
            String fileName = new String(("网关-"+gate+":车位检测器-"+infrared+"-"+start+"-"+end+typeText+"统计").getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            personService.exportInfrered(response.getOutputStream(),(List)data.get("allList"),"网关-"+gate+"-车位检测器-"+infrared);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/exportFraredCount")
    @ApiOperation(value = "车位检测器数据分析",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "开始时间"),
            @ApiImplicitParam(name = "end",value = "结束时间"),
            @ApiImplicitParam(name = "infrarednum",value = "车位检测器编号"),
            @ApiImplicitParam(name = "gatewaynum",value = "网关编号"),
    })
    public void exportCount(String start , String end ,String infrarednum,String gatewaynum,   HttpServletResponse response) throws InterruptedException, IOException {

        response.setContentType("application/binary;charset=UTF-8");
        Integer gate=null;
        Integer infraredd=null;
        boolean gate_boolean=isNumeric(gatewaynum);
        boolean inf_boolean=isNumeric(infrarednum);
        if(gatewaynum!=null&&gatewaynum.length()!=0)
         gate=gate_boolean?Integer.valueOf(gatewaynum):Integer.decode(gatewaynum).intValue();
        if(infrarednum!=null&&infrarednum.length()!=0)
         infraredd=inf_boolean?Integer.valueOf(infrarednum):Integer.decode(infrarednum).intValue();
        Map final_lsit=new HashMap();
      /* if(infrarednum==null&&infrarednum.length()==0)*/{
          List<Integer> infrareds=  gatewayService.findAllInfrared(start, end,gate,infraredd);

            for (Integer infrared:infrareds) {
               List<Integer> gateWay = gatewayService.findInfraredCount(start, end, infrared);
               List dasd = new ArrayList();
               for (Integer num : gateWay) {
                   System.out.println("num"+num.intValue());
                   System.out.println("gate"+gate.intValue());
                   if(gate==null||num.intValue()==gate.intValue())
                   {
                      // System.out.println("gate--"+gate.intValue());
                       List<InfraredOrigin> list = gatewayService.findInfraredOrigin(start, end, num, infrared, null, null);
                       InfraredOriginCount sss = infraredNumCount(list);
                       sss.setGatewaynum(num);
                       //sss.setInfrarednum();
                       dasd.add(sss);
                   }

               }
               System.out.println("新添加导出"+infrared);
               final_lsit.put(infrared,dasd);
           }



       }/*else {

           List dasd = new ArrayList();
           List<Integer> gateWay = gatewayService.findInfraredCount(start, end, infraredd);
           for (Integer num : gateWay
           ) {
               List<InfraredOrigin> list = gatewayService.findInfraredOrigin(start, end, num, infraredd, null, null);
               InfraredOriginCount sss = infraredNumCount(list);
               sss.setGatewaynum(num);
               //sss.setInfrarednum();
               dasd.add(sss);
           }
           final_lsit.put(infraredd,dasd);
       }*/
        String fileName = new String((start+"-"+end+"丢包统计").getBytes(), StandardCharsets.ISO_8859_1);
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
        personService.exportInfreredCount(response.getOutputStream(),final_lsit);



    }
    @RequestMapping(value = "/bbbbxxx")
    public CommonResult beacondataxx(@RequestBody NedData nedData) {
        try {
            NedData sss=  nedData;
        } catch (Exception e) {
            return new CommonResult(400, LocalUtil.get(KafukaTopics.SEND_FAIL));
        }
        return new CommonResult(200, LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
    }

    @RequestMapping("/exportFraredCount2")
    @ApiOperation(value = "车位检测器数据分析",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "开始时间"),
            @ApiImplicitParam(name = "end",value = "结束时间"),
            @ApiImplicitParam(name = "infrarednum",value = "车位检测器编号"),
            @ApiImplicitParam(name = "gatewaynum",value = "网关编号"),
    })
    public void exportCount2(String start , String end ,String infrarednum,String gatewaynum,HttpServletResponse response) throws InterruptedException, IOException {

        response.setContentType("application/binary;charset=UTF-8");
        Integer gate=null;
        Integer infraredd=null;
        boolean gate_boolean=isNumeric(gatewaynum);
        boolean inf_boolean=isNumeric(infrarednum);
        if(gatewaynum!=null&&gatewaynum.length()!=0)
            gate=gate_boolean?Integer.valueOf(gatewaynum):Integer.decode(gatewaynum).intValue();
        if(infrarednum!=null&&infrarednum.length()!=0)
            infraredd=inf_boolean?Integer.valueOf(infrarednum):Integer.decode(infrarednum).intValue();
            List<ESInfraredOriginal> esInfraredOriginals = esInfraredOriginalImpl.searchInfraredOriginal(String.valueOf(gate), String.valueOf(infraredd), null, start, end);
            // List<InfraredOrigin> InfraredOriginal = esInfraredOriginals.stream()
            //     .map(esInfrared -> {
            //         InfraredOrigin origin = new InfraredOrigin();
            //         // 在这里进行属性赋值，根据 ESInfraredOriginal 对象的属性设置 InfraredOriginal 对象的属性
            //         // 例如：infrared.setId(esInfrared.getId());
            //         origin.setGatewaynum(Integer.valueOf(esInfrared.getGatewayNum()));
            //         origin.setInfrarednum(Integer.valueOf(esInfrared.getInfraredNum()));
            //         origin.setTimestamp(LocalDateTime.parse(esInfrared.getTimestamp()));
            //         origin.setCount(esInfrared.getCount().shortValue());
            //         origin.setState(esInfrared.getState().shortValue());
            //         origin.setPower(Integer.valueOf(esInfrared.getGatewayNum()));
            //         origin.setRssi(esInfrared.getRssi());
            //         return origin;
            //     })
            //     .collect(Collectors.toList());
        List<ESInfraredOriginal> esInfraredOriginalList = new ArrayList<>();
        ESInfraredOriginal sss = infraredNumCount2(esInfraredOriginals);
        esInfraredOriginalList.add(sss);

        String fileName = new String((start+"-"+end+"丢包统计").getBytes(), StandardCharsets.ISO_8859_1);
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
        personService.exportInfreredCount2(response.getOutputStream(),esInfraredOriginalList);
    }


    @RequestMapping("/state")
    @ApiOperation(value = "车位检测器数据分析",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(name = "start",value = "开始时间"),
            @ApiImplicitParam(name = "end",value = "结束时间"),
            @ApiImplicitParam(name = "gatewaynum",value = "网关编号"),
            @ApiImplicitParam(name = "infrarednum",value = "车位检测器编号"),
            @ApiImplicitParam(name = "state",value = "状态，0空闲  1占用"),
    })
    public Map test(String start ,String end ,String gatewaynum,String infrarednum,String state) throws InterruptedException {
        Map data=new HashMap();
        //data.put("errorList",overTime);
        System.out.println(start+":"+end+":"+gatewaynum+":"+infrarednum+":"+state);


        if(!NullUtils.isEmpty(gatewaynum)){
            if(gatewaynum.equals("-1")){
            gatewaynum=null;
            }
            Object   res= gatewayService.findGateWayState(start,end,gatewaynum,NullUtils.isEmpty(state)?null:Integer.valueOf(state));
            data.put("allList",res);


        }
        if(!NullUtils.isEmpty(infrarednum)){
            if(infrarednum.equals("-1")){
                infrarednum=null;
            }
            Object   res=   gatewayService.findInfraredState(start,end,infrarednum,state);
            data.put("allList",res);
        }


        return data;

    }


    public static ESInfraredOriginal infraredNumCount2(List<ESInfraredOriginal> list){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<ESInfraredOriginal> lost=new ArrayList<>();

        ESInfraredOriginal infraredOriginCount=  new ESInfraredOriginal();
        int len=list.size()-1;
        int warning=1000;
        int offTime=1000;
        // System.out.println("list"+list.size());
        for(int i=0;i<len;i++){
            ESInfraredOriginal inf0 = list.get(i);
            ESInfraredOriginal inf1 = list.get(i + 1);
            infraredOriginCount.setInfraredNum(inf0.getInfraredNum());

            Long ot= ChronoUnit.MILLIS.between(LocalDateTime.parse(inf1.getTimestamp(),formatter),LocalDateTime.parse(inf0.getTimestamp(),formatter))/1000;
            inf1.setOt(ot);

            if(inf0.getCount()==inf1.getCount()&&ot<70){
                inf1.setTime_count(inf0.getTime_count()+1);
                inf0.setTime_count(0);
            }else{
                lost.add(inf0);
            }
            if(infraredOriginCount.getTotal_start()==null){
                infraredOriginCount.setTotal_start(inf0.getCount());
            }
            if(inf1.getCount()>inf0.getCount()&&ot>=0){
                infraredOriginCount.setTotal(infraredOriginCount.getTotal()+4*(inf1.getCount()-infraredOriginCount.getTotal_start()));
                infraredOriginCount.setTotal_start(inf1.getCount());
                int ss = inf1.getCount() - inf0.getCount();
                // System.out.println("时间戳差"+inf0.getCount());
                // System.out.println("时间戳差"+ss);
                if(inf1.getCount()-inf0.getCount()>1) {
                    infraredOriginCount.setTotal_back(infraredOriginCount.getTotal_back()+inf1.getCount()-inf0.getCount()-1);
                    infraredOriginCount.setBack_detail(infraredOriginCount.getBack_detail()+inf0.getTimestamp()+"-"+inf1.getTimestamp()+"-时间戳"+inf0.getCount()+"-"+inf1.getCount()+"\r\n"
                    );
                }else{

                }
            }
            if(inf1.getCount()<inf0.getCount()&&ot>=0){
                infraredOriginCount.setTotal(infraredOriginCount.getTotal()+4*(inf1.getCount()+128-infraredOriginCount.getTotal_start()));
                infraredOriginCount.setTotal_start(inf1.getCount());
                if(inf1.getCount()+128-inf0.getCount()>1){
                    infraredOriginCount.setTotal_back(infraredOriginCount.getTotal_back()+inf1.getCount()+128-inf0.getCount()-1);
                    infraredOriginCount.setBack_detail(infraredOriginCount.getBack_detail()+inf0.getTimestamp()+"-"+inf1.getTimestamp()+"-时间戳"+inf0.getCount()+"-"+inf1.getCount()+"\r\n"
                    );
                }
            }
        }
        infraredOriginCount.setTotal_receive(len+1);
        return infraredOriginCount;
    }


//计算单个车位检测器一段时间内各个网关的丢包率和丢数率
    public static InfraredOriginCount infraredNumCount( List<InfraredOrigin> list){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<InfraredOrigin> lost=new ArrayList<>();

        InfraredOriginCount infraredOriginCount=  new InfraredOriginCount();
        int len=list.size()-1;
        int warning=1000;
        int offTime=1000;
        System.out.println("list"+list.size());
        for(int i=0;i<len;i++){
            InfraredOrigin inf0 = list.get(i);
            InfraredOrigin inf1 = list.get(i + 1);

            Long ot= ChronoUnit.MILLIS.between(inf1.getTimestamp(),inf0.getTimestamp())/1000;
            inf1.setOt(ot);

            if(inf0.getCount()==inf1.getCount()&&ot<70){
                inf1.setTime_count(inf0.getTime_count()+1);
                inf0.setTime_count(0);
            }else{
                lost.add(inf0);
            }
            if(infraredOriginCount.getTotal_start()==null){
                infraredOriginCount.setTotal_start(inf0.getCount().intValue());
            }
            if(inf1.getCount()>inf0.getCount()&&ot>=0){
                infraredOriginCount.setTotal(infraredOriginCount.getTotal()+4*(inf1.getCount()-infraredOriginCount.getTotal_start()));
                infraredOriginCount.setTotal_start(inf1.getCount().intValue());
                int ss = inf1.getCount() - inf0.getCount();
                System.out.println("时间戳差"+inf0.getCount());
                System.out.println("时间戳差"+ss);
                if(inf1.getCount()-inf0.getCount()>1) {
                    infraredOriginCount.setTotal_back(infraredOriginCount.getTotal_back()+inf1.getCount()-inf0.getCount()-1);
                    infraredOriginCount.setBack_detail(infraredOriginCount.getBack_detail()+inf0.getTimestamp().format(formatter)+"-"+
                            inf1.getTimestamp().format(formatter)+"-时间戳"+inf0.getCount()+"-"+inf1.getCount()+"\r\n"
                    );
                }else{

                }
            }
            if(inf1.getCount()<inf0.getCount()&&ot>=0){
                infraredOriginCount.setTotal(infraredOriginCount.getTotal()+4*(inf1.getCount()+128-infraredOriginCount.getTotal_start()));
                infraredOriginCount.setTotal_start(inf1.getCount().intValue());
                if(inf1.getCount()+128-inf0.getCount()>1){
                    infraredOriginCount.setTotal_back(infraredOriginCount.getTotal_back()+inf1.getCount()+128-inf0.getCount()-1);
                    infraredOriginCount.setBack_detail(infraredOriginCount.getBack_detail()+inf0.getTimestamp().format(formatter)+"-"+
                           inf1.getTimestamp().format(formatter)+"-时间戳"+inf0.getCount()+"-"+inf1.getCount()+"\r\n"
                    );
                }

            }
        }
        infraredOriginCount.setTotal_receive(len+1);
                return infraredOriginCount;
    }

    private static boolean isNumeric(String cadena) {
        try {
            Long.parseLong(cadena);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static void main(String[] args) {
      /*  List sss=new ArrayList();
        InfraredOrigin data_0 = new InfraredOrigin();
        data_0.setTimestamp(new Date());
        data_0.setCount((short)6);
        sss.add(data_0);
        InfraredOrigin data_1 = new InfraredOrigin();
        data_1.setTimestamp(new Date());
        data_1.setCount((short)7);
        sss.add(data_1);
        InfraredOrigin data_2 = new InfraredOrigin();
        data_2.setTimestamp(new Date());
        data_2.setCount((short)7);
        sss.add(data_2);
        InfraredOrigin data_3 = new InfraredOrigin();
        data_3.setTimestamp(new Date());
        data_3.setCount((short)7);
        sss.add(data_3);
        InfraredOrigin data_4 = new InfraredOrigin();
        data_4.setTimestamp(new Date());
        data_4.setCount((short)127);
        sss.add(data_4);

        InfraredOrigin data_5 = new InfraredOrigin();
        data_5.setTimestamp(new Date());
        data_5.setCount((short)4);
        sss.add(data_5);
        InfraredOriginCount ssss = infraredNumCount(sss);

        System.out.println("total:"+ssss.getTotal());
        System.out.println("total_receive:"+ssss.getTotal_receive());
        System.out.println("total_back:"+ssss.getTotal_back());*/


    }

    @GetMapping("/redis")
    public String redis(){

        InfraredOrigin origin=new InfraredOrigin();
        origin.setGatewaynum(3);
        origin.setInfrarednum(4);
        origin.setTimestamp(LocalDateTime.now());
      gatewayService.addInfraredOrigin(origin);


        InfraredOrigin origin1=new InfraredOrigin();
        origin1.setGatewaynum(3);
        origin1.setInfrarednum(new Random().nextInt(10));
        origin1.setTimestamp(LocalDateTime.now());
        gatewayService.addInfraredOrigin(origin);
     /*   HashMap map=new HashMap();
        map.put("1001",new Person());
        redisService.setTagData("1-1",map);
        Map map1 = redisService.getTagData("1-1");
        if(map1!=null){
            Person person2 = new Person();
            person2.setBirthday("1988-07-13");
            map1.put("1002",person2);
            redisService.setTagData("1-1",map1);
        }
        Map map2 = redisService.getTagData("1-1");
        Person sdas =(Person) map2.get("1002");
*/
        return "1";
    }

/*    @GetMapping("/refresh")
    public String refresh() throws SQLException
    {
        DruidDataSource master = SpringContextHolder.getBean("dataSource");
        master.setUrl(druidConfiguration.getDbUrl());
        master.setUsername(druidConfiguration.getUsername());
        master.setPassword(druidConfiguration.getPassword());
        master.setDriverClassName(druidConfiguration.getDriverClassName());
        master.restart();
        return "ss";
    }*/

}
