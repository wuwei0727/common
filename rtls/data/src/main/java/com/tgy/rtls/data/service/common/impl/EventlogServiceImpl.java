package com.tgy.rtls.data.service.common.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.Eventlog;
import com.tgy.rtls.data.entity.common.EventlogType;
import com.tgy.rtls.data.mapper.common.EventlogMapper;
import com.tgy.rtls.data.service.common.EventlogService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common.impl
 * @date 2020/11/9
 */
@Service
public class EventlogServiceImpl implements EventlogService {
    @Autowired(required = false)
    private EventlogMapper eventlogMapper;
    @Autowired
    private LocalUtil localUtil;

    @Override
    public List<Eventlog> findByAll(Integer instanceid, Integer map, Integer type, Integer typeSimple,String startTime, String endTime,Integer departmentId,String personName) {
        return eventlogMapper.findByAll(instanceid,map,type,typeSimple,startTime,endTime, departmentId, personName,localUtil.getLocale());
    }

    @Override
    public List<EventlogType> findByType(Integer instanceid) {
        return eventlogMapper.findByType(instanceid);
    }

    @Override
    public List<EventlogType> findByTypeSimple() {
        return eventlogMapper.findByTypeSimple(localUtil.getLocale());
    }

    @Override
    public void exportEventlog(ServletOutputStream out, Integer instanceid, Integer map, Integer type,Integer typeSimple, String startTime, String endTime,String title,Integer departmentId,String personName) throws Exception {
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
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
         //   String[] titles = {"序号", "时间","部门名称", "人员","标签","事件", "关联地图"};
            String[] titles = new String[10];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.TIME);
            titles[2]= LocalUtil.get(KafukaTopics.DEPARTMENT);
            titles[3]= LocalUtil.get(KafukaTopics.NAME);
            titles[4]= LocalUtil.get(KafukaTopics.CARDNUM);
            titles[5]= LocalUtil.get(KafukaTopics.EVENT);
            titles[6]= LocalUtil.get(KafukaTopics.RELATION_MAP);

            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 5000);
            hssfSheet.setColumnWidth(2, 3500);
            hssfSheet.setColumnWidth(3, 3500);
            hssfSheet.setColumnWidth(4, 8000);
            hssfSheet.setColumnWidth(5, 3500);
            hssfSheet.setColumnWidth(6, 8000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<Eventlog> eventlogs=eventlogMapper.findByAll(instanceid,map,type,typeSimple,startTime,endTime, departmentId, personName,localUtil.getLocale());
            if (!NullUtils.isEmpty(eventlogs)){
                for (int i=0;i<eventlogs.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    Eventlog eventlog=eventlogs.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(dateFormat.format(eventlog.getTime()));
                    hssfRow.createCell(2).setCellValue(eventlog.getDepartmentName());
                    hssfRow.createCell(3).setCellValue(eventlog.getPersonName());
                    hssfRow.createCell(4).setCellValue(eventlog.getTagName());
                    hssfRow.createCell(5).setCellValue(eventlog.getEvent());
                    hssfRow.createCell(6).setCellValue(eventlog.getMapName());

                }
            }
            /*
            * 合并单元格
            * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,6);
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
