package com.tgy.rtls.location.controller.NB_IOT;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.nb_device.BusinessCarports;
import com.tgy.rtls.data.entity.nb_device.Nb_device;
import com.tgy.rtls.data.entity.park.GuideScreenDevice;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.park.GuideScreenDeviceMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.location.config.deviceconfig.ScreenConfig;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.test.MutilClient;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/park/update/status")
public class NB_Controller {



    @Autowired(required = false)
    KafukaSender kafukaSender;
    @Autowired
    TagService tagService;
    @Autowired
    MapContainer mapContainer;
    @Autowired
    SubService subService;
    @Autowired(required = false)
    TagMapper tagMapper;
    @Autowired(required = false)
    private ParkMapper parkMapper;
    private double batter=0.15;//标签电压变化临界值
    @Autowired(required = false)
    private GuideScreenDeviceMapper guideScreenDeviceMapper;
    @Autowired(required = false)
    private com.tgy.rtls.location.config.deviceconfig.ScreenConfig ScreenConfig;
    @ResponseBody
    @RequestMapping(value ="/businessCarports")
    public JSONObject businessCarports(@RequestBody JSONObject jsonObject
                                    ){

        try {
            Nb_device nb_device = (Nb_device) JSONObject.toBean(jsonObject, Nb_device.class);
            System.out.println("mac" + nb_device.getMac());
            System.out.println("status" + nb_device.getStatus());
            System.out.println("voltage" + nb_device.getVoltage());
            System.out.println("berthcode" + nb_device.getBerthcode());
            processNbDevice(nb_device);

        }catch (Exception e){
            System.out.println(e);
            BusinessCarports businessCarports = (BusinessCarports) JSONObject.toBean(jsonObject, BusinessCarports.class);
            if(businessCarports.getType()!=null) {
                switch (businessCarports.getType()) {
                    case "EVENT_DEVICE_ADD":
                        System.out.println( "EVENT_DEVICE_ADD:::"+businessCarports.getDevice_id());
                        break;
                    case "EVENT_DEVICE_REMOVE":
                        System.out.println( "EVENT_DEVICE_REMOVE::"+businessCarports.getDevice_id());
                        break;
                    default:

                        break;
                }
            }
        }

        JSONObject res = new JSONObject();
        res.put("message","update businessCarport success");
        res.put("status","1001");
        return res;

    }

    @ResponseBody
    @RequestMapping(value ="/parkLeftPlacesCount")
    @ApiOperation(value = "停车场闸机上传剩余车位数",notes = "无")
    public CommonResult<Object> loraPara(String name,Integer count,Integer map ){
        try {
            if(name==null){
                return new CommonResult<>(500,"车场名称不能为空",null);
            }
            if(count==null){
                return new CommonResult<>(500,"车场剩余车位数不能为空",null);
            }
            Integer guideNum= guideScreenDeviceMapper.get4GDeviceNameByScreenName(name,map);
            if(guideNum!=null){
                List<GuideScreenDevice> guideScreenDevices= guideScreenDeviceMapper.getGuideScreenDeviceById( guideNum,null,null);
                if(guideScreenDevices!=null&&guideScreenDevices.size()>0){
                    GuideScreenDevice guideScreenDevice = guideScreenDevices.get(0);
                    ScreenConfig.sendEmpty_placeToScreen_S(guideScreenDevice.getDeviceId(),count,name);
                }else{
                    return new CommonResult<>(500,"显示屏未绑定任何无线设备",null);
                }
            }else{
                return new CommonResult<>(500,"闸机未绑定任何显示屏",null);
            }
        }catch (Exception e){
            return new CommonResult<>(500,"数据处理异常",e.toString());
        }

            return new CommonResult<>(200,"上传成功",null);

    }
    void processNbDevice(Nb_device nb){

        String mac=nb.getMac();
        nb.setUploadtime(nb.getUploadtime()/1000);
      List list= tagService.findByNbMac(mac,null);
        Nb_device nb_device=null;
       if(list==null || list.size()==0){
           tagService.addNb(nb);
       }else {
           nb_device=(Nb_device) list.get(0);
           //如果电压变化超过了临界值就修改
           boolean result=Math.abs((Integer.valueOf(nb.getVoltage())-Integer.valueOf(nb_device.getVoltage()))/100d)>=batter?true:false;
           boolean result1=nb_device.getStatus().shortValue()!=nb.getStatus().shortValue()?true:false;
           boolean result2=nb_device.getBerthcode().equals(nb.getBerthcode())?false:true;
           if (result||result1||result2){
               nb.setId(nb_device.getId());
               tagService.updateNb(nb);
           }
       }
    }

    void ss(String parkid,int state,int power) {
        List<Infrared> infrareds = tagMapper.findIredByIdAndName(null, null, parkid + "");
        Infrared infrared;


        if (infrareds != null && infrareds.size() == 1) {
            infrared = infrareds.get(0);
            Short network = infrared.getNetworkstate();
            if (network == 2) {
                network = 3;
            } else
                network = 1;
            if (infrared != null && infrared.getPlace() != null) {
                List<ParkingPlace> places = parkMapper.getPlaceById(infrared.getPlace());
                ParkingPlace place = null;
                if (places != null && places.size() > 0) {
                    place = places.get(0);
                    List<Infrared> devices_place = tagMapper.findInfraredId(place.getId(), null,null);
                    int final_state = state;

                    for (Infrared device : devices_place
                    ) {
                        System.out.println("devices num"+device.getNum());
                        System.out.println("devices status"+device.getStatus());
                        if (!device.getNum().equals(parkid + "")) {
                            if (device.getStatus() == 1) {
                                final_state = 1;
                                System.out.println("devices num"+final_state);
                            }
                        }
                    }
                    System.out.println("update place name"+place.getName());
                    System.out.println("update place state"+final_state);
                    place.setState((short) final_state);
                    parkMapper.updatePlace(place);
                }

            }
            if (!(infrared.getPower() == (power) && infrared.getStatus() == (state) && infrared.getNetworkstate() == 1)/*&&(timestamp>=infrared.getCount()||(infrared.getCount()-timestamp)>120)*/) {
                infrared.setPower((short) power);
                infrared.setStatus((short) state);
                infrared.setNetworkstate(network);
                infrared.setBatteryTime(new Date());
                infrared.setCount(0);
                tagMapper.updateInfrared(infrared);

                // infrared.setNetworkName("在线");
                //   logger.info("#状态变化发送#"+bsid+"#车位检测器状态ID#"+parkid+"#网关messageid#"+msg_id+"#车位占用状态#"+state+"#时间戳#"+timestamp+"#电量#"+power+"#信标强度#"+rssi);
                // infrared.setNetworkName("在线");
                //  kafukaSender.send(KafukaTopics.INFRARED_STATE,infrared.toString());


            }
        }

    }


}
