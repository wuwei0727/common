package com.tgy.rtls.data.service.checkingin.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SheetStyle;
import com.tgy.rtls.data.entity.checkingin.*;
import com.tgy.rtls.data.mapper.checkingin.WorkorderMapper;
import com.tgy.rtls.data.service.checkingin.WorkorderService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.checkingin.impl
 * @date 2020/11/13
 */
@Service
@Transactional
public class WorkorderServiceImpl implements WorkorderService {
    @Autowired(required = false)
    private WorkorderMapper workorderMapper;
    @Autowired
    private LocalUtil localUtil;
    @Override
    public List<WorkorderVO> findByAll(Integer instanceid) {
        return workorderMapper.findByAll(instanceid,localUtil.getLocale());
    }

    @Override
    public WorkorderVO findById(Integer id) {
        return workorderMapper.findById(id,localUtil.getLocale());
    }

    @Override
    public Workorder findByNum(String num) {
        return workorderMapper.findByNum(num);
    }


    @Override
    public boolean addWorkorder(WorkorderVO workorderVO) {
        if (workorderMapper.addWorkorder(workorderVO)>0){
            if (workorderVO.getWorksystemList()!=null){
                for (Worksystem worksystem:workorderVO.getWorksystemList()){
                    worksystem.setWoid(workorderVO.getId());
                    workorderMapper.addWorksystem(worksystem);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateWorkorder(WorkorderVO workorderVO) {
        workorderMapper.updateWorkorder(workorderVO);
        if (workorderVO.getWorksystemList()!=null){
            for (Worksystem worksystem:workorderVO.getWorksystemList()){
                workorderMapper.updateWorksystem(worksystem);
            }
        }
        return true;
    }

    @Override
    public boolean delWorkorder(String ids){
        String[] split=ids.split(",");
        workorderMapper.delWorksystem(split);
        workorderMapper.delWorkorder(split);
        return true;
    }

    @Override
    public boolean delWorkorderByInstance(Integer instanceid) {
        return  workorderMapper.delWorkorderByInstance( instanceid);
    }

    @Override
    public boolean addScheduling(Scheduling scheduling,String personids){
        if (!NullUtils.isEmpty(personids)) {
            String[] split = personids.split(",");
            for (String s : split) {
                scheduling.setPersonid(Integer.valueOf(s));
                workorderMapper.addScheduling(scheduling);
            }
        }
        return true;
    }

    @Override
    public boolean updateScheduling(Scheduling scheduling,String personids) {
        //先删除
        workorderMapper.delScheduling(scheduling);
        //再添加
        return addScheduling(scheduling,personids);
    }

    @Override
    public boolean delSchedulingWoid(Integer woid, String month) {
        return workorderMapper.delSchedulingWoid(woid,month)>0;
    }

    @Override
    public List<SchedulingVO> findByScheduling(String month,Integer instanceid) {
        return workorderMapper.findByScheduling(month,instanceid,localUtil.getLocale());
    }

    @Override
    public List<WorkorderVO> findBySchedulingWorkorder(String month,Integer instanceid) {
        return workorderMapper.findBySchedulingWorkorder(month,instanceid,localUtil.getLocale());
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
            row1Cell1.setCellValue(LocalUtil.get(KafukaTopics.NAME)+"  "+LocalUtil.get(KafukaTopics.DATE));
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
            //存储周末的列数
            List<Integer> weekRow=new ArrayList<>();
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
                    row2Cellnum.setCellStyle(style2);
                    weekRow.add(num);
                }else {
                    row1Cellnum.setCellStyle(style1);
                    row2Cellnum.setCellStyle(style1);
                }
                row2Cellnum.setCellValue(day);
                row1Cellnum.setCellValue(sdf1.format(date1.getTime()).substring(8));
                hssfSheet.setColumnWidth(num, 1000);
            }
            HSSFRow hssfRow;
            hssfSheet.setColumnWidth(0, 0);
            hssfSheet.setColumnWidth(1, 2000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<PersonSchedulingVO> personSchedulingVOS=workorderMapper.findBySchedulingPerson(month,instanceid);
            for (int i=0;i<personSchedulingVOS.size();i++){
                int rownum=i+3;
                hssfRow=hssfSheet.createRow(rownum);
                hssfRow.setRowStyle(style1);
                PersonSchedulingVO personSchedulingVO=personSchedulingVOS.get(i);
                //填充人员所在部门和人员名
                //部门
                HSSFCell cell1=hssfRow.createCell(0);
                cell1.setCellStyle(style1);
                cell1.setCellValue(personSchedulingVO.getDepartmentName());
                //姓名
                HSSFCell cell2=hssfRow.createCell(1);
                cell2.setCellStyle(style1);
                cell2.setCellValue(personSchedulingVO.getName());
                //周末填充数据
                for(Integer num:weekRow){
                    HSSFCell cell=hssfRow.createCell(num);
                    cell.setCellStyle(style2);
                    cell.setCellValue("-");
                }
                if (!NullUtils.isEmpty(personSchedulingVO.getSchedulings())) {
                    for (Scheduling scheduling:personSchedulingVO.getSchedulings()){
                        //获取排班样式
                        Workorder workorder=workorderMapper.findByWorkorderId(scheduling.getWoid());
                        //分三八制和四六制
                        String type="";
                        if (workorder.getSystem().equals("0")){//三八制
                            if (scheduling.getType()==1){
                                type=LocalUtil.get(KafukaTopics.MORNING);
                            }else if (scheduling.getType()==2){
                                type=LocalUtil.get(KafukaTopics.NOON);
                            }else {
                                type=LocalUtil.get(KafukaTopics.NIGHT);;
                            }
                        }else {//四六制
                            if (scheduling.getType()==1){
                                type=LocalUtil.get(KafukaTopics.ONE);;
                            }else if (scheduling.getType()==2){
                                type=LocalUtil.get(KafukaTopics.TWO);;
                            }else if (scheduling.getType()==3){
                                type=LocalUtil.get(KafukaTopics.THREE);;
                            }else {
                                type=LocalUtil.get(KafukaTopics.FOUR);;
                            }
                        }
                        CellStyle styleColor=SheetStyle.getStyleColor(workbook,workorder.getStyle());
                        HSSFCell celltype=hssfRow.createCell(scheduling.getDay()+1);
                        celltype.setCellStyle(styleColor);
                        celltype.setCellValue(type);
                    }
                }
            }
            //该月使用的样式制度信息
            List<WorksystemVO> worksystemVOs=workorderMapper.findByWorksystemVO(month,instanceid);
            for (int i=0;i<worksystemVOs.size();i++){
                int rownum=i+personSchedulingVOS.size()+3;
                hssfRow=hssfSheet.createRow(rownum);
                hssfRow.setRowStyle(style1);
                WorksystemVO worksystem=worksystemVOs.get(i);
                //分三八制和四六制
                String type="";
                if (worksystem.getSystem().equals("0")){//三八制
                    if (worksystem.getType()==1){
                        type=LocalUtil.get(KafukaTopics.MORNING);
                    }else if (worksystem.getType()==2){
                        type=LocalUtil.get(KafukaTopics.NOON);
                    }else {
                        type=LocalUtil.get(KafukaTopics.NIGHT);
                    }
                }else {//四六制
                    if (worksystem.getType()==1){
                        type=LocalUtil.get(KafukaTopics.ONE);;
                    }else if (worksystem.getType()==2){
                        type=LocalUtil.get(KafukaTopics.TWO);;
                    }else if (worksystem.getType()==3){
                        type=LocalUtil.get(KafukaTopics.THREE);;
                    }else {
                        type=LocalUtil.get(KafukaTopics.FOUR);;
                    }
                }
                if (rownum==(personSchedulingVOS.size()+3)){
                    HSSFCell cell=hssfRow.createCell(0);
                    cell.setCellValue(LocalUtil.get(KafukaTopics.CHART));
                    cell.setCellStyle(style1);
                }
                //制度
                HSSFCell cell1=hssfRow.createCell(2);
                CellStyle styleColor=SheetStyle.getStyleColor(workbook,worksystem.getStyle());
                cell1.setCellStyle(styleColor);
                cell1.setCellValue(type);
                //时间
                HSSFCell cell2=hssfRow.createCell(3);
                cell2.setCellStyle(style1);
                String oneday=LocalUtil.get(KafukaTopics.TODAY);
                Integer start= Integer.valueOf(worksystem.getStartTime().replace(":",""));
                Integer end= Integer.valueOf(worksystem.getEndTime().replace(":",""));
                if (end<start){
                    oneday=LocalUtil.get(KafukaTopics.TOMORROW);;
                }
                cell2.setCellValue(LocalUtil.get(KafukaTopics.TODAY)+worksystem.getStartTime()+"-"+oneday+worksystem.getEndTime());
                //合并单元格
                CellRangeAddress region=new CellRangeAddress(rownum,rownum,3,maxday+1);
                hssfSheet.addMergedRegion(region);
                SheetStyle.setRegionStyle(hssfSheet,region,style1);
            }
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,maxday+1);
            hssfSheet.addMergedRegion(region);
            SheetStyle.setRegionStyle(hssfSheet,region,style);
            try {
                CellRangeAddress regionend = new CellRangeAddress(personSchedulingVOS.size() + 3, personSchedulingVOS.size() + worksystemVOs.size() + 2, 0, 1);
                hssfSheet.addMergedRegion(regionend);
                SheetStyle.setRegionStyle(hssfSheet, region, style1);
            }catch (Exception e){

            }

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
    public List<PersonSchedulingVO> exportSchedulingSel(String month, Integer instanceid) {
        return workorderMapper.findBySchedulingPerson(month,instanceid);
    }

    @Override
    public List<WorkInf> findWorkInfNameByPersonIdAsso(Integer personid) {
        return workorderMapper.findWorkInfNameByPersonIdAsso(personid,localUtil.getLocale());
    }

    @Override
    public List<WorkInf> findWorkInfByPersonId(Integer personid) {
        return workorderMapper.findWorkInfByPersonId(personid,localUtil.getLocale());
    }
}
