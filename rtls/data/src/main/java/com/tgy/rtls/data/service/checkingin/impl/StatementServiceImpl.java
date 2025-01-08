package com.tgy.rtls.data.service.checkingin.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.common.TimeUtil;
import com.tgy.rtls.data.config.SheetStyle;
import com.tgy.rtls.data.entity.checkingin.Attendancerule;
import com.tgy.rtls.data.entity.checkingin.Statement;
import com.tgy.rtls.data.entity.checkingin.StatementVO;
import com.tgy.rtls.data.mapper.checkingin.StatementMapper;
import com.tgy.rtls.data.service.checkingin.StatementService;
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
 * @date 2020/11/16
 */
@Service
@Transactional
public class StatementServiceImpl implements StatementService {
    @Autowired(required = false)
    private StatementMapper statementMapper;
    @Autowired
    private LocalUtil localUtil;

    @Override
    public List<Attendancerule> findByRule(Integer instanceid) {
        return statementMapper.findByRule(instanceid,localUtil.getLocale());
    }

    @Override
    public boolean updateRule(List<Attendancerule> rules) {
        for(Attendancerule rule:rules) {
            statementMapper.updateRule(rule);
        }
        return true;
    }

    @Override
    public List<StatementVO> findByAll(Integer instanceid,String month, Integer departmentid, Integer worktypeid, Integer jobid, Integer classid) {
        List<StatementVO> statementVOList=statementMapper.findByAll(instanceid,departmentid,worktypeid,jobid,classid);
        for (StatementVO statementVO:statementVOList){
            int addendanceday=0;//出勤天数
            int belateday=0;//迟到天数
            int leaveday=0;//早退天数
            int absenteeismday=0;//旷工天数
            int vacationday=0;//休假时间
            List<Statement> statements=statementMapper.findByStatement(month,statementVO.getId(),localUtil.getLocale());
            for (Statement statement:statements) {
                statement.setDuration(TimeUtil.StatementDuration(statement.getInTime(),statement.getOutTime()));
                int status=StatementStatus(statement,instanceid);
                statement.setStatus(status);
                switch (status){
                    case 1:
                        addendanceday++;
                        break;
                    case 2:
                        break;
                    case 3:
                        belateday++;
                        break;
                    case 4:
                        leaveday++;
                        break;
                    case 5:
                        absenteeismday++;
                        break;
                    case 6:
                        belateday++;
                        leaveday++;
                        break;
                }
            }
            statementVO.setStatements(statements);
            statementVO.setAddendanceday(addendanceday);
            statementVO.setBelateday(belateday);
            statementVO.setLeaveday(leaveday);
            statementVO.setAbsenteeismday(absenteeismday);
            statementVO.setVacationday(vacationday);
            statementVO.setMonth(month);
        }
        return statementVOList;
    }

    @Override
    public void exportStatementVO(ServletOutputStream out, Integer instanceid, String month, Integer departmentid, Integer worktypeid, Integer jobid, Integer classid, String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            /*
             * 表格样式
             * */
            //1.字体--标题
            CellStyle style=SheetStyle.getStyle(workbook);
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
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(date1);
                int num1 = calendar1.get(Calendar.DAY_OF_WEEK) - 1;
                if (num1< 0)
                    num1 = 0;
                   // if (day.equals("六")||day.equals("日")){
                if (num1==0 || num1==6){
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
            HSSFCell row1cell2=hssfRow1.createCell(maxday+2);
            row1cell2.setCellValue(LocalUtil.get(KafukaTopics.ATTENDENCE));
            row1cell2.setCellStyle(style1);
            HSSFCell row1cell3=hssfRow1.createCell(maxday+3);
            row1cell3.setCellValue(LocalUtil.get(KafukaTopics.LATE));
            row1cell3.setCellStyle(style1);
            HSSFCell row1cell4=hssfRow1.createCell(maxday+4);
            row1cell4.setCellValue(LocalUtil.get(KafukaTopics.EARLY_LEAVE));
            row1cell4.setCellStyle(style1);
            HSSFCell row1cell5=hssfRow1.createCell(maxday+5);
            row1cell5.setCellValue(LocalUtil.get(KafukaTopics.ABSENT));
            row1cell5.setCellStyle(style1);
            HSSFCell row1cell6=hssfRow1.createCell(maxday+6);
            row1cell6.setCellValue(LocalUtil.get(KafukaTopics.HOLIDAY));
            row1cell6.setCellStyle(style1);
            for (int i=2;i<7;i++){
                HSSFCell row2cell=hssfRow2.createCell(maxday+i);
                row2cell.setCellStyle(style1);
                row2cell.setCellValue(LocalUtil.get(KafukaTopics.DAYS));
            }
            HSSFRow hssfRow;
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            hssfSheet.setColumnWidth(0, 0);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(maxday+2, 1500);
            hssfSheet.setColumnWidth(maxday+3, 1500);
            hssfSheet.setColumnWidth(maxday+4, 1500);
            hssfSheet.setColumnWidth(maxday+5, 1500);
            hssfSheet.setColumnWidth(maxday+6, 1500);
            //写入实体数据
            List<StatementVO> statementVOList=findByAll(instanceid,month,departmentid,worktypeid,jobid,classid);
            if (!NullUtils.isEmpty(statementVOList)){
                for (int i=0;i<statementVOList.size();i++){
                    int rownum=i+3;
                    hssfRow=hssfSheet.createRow(rownum);
                    hssfRow.setRowStyle(style1);
                    StatementVO statementVO=statementVOList.get(i);
                    //部门
                 /*   HSSFCell cell1=hssfRow.createCell(0);
                    cell1.setCellStyle(style1);
                    cell1.setCellValue(statementVO.getDepartmentName());*/
                    //姓名
                    HSSFCell cell2=hssfRow.createCell(1);
                    cell2.setCellStyle(style1);
                    cell2.setCellValue(statementVO.getName());
                    if (!NullUtils.isEmpty(statementVO.getStatements())){
                        for (Statement statement:statementVO.getStatements()){
                            HSSFCell cell3=hssfRow.createCell(statement.getDay() + 1);
                            cell3.setCellStyle(style1);
                            //1出勤 2休假 3迟到 4早退 5旷工 6迟到+早退
                            if (statement.getStatus()==1) {
                                cell3.setCellValue("√");
                            }else if(statement.getStatus()==2){
                                cell3.setCellValue("×");
                            }else if(statement.getStatus()==3){
                                cell3.setCellValue("△");
                            }else if(statement.getStatus()==4){
                                cell3.setCellValue("▲");
                            }else if(statement.getStatus()==5){
                                cell3.setCellValue("□");
                            }else if(statement.getStatus()==6){
                                cell3.setCellValue("△▲");
                            }
                        }
                    }
                    //周末填充数据
                    for(Integer num:weekRow){
                        HSSFCell cell=hssfRow.createCell(num);
                        cell.setCellStyle(style2);
                        cell.setCellValue("-");
                    }
                    HSSFCell cell4=hssfRow.createCell(maxday+2);
                    cell4.setCellStyle(style1);
                    cell4.setCellValue(statementVO.getAddendanceday());

                    HSSFCell cell5=hssfRow.createCell(maxday+3);
                    cell5.setCellStyle(style1);
                    cell5.setCellValue(statementVO.getVacationday());

                    HSSFCell cell6=hssfRow.createCell(maxday+4);
                    cell6.setCellStyle(style1);
                    cell6.setCellValue(statementVO.getBelateday());

                    HSSFCell cell7=hssfRow.createCell(maxday+5);
                    cell7.setCellStyle(style1);
                    cell7.setCellValue(statementVO.getAbsenteeismday());

                    HSSFCell cell8=hssfRow.createCell(maxday+6);
                    cell8.setCellStyle(style1);
                    cell8.setCellValue(statementVO.getLeaveday());
                }
            }
            HSSFRow hssfRowend = hssfSheet.createRow(3+statementVOList.size());
            hssfRowend.setRowStyle(style1);
            //hssfRowend.createCell(0).setCellValue("出勤：√        休假：×       迟到：△         早退：▲          旷工：□");
           String describe= LocalUtil.get(KafukaTopics.ATTENDENCE)+ "：√       " +
                    LocalUtil.get(KafukaTopics.HOLIDAY)+ "：×       " +
                    LocalUtil.get(KafukaTopics.LATE)+ "：△       " +
                    LocalUtil.get(KafukaTopics.EARLY_LEAVE)+ "：▲       " +
                    LocalUtil.get(KafukaTopics.ABSENT)+ "：□";
            hssfRowend.createCell(0).setCellValue(describe);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,maxday+6);
            hssfSheet.addMergedRegion(region);
            SheetStyle.setRegionStyle(hssfSheet,region,style);

            CellRangeAddress regionend=new CellRangeAddress(3+statementVOList.size(),3+statementVOList.size(),0,maxday+6);
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

    /*
    * 计算人员考勤状态 1出勤 2休假 3迟到 4早退 5旷工 6迟到+早退
    * */
    private int StatementStatus(Statement statement,Integer instanceid){
        try {
            int status=1;//默认正常
            //1.计算上班和下班与实际上下班时间的差距
            int intime=TimeUtil.StatementStaus(statement.getStartTime(),statement.getInTime());
            int outtime=TimeUtil.StatementStaus(statement.getOutTime(),statement.getEndTime());
            //2.获取该实例的考勤规则
            //2.1旷工规则
            Attendancerule absenteeismrule=statementMapper.findByRuleId(instanceid,3);
            if (!NullUtils.isEmpty(absenteeismrule) ) {
                String[] absenteeism = absenteeismrule.getRule().split(",");
                if (intime > Integer.parseInt(absenteeism[0]) || outtime > Integer.parseInt(absenteeism[1])) {//旷工
                    status = 5;
                    return status;
                }
            }
            //2.1迟到规则
            Attendancerule belaterule=statementMapper.findByRuleId(instanceid,1);
            if (!NullUtils.isEmpty(belaterule)){
                String[] belate=belaterule.getRule().split(",");
                if (/*intime>Integer.parseInt(belate[0]) &&*/ intime>Integer.parseInt(belate[1])) {//迟到
                    status = 3;
                }
            }
            //2.1早退规则
            Attendancerule leaverule=statementMapper.findByRuleId(instanceid,2);
            if (!NullUtils.isEmpty(leaverule)) {
                String[] leave = leaverule.getRule().split(",");
                if (/*outtime > Integer.parseInt(leave[0]) && */outtime > Integer.parseInt(leave[1])) {//早退
                    if (status == 3) {
                        status = 6;//迟到+早退
                    } else {
                        status = 4;//早退
                    }
                }
            }
            return status;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }


}
