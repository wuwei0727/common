package com.tgy.rtls.data.service.message.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.message.WarnMap;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.entity.type.Status;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.mapper.message.WarnRuleMapper;
import com.tgy.rtls.data.mapper.type.StatusMapper;
import com.tgy.rtls.data.service.message.WarnRecordService;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.message.impl
 * @date 2020/10/30
 */
@Service
@Transactional
public class WarnRecordServiceImpl implements WarnRecordService {
    @Autowired
    private WarnRecordMapper warnRecordMapper;
    @Autowired
    private WarnRuleMapper warnRuleMapper;
    @Autowired
    private StatusMapper statusMapper;
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private LocalUtil localUtil;

    @Override
    public List<WarnRecord> findByRecordAll(String instanceid,Integer map,String startTime,String endTime,Integer type,Integer warnstate,Integer areaType,String areaName){
        return warnRecordMapper.findByAll(instanceid,map,startTime,endTime,type,warnstate,areaType,areaName,localUtil.getLocale(),LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<Status> findByWarnType() {
        return statusMapper.findByAll("warn",localUtil.getLocale());
    }

    @Override
    public Boolean addWarnRecord(WarnRecord warn) {
        return warnRecordMapper.addWarnRecord(warn)>0;
    }

    @Override
    public List<WarnRecord> findByWarn(Integer map) {
        return warnRecordMapper.findByWarn(map,localUtil.getLocale());
    }

    @Override
    public WarnRecord findByRecordId(Integer id) {
        return warnRecordMapper.findById(id,localUtil.getLocale());
    }

    @Override
    public List<WarnRule> findByRuleAll(Integer instanceid,Integer type,Integer map, Integer enable) {
        return warnRuleMapper.findByAll(instanceid,type,map,enable,localUtil.getLocale());
    }

    @Override
    public List<WarnMap> findByMap(Integer map) {
        return warnRuleMapper.findByMap(map);
    }

    @Override
    public WarnRule findByType(Integer type, Integer map,Integer enable) {
        return warnRuleMapper.findByType(type,map,enable);
    }

    @Override
    public Boolean updateWarnRule(List<WarnRule> rules) {
        for (WarnRule rule:rules){
            warnRuleMapper.updateWarnRule(rule);
            WarnRule warnRule=warnRuleMapper.findByWarnRuleId(rule.getId());
            JSONObject object=new JSONObject();
            object.put("map",warnRule.getMap());
            object.put("type",warnRule.getType());
            kafkaTemplate.send("warnRule",object.toString());
        }
        return true;
    }

    @Override
    public WarnRecord findByType(Integer map, Integer area, Integer personid, Integer type) {
        return warnRecordMapper.findByType(map,area,personid,type);
    }

    @Override
    public boolean updateWarnRecord(String endTime, Integer id) {
        return warnRecordMapper.updateWarnRecord(endTime,id)>0;
    }

    @Override
    public void exportWarnRecord(ServletOutputStream out, String instanceid, Integer map, String startTime, String endTime,Integer type,String title, Integer areaType,String areaName,Integer warnstate) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            //居中样式
    //        String[] titles = {"序号", "报警类型", "报警原因", ,"区域类型","区域名称"，"开始时间", "结束时间", "持续时间", "是否结束", "关联地图"};
            String[] titles = new String[10];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.WARNING_TYPE);
            titles[2]= LocalUtil.get(KafukaTopics.WARNING_REASON);
            titles[3]= LocalUtil.get(KafukaTopics.AREA_TYPE);
            titles[4]= LocalUtil.get(KafukaTopics.AREA_NAME);
            titles[5]= LocalUtil.get(KafukaTopics.STARTTIME);
            titles[6]= LocalUtil.get(KafukaTopics.ENDTIME);
            titles[7]= LocalUtil.get(KafukaTopics.DURATION);
            titles[8]= LocalUtil.get(KafukaTopics.ISEND);
            titles[9]= LocalUtil.get(KafukaTopics.RELATION_MAP);


            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 3500);
            hssfSheet.setColumnWidth(2, 8000);
            hssfSheet.setColumnWidth(3, 3500);
            hssfSheet.setColumnWidth(4, 3500);
            hssfSheet.setColumnWidth(5, 3500);
            hssfSheet.setColumnWidth(6, 3500);
            hssfSheet.setColumnWidth(7, 3500);
            hssfSheet.setColumnWidth(8, 3500);
            hssfSheet.setColumnWidth(9, 3500);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<WarnRecord> warnRecords=warnRecordMapper.findByAll(instanceid,map,startTime,endTime,type,warnstate,areaType,areaName,localUtil.getLocale(),LocalUtil.get(KafukaTopics.TIMEFORMAT));
            if (!NullUtils.isEmpty(warnRecords)){
                for (int i=0;i<warnRecords.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    WarnRecord warnRecord=warnRecords.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(warnRecord.getTypeName());
                    hssfRow.createCell(2).setCellValue(warnRecord.getDescribe());
                    if (!NullUtils.isEmpty(warnRecord.getAreaType())) {
                        hssfRow.createCell(3).setCellValue(warnRecord.getAreaType());
                    }else {
                        hssfRow.createCell(3).setCellValue("");
                    }
                    if (!NullUtils.isEmpty(warnRecord.getAreaName())) {
                        hssfRow.createCell(4).setCellValue(warnRecord.getAreaName());
                    }else {
                        hssfRow.createCell(4).setCellValue("");
                    }
                    hssfRow.createCell(5).setCellValue(dateFormat.format(warnRecord.getStartTime()));
                    if (!NullUtils.isEmpty(warnRecord.getEndTime())) {
                        hssfRow.createCell(6).setCellValue(dateFormat.format(warnRecord.getEndTime()));
                    }else {
                        hssfRow.createCell(6).setCellValue("");
                    }
                    if (!NullUtils.isEmpty(warnRecord.getDescribe())) {
                        hssfRow.createCell(7).setCellValue(warnRecord.getDuration());
                    }else {
                        hssfRow.createCell(7).setCellValue("");
                    }
                    if (warnRecord.getWarnstate()==0) {
                        hssfRow.createCell(8).setCellValue("报警中");
                    }else {
                        hssfRow.createCell(8).setCellValue("报警结束");
                    }
                    hssfRow.createCell(9).setCellValue(warnRecord.getMapName());

                }
            }
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,9);
            hssfSheet.addMergedRegion(region);
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }
}
