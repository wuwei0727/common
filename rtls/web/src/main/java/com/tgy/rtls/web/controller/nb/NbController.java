package com.tgy.rtls.web.controller.nb;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.nb_device.Nb_device;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceUseRecord;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.park.ParkingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/nb")
public class NbController {

    @Autowired
    TagService tagService;
    @Autowired(required = false)
    TagMapper tagMapper;

    @Autowired
    ParkingService parkingService;
    @Autowired(required = false)
    BookMapper bookMapper;

    private double batter=0.15;//标签电压变化临界值



    @RequestMapping(value = "/getNbDevice")
    @ApiOperation(value = "查找地磁检测设备",notes = "111")
    public CommonResult<Object> getPlaceUseRatio(String  mac,String license,Integer map, Integer pageIndex,Integer pageSize) {
        try {
            if(license!=null){
                license=license.toUpperCase();
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if(pageSize!=-1)
                PageHelper.startPage(pageIndex,pageSize);
            List list= tagMapper.findByNbMacName(mac,license,map);
            PageInfo<Object> pageInfo=new PageInfo<>(list);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            if(pageSize!=null&&pageSize!=-1)
                res.setData(result);

            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
    @RequestMapping(value = "/getNbById/{id}")
    @ApiOperation(value = "查找地磁检测设备",notes = "111")
    public CommonResult<Object> getPlaceUseRatio(@PathVariable("id")Integer id) {
        try {

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));

            Nb_device data= (Nb_device) tagMapper.findByNbId(id);
                res.setData(data);

            return res;

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

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


