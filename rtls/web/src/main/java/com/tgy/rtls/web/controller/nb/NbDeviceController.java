package com.tgy.rtls.web.controller.nb;

import com.tgy.rtls.data.entity.nb_device.BusinessCarports;
import com.tgy.rtls.data.entity.nb_device.Nb_device;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceUseRecord;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.park.ParkingService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/park/update/status")
public class NbDeviceController {

    @Autowired
    TagService tagService;
    @Autowired(required = false)
    TagMapper tagMapper;

    @Autowired
    ParkingService parkingService;
    @Autowired(required = false)
    BookMapper bookMapper;

    private double batter=0.15;//标签电压变化临界值
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




    void processNbDevice(Nb_device nb){
        String mac=nb.getMac();
        nb.setUploadtime(nb.getUploadtime()/1000);
        List list= tagService.findByNbMac(mac,null);
        if(list==null||list.size()==0){
            tagService.addNb(nb);
        }else {
            //如果电压变化超过了临界值就修改
            Nb_device nb_device=(Nb_device)list.get(0);
            List<ParkingPlace> places = parkingService.findByAllPlace(nb_device.getPlace(), null, null, null, null, null, null, null, null, null, null, null, null, null,null);

            boolean result=Math.abs((Integer.valueOf(nb.getVoltage())-Integer.valueOf(nb_device.getVoltage()))/100d)>=batter?true:false;
            boolean result1=nb_device.getStatus().shortValue()!=nb.getStatus().shortValue()?true:false;
            boolean result2=nb_device.getBerthcode().equals(nb.getBerthcode())?false:true;
            if(nb_device.getPlace()!=null&&places!=null&&places.size()!=0){
                ParkingPlace place = places.get(0);
                if(place.getState().shortValue()!=nb.getStatus().shortValue()){
                    place.setState(nb.getStatus().shortValue());
                    parkingService.updatePlace(place);
                    PlaceUseRecord record = bookMapper.selectPlaceUseRecordByPlaceid(place.getId());
                    if(nb.getStatus().shortValue()==0){
                        if(record!=null){
                            record.setEnd(new Timestamp(new Date().getTime()).toString());
                            bookMapper.UpdatePlaceUseRecordByid(record);
                        }
                    }else {
                        if(record==null){
                            record=new PlaceUseRecord();
                            record.setLicense(place.getLicense());
                            record.setMap(place.getMap());
                            record.setPlace(place.getId());
                            record.setStart(new Timestamp(new Date().getTime()).toString());
                            bookMapper.addPlaceUseRecord(record);
                        }

                    }

                }
            }

            if (result||result1||result2){
                nb.setId(nb_device.getId());
                tagService.updateNb(nb);
            }
        }
    }

}


