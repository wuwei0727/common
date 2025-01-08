package com.tgy.rtls.web.controller.location;

import com.alibaba.fastjson.JSONArray;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.location.DebugRecord;
import com.tgy.rtls.data.entity.location.Originaldata;
import com.tgy.rtls.data.entity.location.Trailrecord;
import com.tgy.rtls.data.entity.map.AreaVO;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.service.common.RecordService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.location.LocationService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.message.WarnRecordService;
import com.tgy.rtls.data.service.user.PersonService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.location
 * @date 2020/10/22
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/location")
public class LocationController {
    @Autowired
    private LocationService locationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private WarnRecordService warnRecordService;
    @Autowired
    private PersonService personService;
    @Autowired
    private SubService subService;
    @Autowired
    private RecordService recordService;
    @Autowired
    private Map2dService map2dService;

    @RequestMapping(value = "/addOriginaldata")
    @ApiOperation(value = "原始数据新增接口", notes = "标签信息")
    public CommonResult<Integer> addOriginaldata(Originaldata originaldata) {
        try {
            if (locationService.addOriginaldata(originaldata)) {
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<Integer>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    /*
     * 轨迹回放
     * */
    @RequestMapping(value = "/getTrailRecordSel")
    @ApiOperation(value = "轨迹回放接口", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "map", value = "地图id", required = true, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "personid", value = "人员id", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "num", value = "人员工号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "name", value = "人员名称", required = false, dataType = "String")
    })
    public CommonResult<Object> getTrailRecordSel(Integer personid, String name,String num, String startTime, String endTime, Integer map) {
        try {
            List<Trailrecord> trailrecords = locationService.findByTrail(personid, name,num, startTime, endTime, map);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), trailrecords);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 轨迹回放
     * */
    @RequestMapping(value = "/getDebugData")
    @ApiOperation(value = "轨迹回放接口", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "id", value = "标签id", required = true, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "map", value = "mapid", required = false, dataType = "int")
    })
    public CommonResult<Object> getTrailRecordSel(Integer id, String startTime, String endTime, Integer map) {
        try {
            List<DebugRecord> trailrecords = locationService.findByTrailWithDebugData(id, startTime, endTime, map);
            for (DebugRecord record:trailrecords
                 ) {
               record.debugData=(JSONArray.parseArray(record.getDebugdata()));
                record.setDebugdata(null);

            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), trailrecords);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    /*
     * 切换地图时传输 人数统计信息  地图区域信息  定位数据信息 报警记录信息
     * */
    @RequestMapping(value = "/getMapMessageSel/{id}")
    @ApiOperation(value = "切换地图获取信息", notes = "")
    @ApiImplicitParam(paramType = "path", name = "id", value = "2维地图id", required = true, dataType = "int")
    public CommonResult<Object> getMapMessageSel(@PathVariable("id") Integer id) {
        try {
            Map<String,Object> result=new HashMap<>();
            /*
             * 定位数据统计
             * */
            Set<String> keys = redisTemplate.keys("tag:*");//定位数据
            JSONArray jsonArrayLocation = new JSONArray();
            for (String key : keys) {
                String value = redisService.get(key);
                JSONObject object = JSONObject.fromObject(value);
                int mapid = object.getInt("map");
                if (mapid == id) {
                    jsonArrayLocation.add(object);
                }
            }
            JSONObject jsonLocation = new JSONObject();
            jsonLocation.put("type", 1);
            jsonLocation.put("data", jsonArrayLocation);
            jsonLocation.put("map", id);
            result.put("location",jsonLocation);
            /*
             * 报警信息统计
             * */
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<WarnRecord> warnRecords = warnRecordService.findByWarn(id);
            JSONArray jsonArrayWarn = new JSONArray();
            for (WarnRecord warnRecord : warnRecords) {
                JSONObject object = new JSONObject();
                object.put("id", warnRecord.getId());//报警信息编号（唯一标识）
                object.put("startTime", dateFormat.format(warnRecord.getStartTime()));
                object.put("warntype", warnRecord.getType());//报警类型
                object.put("typeName", warnRecord.getTypeName());//报警名
                object.put("describe", warnRecord.getDescribe());//内容
                object.put("area", warnRecord.getArea());//区域
                object.put("personid", warnRecord.getPersonid());//人员
                object.put("tagid", warnRecord.getTagid());//标签id
                object.put("warnstate", 0);//报警状态 0报警 1结束
                jsonArrayWarn.add(object);
            }
            JSONObject jsonWarn = new JSONObject();
            jsonWarn.put("type", 2);
            jsonWarn.put("data", jsonArrayWarn);
            jsonWarn.put("map", id);//属于哪个地图的信息
            result.put("warn",jsonWarn);
            /*
             * 人数统计信息
             **/
            JSONArray jsonArrayOverview = new JSONArray();
            //井下人数
            int incoalCount = personService.findByCount(id);
            JSONObject incoal = new JSONObject();
            incoal.put("counttype", 1);
            incoal.put("count", incoalCount);
            //地图超员人数上限
            WarnRule warnRule = warnRecordService.findByType(1, id,null);
            incoal.put("all", warnRule.getRule());
            //离线人数
            int offCount = personService.findByOff(id);
            JSONObject off = new JSONObject();
            off.put("counttype", 2);
            off.put("count", offCount);
            //超时人数
            int overtimeCount = personService.findByOvertime(id);
            JSONObject overtime = new JSONObject();
            overtime.put("counttype", 3);
            overtime.put("count", overtimeCount);
            //地图分站数
            int subConut = subService.findBySubCount(id);
            JSONObject sub = new JSONObject();
            sub.put("counttype", 4);
            sub.put("count", subConut);
            jsonArrayOverview.add(incoal);
            jsonArrayOverview.add(off);
            jsonArrayOverview.add(overtime);
            jsonArrayOverview.add(sub);
            JSONObject jsonOverview = new JSONObject();
            jsonOverview.put("type", 3);//
            jsonOverview.put("data", jsonArrayOverview);
            jsonOverview.put("map", id);
            result.put("overview",jsonOverview);
            /*
             * 地图区域信息集
             * */
            List<AreaVO> areaVOs = recordService.findByArea(id, null, null, null);
            JSONArray jsonArrayArea = new JSONArray();
            for (AreaVO area : areaVOs) {
                JSONObject object = new JSONObject();
                object.put("id", area.getId());//区域自增id 唯一标识
                object.put("name", area.getName());//区域名
                object.put("typeName", area.getTypeName());//类型名
                object.put("count", area.getCount());//区域人数
                if (!NullUtils.isEmpty(area.getMaxnum())) {
                    object.put("all", area.getMaxnum());//人数上限
                } else {
                    object.put("all", 0);//人数上限
                }
                jsonArrayArea.add(object);
            }
            JSONObject jsonArea = new JSONObject();
            jsonArea.put("type", 4);
            jsonArea.put("data", jsonArrayArea);
            jsonArea.put("map", id);
            result.put("area",jsonArea);
            return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
