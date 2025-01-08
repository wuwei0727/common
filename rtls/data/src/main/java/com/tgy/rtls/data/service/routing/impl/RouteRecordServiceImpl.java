package com.tgy.rtls.data.service.routing.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SheetStyle;
import com.tgy.rtls.data.entity.routing.*;
import com.tgy.rtls.data.mapper.routing.RouteRecordMapper;
import com.tgy.rtls.data.service.routing.RouteRecordService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.routing.impl
 * @date 2020/11/24
 */
@Service
@Transactional
public class RouteRecordServiceImpl implements RouteRecordService{
    @Autowired(required = false)
    private RouteRecordMapper routeRecordMapper;

    @Override
    public List<Routetask> findByTask(String month,Integer instanceid) {
        return routeRecordMapper.findByTask(month,instanceid);
    }

    @Override
    public List<Route> findByRoute(String month,Integer instanceid) {
        return routeRecordMapper.findByRoute(month,instanceid);
    }

    @Override
    public boolean addRoutetask(Routetask task) {
        return routeRecordMapper.addRoutetask(task)>0;
    }

    @Override
    public boolean updateRoutetask(Routetask task) {
        if(task.getPersonid()==null) {
            routeRecordMapper.delRoutetaskByid(task.getId());
            return true;
        }
        return routeRecordMapper.updateRoutetask(task)>0;
    }

    @Override
    public boolean delRoutetask(Integer id, String month) {
        return routeRecordMapper.delRoutetask(id,month)>0;
    }

    @Override
    public List<RouteData> findByRouteRecord(String month, String name, String keyword,Integer map,Integer instanceid) {
        List<RouteData> routeDatas=routeRecordMapper.findByRouteData(name,map,instanceid);
        for (RouteData routeData:routeDatas) {
            List<RouteVO> routeVOs = routeRecordMapper.findByRouteRecord(month, routeData.getId(), keyword,map);
            for (RouteVO routeVO : routeVOs) {
                int status = 1;
                if (!NullUtils.isEmpty(routeVO.getRoutedotVOs())) {
                    for (RoutedotVO routedotVO : routeVO.getRoutedotVOs()) {
                        if (routedotVO.getStatus() == 0) {
                            status = 0;
                            break;
                        }
                    }
                }
                routeVO.setStatus(status);
            }
            routeData.setRouteVOS(routeVOs);
        }
        return routeDatas;
    }

    @Override
    //@Cacheable(value = "routedotId",key = "#month"+'-'+"#day"+"-"+"#personid")
    public List<Routedot> findByRoutedotId(String month, Integer day, Integer personid) {
        return routeRecordMapper.findByRoutedotId(month,day,personid);
    }

    @Override
    public boolean addRouteRecord(Routerecord record) {
        return routeRecordMapper.addRouteRecord(record)>0;
    }

    @Override
    public Routerecord findByRouteRecordId(String month, Integer day, Integer personid, Integer rdid) {
        return routeRecordMapper.findByRouteRecordId(month,day,personid,rdid);
    }

    @Override
    public void exportRouteRecord(ServletOutputStream out, String month, String name, String keyword, Integer map,Integer instanceid, String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            /*
             * 表格样式
             * */
            //1.字体--标题
            CellStyle style= SheetStyle.getStyle(workbook);
            //2.字体--正文
            CellStyle style1=SheetStyle.getStyle1(workbook);
            //3.字体--正文+颜色
            CellStyle style2=SheetStyle.getStyle2(workbook);
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.setRowStyle(style);
            hssfRowTitle.createCell(0).setCellValue(title);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow1 = hssfSheet.createRow(1);
            HSSFRow hssfRow2 = hssfSheet.createRow(2);
            hssfRow1.setRowStyle(style1);
            hssfRow2.setRowStyle(style1);


            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<RouteData> routeDataList=findByRouteRecord(month,name,keyword,map,instanceid);
           //存储异常路线个数
            Map<Integer,Integer> errorNum=new HashMap<>();
            int j=0;
            for (int i=0;i<routeDataList.size()*2;i=i+2){
                int rownum1=i+3;
                int rownum2=rownum1+1;
                hssfRow1=hssfSheet.createRow(rownum1);
                hssfRow2=hssfSheet.createRow(rownum2);
                hssfRow1.setRowStyle(style1);
                hssfRow2.setRowStyle(style1);
                RouteData routeData=routeDataList.get(j);
                //路线名称
                HSSFCell cell1=hssfRow1.createCell(0);
                cell1.setCellStyle(style1);
                cell1.setCellValue(routeData.getName());
                //状态
                HSSFCell cell2=hssfRow2.createCell(0);
                cell2.setCellValue(LocalUtil.get(KafukaTopics.STATE));
                //合并
                CellRangeAddress region1=new CellRangeAddress(rownum1,rownum1,0,1);
                hssfSheet.addMergedRegion(region1);
                SheetStyle.setRegionStyle(hssfSheet,region1,style1);
                CellRangeAddress region2=new CellRangeAddress(rownum2,rownum2,0,1);
                hssfSheet.addMergedRegion(region2);
                SheetStyle.setRegionStyle(hssfSheet,region2,style1);

                if (!NullUtils.isEmpty(routeData.getRouteVOS())) {
                    for (RouteVO routeVO:routeData.getRouteVOS()){
                        //人员名称
                        HSSFCell cellPersonName=hssfRow1.createCell(routeVO.getDay() + 1);
                        cellPersonName.setCellValue(routeVO.getPerson().getName());
                        cellPersonName.setCellStyle(style1);
                        HSSFCell cellState=hssfRow2.createCell(routeVO.getDay() + 1);
                        cellState.setCellStyle(style1);
                        if (routeVO.getStatus()==1) {
                            cellState.setCellValue("√");
                        }else {
                            cellState.setCellValue("×");
                            //存储异常路线图
                            Integer num=errorNum.get(routeVO.getDay()+1);
                            if (NullUtils.isEmpty(num)) {
                                errorNum.put(routeVO.getDay() + 1, 1);
                            }else {
                                num++;
                                errorNum.put(routeVO.getDay() + 1, num);
                            }
                        }
                    }
                }
                j++;
            }
            /*
             * 获取当前月份的天与星期
             * */
            //获取该月的最大天数
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DATE, 1);
            calendar.set(Calendar.YEAR, Integer.parseInt(month.substring(0,4)));
            calendar.set(Calendar.MONTH, Integer.parseInt(month.substring(4)) - 1);
            int maxday=calendar.getActualMaximum(Calendar.DATE);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd");
            sdf1.setLenient(false);
            SimpleDateFormat sdf2 = new SimpleDateFormat("EEE");
            HSSFRow hssfRowerror = hssfSheet.createRow(3+routeDataList.size()*2);
            hssfRowerror.createCell(0).setCellValue(LocalUtil.get(KafukaTopics.ABNORMAL)+LocalUtil.get(KafukaTopics.ROUTER));
            hssfRowerror.setRowStyle(style1);
            CellRangeAddress regionerror=new CellRangeAddress(3+routeDataList.size()*2,3+routeDataList.size()*2,0,1);
            hssfSheet.addMergedRegion(regionerror);
            SheetStyle.setRegionStyle(hssfSheet,regionerror,style1);

            hssfRow1 = hssfSheet.createRow(1);
            hssfRow2 = hssfSheet.createRow(2);
            HSSFCell row1Cell1=hssfRow1.createCell(0);
            row1Cell1.setCellStyle(style1);
            row1Cell1.setCellValue(LocalUtil.get(KafukaTopics.ROUTER)+"  "+LocalUtil.get(KafukaTopics.DATE));
            for (int i = 1; i < maxday+1; i++) {
                StringBuilder s=new StringBuilder(month);
                Date date1 = sdf1.parse(s.insert(4,"/") + "/" + i);
                int num=i+1;
                HSSFCell Row1cellNum=hssfRow1.createCell(num);
                HSSFCell Row2cellNum=hssfRow2.createCell(num);

                Row2cellNum.setCellValue(sdf2.format(date1).substring(2));
                Row1cellNum.setCellValue(sdf1.format(date1.getTime()).substring(8));
                //星期
                String day=sdf2.format(date1).substring(2);
                if (day.equals("六")||day.equals("日")){
                    Row1cellNum.setCellStyle(style2);
                    Row1cellNum.setCellStyle(style2);
                }else {
                    Row1cellNum.setCellStyle(style1);
                    Row1cellNum.setCellStyle(style1);
                }
                //存路线异常数
                Integer errornum=errorNum.get(num);
                HSSFCell cellError=hssfRowerror.createCell(num);
                cellError.setCellStyle(style1);
                if (NullUtils.isEmpty(errornum)) {
                    cellError.setCellValue(0);
                }else {
                    cellError.setCellValue(errornum);
                }
                hssfSheet.setColumnWidth(num, 2000);
            }

            HSSFRow hssfRowend = hssfSheet.createRow(4+routeDataList.size()*2);
            hssfRowend.createCell(0).setCellValue(LocalUtil.get(KafukaTopics.NORMAL)+"：√    "+LocalUtil.get(KafukaTopics.ABNORMAL)+"：×");
            hssfRowend.setRowStyle(style1);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,maxday+1);
            hssfSheet.addMergedRegion(region);
            SheetStyle.setRegionStyle(hssfSheet,region,style);
            CellRangeAddress regionend=new CellRangeAddress(4+routeDataList.size()*2,4+routeDataList.size()*2,0,maxday+1);
            hssfSheet.addMergedRegion(regionend);
            SheetStyle.setRegionStyle(hssfSheet,regionend,style1);
            CellRangeAddress reginstart=new CellRangeAddress(1,2,0,1);
            hssfSheet.addMergedRegion(reginstart);
            SheetStyle.setRegionStyle(hssfSheet,reginstart,style1);
            /*
             * 画斜线
             * */
            //画线(由左上到右下的斜线)  在A1的第一个cell（单位  分类）加入一条对角线
            HSSFPatriarch patriarch = hssfSheet.createDrawingPatriarch();
            HSSFClientAnchor a = new HSSFClientAnchor(0, 0, 1023, 255, (short) 0, 1, (short) 1, 2);
            HSSFSimpleShape shape1 = patriarch.createSimpleShape(a);
            shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
            shape1.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);
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

    @Override
    public void exportRouteTask(ServletOutputStream out, String month, Integer instanceid, String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            /*
             * 表格样式
             * */
            //1.字体--标题
            CellStyle style= SheetStyle.getStyle(workbook);
            //2.字体--正文
            CellStyle style1=SheetStyle.getStyle1(workbook);
            //3.字体--正文+颜色
            CellStyle style2=SheetStyle.getStyle2(workbook);
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.setRowStyle(style);
            hssfRowTitle.createCell(0).setCellValue(title);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow1 = hssfSheet.createRow(1);
            HSSFRow hssfRow2 = hssfSheet.createRow(2);
            hssfRow1.setRowStyle(style1);
            hssfRow2.setRowStyle(style1);
            HSSFCell row1Cell1=hssfRow1.createCell(0);
            row1Cell1.setCellStyle(style1);
            row1Cell1.setCellValue(LocalUtil.get(KafukaTopics.ROUTER)+"  "+LocalUtil.get(KafukaTopics.DATE));

            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            /*
             * 获取当前月份的天与星期
             * */
            //获取该月的最大天数
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DATE, 1);
            calendar.set(Calendar.YEAR, Integer.parseInt(month.substring(0,4)));
            calendar.set(Calendar.MONTH, Integer.parseInt(month.substring(4)) - 1);
            int maxday=calendar.getActualMaximum(Calendar.DATE);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd");
            sdf1.setLenient(false);
            SimpleDateFormat sdf2 = new SimpleDateFormat("EEE");
            for (int i = 1; i < maxday+1; i++) {
                StringBuilder s=new StringBuilder(month);
                Date date1 = sdf1.parse(s.insert(4,"/") + "/" + i);
                int num=i+1;
                HSSFCell row1Cellnum=hssfRow1.createCell(num);
                HSSFCell row2Cellnum=hssfRow2.createCell(num);
                //星期
                String day=sdf2.format(date1).substring(2);
                if (day.equals("六")||day.equals("日")){
                    row1Cellnum.setCellStyle(style2);
                    row1Cellnum.setCellStyle(style2);
                }else {
                    row1Cellnum.setCellStyle(style1);
                    row1Cellnum.setCellStyle(style1);
                }

                row2Cellnum.setCellValue(day);
                row1Cellnum.setCellValue(sdf1.format(date1.getTime()).substring(8));
                hssfSheet.setColumnWidth(num, 1000);
            }
            //写入实体数据
            HSSFRow hssfRow;
            List<Route> routes=routeRecordMapper.findByRoute(month,instanceid);
            for (int i=0;i<routes.size();i++){
                int rownum=i+3;
                hssfRow=hssfSheet.createRow(rownum);
                hssfRow.setRowStyle(style1);
                Route route=routes.get(i);
                HSSFCell cellName=hssfRow.createCell(0);
                cellName.setCellStyle(style1);
                cellName.setCellValue(route.getName());
                CellRangeAddress region1=new CellRangeAddress(rownum,rownum,0,1);
                hssfSheet.addMergedRegion(region1);
                SheetStyle.setRegionStyle(hssfSheet,region1,style1);
                List<Routetask> routetasks=routeRecordMapper.findByTaskRid(month,route.getId());
                if (!NullUtils.isEmpty(routetasks)){
                    for (Routetask routetask:routetasks){
                        HSSFCell cellPersonName=hssfRow.createCell(routetask.getDay()+1);
                        cellPersonName.setCellStyle(style1);
                        cellPersonName.setCellValue(routetask.getPerson().getName());
                    }
                }
                //路线名称
            }
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,maxday+1);
            hssfSheet.addMergedRegion(region);
            SheetStyle.setRegionStyle(hssfSheet,region,style);
            CellRangeAddress reginstart=new CellRangeAddress(1,2,0,1);
            hssfSheet.addMergedRegion(reginstart);
            SheetStyle.setRegionStyle(hssfSheet,reginstart,style1);
            /*
             * 画斜线
             * */
            //画线(由左上到右下的斜线)  在A1的第一个cell（单位  分类）加入一条对角线
            HSSFPatriarch patriarch = hssfSheet.createDrawingPatriarch();
            HSSFClientAnchor a = new HSSFClientAnchor(0, 0, 1023, 255, (short) 0, 1, (short) 1, 2);
            HSSFSimpleShape shape1 = patriarch.createSimpleShape(a);
            shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
            shape1.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);
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
