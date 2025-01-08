package com.tgy.rtls.data.service.common.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.SubSyn;
import com.tgy.rtls.data.entity.map.AreaVO;
import com.tgy.rtls.data.entity.user.PersonArea;
import com.tgy.rtls.data.entity.user.PersonIncoal;
import com.tgy.rtls.data.entity.user.PersonOff;
import com.tgy.rtls.data.entity.user.PersonSub;
import com.tgy.rtls.data.mapper.common.RecordMapper;
import com.tgy.rtls.data.service.common.RecordService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common.impl
 * @date 2020/10/29
 */
@Service
@Transactional
public class RecordServiceImpl implements RecordService {
    @Autowired(required = false)
    private RecordMapper recordMapper;
    @Autowired
    LocalUtil localUtil;

    private Logger logger = LoggerFactory.getLogger(RecordServiceImpl.class);

    @Override
    public Boolean addIncoal(Integer personid,Integer map) {
        return recordMapper.addIncoal(personid,map)>0;
    }

    @Override
    public Boolean updateIncoal(Integer personid, Integer map,String outTime) {
        return recordMapper.updateIncoal(personid,map,outTime)>0;
    }

    @Override
    public int findByInSub(Integer personid, String num) {
        return recordMapper.findByInSub(personid,num);
    }

    @Override
    public int addInsub(Integer personid, String num, Integer map) {
        return recordMapper.addInsub(personid,num,map);
    }

    @Override
    public int updateInsub(Integer personid, String outTime) {
        return recordMapper.updateInsub(personid,outTime);
    }

    @Override
    public int findByInArea(Integer personid, Integer area) {
        return recordMapper.findByInArea(personid,area);
    }

    @Override
    public int addInArea(Integer personid, Integer area, Integer map) {
        return recordMapper.addInArea(personid,area,map);
    }

    @Override
    public int updateInArea(Integer personid, Integer area, String outTime) {
        return recordMapper.updateInArea(personid,area,outTime);
    }

    @Override
    public List<PersonIncoal> findByIncal(Integer map, Integer departmentid, Integer worktypeid, Integer jobid, String keyword) {
        return recordMapper.findByIncal(map,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT),LocalUtil.get(KafukaTopics.ONLINE),LocalUtil.get(KafukaTopics.OFFLINE));
    }

    @Override
    public List<PersonOff> findByOff(Integer map, Integer departmentid, Integer worktypeid, Integer jobid, String keyword) {
        return recordMapper.findByOff(map,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT),LocalUtil.get(KafukaTopics.ONLINE),LocalUtil.get(KafukaTopics.OFFLINE));
    }

    @Override
    public List<PersonIncoal> findByOvertime(Integer map, Integer departmentid, Integer worktypeid, Integer jobid, String keyword,Integer pageIndex,Integer pageSize) {
        return recordMapper.findByOvertime(map,departmentid,worktypeid,jobid,keyword,pageIndex,pageSize,localUtil.getLocale(),LocalUtil.get(KafukaTopics.TIMEFORMAT),LocalUtil.get(KafukaTopics.OFFLINE));
    }

    @Override
    public List<SubSyn> findBySub(Integer map, String num, Integer networkstate, Integer powerstate, Integer error) {
        List<SubSyn> subSyns=recordMapper.findBySub(map,num,networkstate,powerstate,error,localUtil.getLocale());
        for (SubSyn subSyn:subSyns){
            if (!NullUtils.isEmpty(subSyn.getMaxnum())) {
                if (subSyn.getCount() > subSyn.getMaxnum()) {
                    subSyn.setStatus("超员");
                } else {
                    subSyn.setStatus("正常");
                }
            }
        }
        return subSyns;
    }

    @Override
    public List<PersonSub> findByPersonSub(String num, Integer departmentid, Integer worktypeid, Integer jobid, String keyword) {
        return recordMapper.findByPersonSub(num,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<PersonArea> findByPersonArea(String area, Integer departmentid, Integer worktypeid, Integer jobid, String keyword) {
        return recordMapper.findByPersonArea(area,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<AreaVO> findByArea(Integer map, String name, Integer type, Integer enable) {
        List<AreaVO> areaVOs=recordMapper.findByArea(map,name,type,enable);
        for (AreaVO areaVO:areaVOs){
            if (!NullUtils.isEmpty(areaVO.getMaxnum())) {
                if (areaVO.getCount() > areaVO.getMaxnum()) {
                    areaVO.setStatus("超员");
                } else {
                    areaVO.setStatus("正常");
                }
            }
        }
        return areaVOs;
    }

    @Override
    public void exportPersonIncoalToExcel(ServletOutputStream out, Integer map, Integer departmentid, Integer worktypeid, Integer jobid, String keyword,String title)throws Exception  {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
           // String[] titles = {"序号", "姓名", "工号", "工种", "卡号", "职务", "部门", "下井时间","井下停留时间"};
            String[] titles = new String[9];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.NAME);
            titles[2]= LocalUtil.get(KafukaTopics.NUM);
            titles[3]= LocalUtil.get(KafukaTopics.WORKTYPE);
            titles[4]= LocalUtil.get(KafukaTopics.CARDNUM);
            titles[5]= LocalUtil.get(KafukaTopics.JOB);
            titles[6]= LocalUtil.get(KafukaTopics.DEPARTMENT);
            titles[7]= LocalUtil.get(KafukaTopics.INCOALTIME);
            titles[8]= LocalUtil.get(KafukaTopics.STAYINCOALTIME);

            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 3500);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 3500);
            hssfSheet.setColumnWidth(6, 3500);
            hssfSheet.setColumnWidth(7, 5000);
            hssfSheet.setColumnWidth(8, 5000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<PersonIncoal> personIncoals=recordMapper.findByIncal(map,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT),LocalUtil.get(KafukaTopics.ONLINE),LocalUtil.get(KafukaTopics.OFFLINE));
            if (!NullUtils.isEmpty(personIncoals)){
                for (int i=0;i<personIncoals.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    PersonIncoal personIncoal=personIncoals.get(i);
                    //创建单元格，并设置值
                    HSSFCell cell=hssfRow.createCell(0);
                    cell.setCellValue(i+1);
                    HSSFCell cell1=hssfRow.createCell(1);
                    cell1.setCellValue(personIncoal.getName());
                    HSSFCell cell2=hssfRow.createCell(2);
                    cell2.setCellValue(personIncoal.getNum());
                    HSSFCell cell3=hssfRow.createCell(3);
                    cell3.setCellValue(personIncoal.getWorktypeName());
                    HSSFCell cell4=hssfRow.createCell(4);
                    cell4.setCellValue(personIncoal.getTagName());
                    HSSFCell cell5=hssfRow.createCell(5);
                    cell5.setCellValue(personIncoal.getJobName());
                    HSSFCell cell6=hssfRow.createCell(6);
                    cell6.setCellValue(personIncoal.getDepartmentName());
                    HSSFCell cell7=hssfRow.createCell(7);
                    cell7.setCellValue(dateFormat.format(personIncoal.getTime()));
                    HSSFCell cell8=hssfRow.createCell(8);
                    cell8.setCellValue(personIncoal.getDuration());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception( LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
        }

    @Override
    public void exportPersonOff(ServletOutputStream out, Integer map, Integer departmentid, Integer worktypeid, Integer jobid, String keyword,String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
          //  String[] titles = {"序号", "姓名", "工号", "工种", "卡号", "职务", "部门", "离线时间","离线时长"};
            String[] titles = new String[9];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.NAME);
            titles[2]= LocalUtil.get(KafukaTopics.NUM);
            titles[3]= LocalUtil.get(KafukaTopics.WORKTYPE);
            titles[4]= LocalUtil.get(KafukaTopics.CARDNUM);
            titles[5]= LocalUtil.get(KafukaTopics.JOB);
            titles[6]= LocalUtil.get(KafukaTopics.DEPARTMENT);
            titles[7]= LocalUtil.get(KafukaTopics.OFFLINETIME);
            titles[8]= LocalUtil.get(KafukaTopics.OFFLINEDELAY);
            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 3500);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 3500);
            hssfSheet.setColumnWidth(6, 3500);
            hssfSheet.setColumnWidth(7, 5000);
            hssfSheet.setColumnWidth(8, 5000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<PersonOff> personOffs=recordMapper.findByOff(map,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT),LocalUtil.get(KafukaTopics.ONLINE),LocalUtil.get(KafukaTopics.OFFLINE));
            if (!NullUtils.isEmpty(personOffs)){
                for (int i=0;i<personOffs.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    PersonOff personOff=personOffs.get(i);
                    //创建单元格，并设置值
                    HSSFCell cell=hssfRow.createCell(0);
                    cell.setCellValue(i+1);
                    HSSFCell cell1=hssfRow.createCell(1);
                    cell1.setCellValue(personOff.getName());
                    HSSFCell cell2=hssfRow.createCell(2);
                    cell2.setCellValue(personOff.getNum());
                    HSSFCell cell3=hssfRow.createCell(3);
                    cell3.setCellValue(personOff.getWorktypeName());
                    HSSFCell cell4=hssfRow.createCell(4);
                    cell4.setCellValue(personOff.getTagName());
                    HSSFCell cell5=hssfRow.createCell(5);
                    cell5.setCellValue(personOff.getJobName());
                    HSSFCell cell6=hssfRow.createCell(6);
                    cell6.setCellValue(personOff.getDepartmentName());
                    HSSFCell cell7=hssfRow.createCell(7);
                    cell7.setCellValue(dateFormat.format(personOff.getOffTime()));
                    HSSFCell cell8=hssfRow.createCell(8);
                    cell8.setCellValue(personOff.getDuration());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception( LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
    public void exportPersonOvertimeToExcel(ServletOutputStream out, Integer map, Integer departmentid, Integer worktypeid, Integer jobid, String keyword,String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            /*
             * 表格样式
             * */
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,9);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
          //  String[] titles = {"序号", "姓名", "工号", "工种", "卡号", "职务", "部门", "下井时间","逗留时间","状态"};
            String[] titles = new String[10];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.NAME);
            titles[2]= LocalUtil.get(KafukaTopics.NUM);
            titles[3]= LocalUtil.get(KafukaTopics.WORKTYPE);
            titles[4]= LocalUtil.get(KafukaTopics.CARDNUM);
            titles[5]= LocalUtil.get(KafukaTopics.JOB);
            titles[6]= LocalUtil.get(KafukaTopics.DEPARTMENT);
            titles[7]= LocalUtil.get(KafukaTopics.INCOALTIME);
            titles[8]= LocalUtil.get(KafukaTopics.STAYINCOALTIME);
            titles[9]= LocalUtil.get(KafukaTopics.STATE);
            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 3500);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 3500);
            hssfSheet.setColumnWidth(6, 3500);
            hssfSheet.setColumnWidth(7, 5000);
            hssfSheet.setColumnWidth(8, 5000);
            hssfSheet.setColumnWidth(9, 2000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<PersonIncoal> personOvertimes=recordMapper.findByOvertime(map,departmentid,worktypeid,jobid,keyword,null,null,localUtil.getLocale(),LocalUtil.get(KafukaTopics.TIMEFORMAT),LocalUtil.get(KafukaTopics.OFFLINE));
            if (!NullUtils.isEmpty(personOvertimes)){
                for (int i=0;i<personOvertimes.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    PersonIncoal personOvertime=personOvertimes.get(i);
                    //创建单元格，并设置值
                    HSSFCell cell=hssfRow.createCell(0);
                    cell.setCellValue(i+1);
                    HSSFCell cell1=hssfRow.createCell(1);
                    cell1.setCellValue(personOvertime.getName());
                    HSSFCell cell2=hssfRow.createCell(2);
                    cell2.setCellValue(personOvertime.getNum());
                    HSSFCell cell3=hssfRow.createCell(3);
                    cell3.setCellValue(personOvertime.getWorktypeName());
                    HSSFCell cell4=hssfRow.createCell(4);
                    cell4.setCellValue(personOvertime.getTagName());
                    HSSFCell cell5=hssfRow.createCell(5);
                    cell5.setCellValue(personOvertime.getJobName());
                    HSSFCell cell6=hssfRow.createCell(6);
                    cell6.setCellValue(personOvertime.getDepartmentName());
                    HSSFCell cell7=hssfRow.createCell(7);
                    cell7.setCellValue(dateFormat.format(personOvertime.getTime()));
                    HSSFCell cell8=hssfRow.createCell(8);
                    cell8.setCellValue(personOvertime.getDuration());
                    HSSFCell cell9=hssfRow.createCell(9);
                    cell9.setCellValue(personOvertime.getStatusName());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception( LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
    public void exportexportSubToExcel(ServletOutputStream out, Integer map, String num, Integer networkstate, Integer powerstate, Integer error,String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {

            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
       /*     String[] titles = {"序号", "分站编号", "故障信息", "供电状态", "网络状态", "人数上限", "检测人数","人数状态"};*/
            String[] titles = new String[8];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.NUM_SUB);
            titles[2]= LocalUtil.get(KafukaTopics.ERROR_SUB);
            titles[3]= LocalUtil.get(KafukaTopics.POWER_SUB);
            titles[4]= LocalUtil.get(KafukaTopics.NET_SUB);
            titles[5]= LocalUtil.get(KafukaTopics.TOPCOUNT_PERSON);
            titles[6]= LocalUtil.get(KafukaTopics.CURRENTCOUNT_PERSON);
            titles[7]= LocalUtil.get(KafukaTopics.COUNT_STATE);
            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 2000);
            hssfSheet.setColumnWidth(4, 3500);
            hssfSheet.setColumnWidth(5, 2000);
            hssfSheet.setColumnWidth(6, 2000);
            hssfSheet.setColumnWidth(7, 2000);
            //写入实体数据
            List<SubSyn> subSyns=findBySub(map,num,networkstate,powerstate,error);
            if (!NullUtils.isEmpty(subSyns)){
                for (int i=0;i<subSyns.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    SubSyn subSyn=subSyns.get(i);
                    //创建单元格，并设置值
                    HSSFCell cell=hssfRow.createCell(0);
                    cell.setCellValue(i+1);
                    HSSFCell cell1=hssfRow.createCell(1);
                    cell1.setCellValue(subSyn.getNum());
                    HSSFCell cell3=hssfRow.createCell(2);
                    cell3.setCellValue(subSyn.getErrorName());
                    HSSFCell cell4=hssfRow.createCell(3);
                    cell4.setCellValue(subSyn.getPowerName());
                    HSSFCell cell5=hssfRow.createCell(4);
                    cell5.setCellValue(subSyn.getNetworkName());
                    HSSFCell cell6=hssfRow.createCell(5);
                    if (!NullUtils.isEmpty(subSyn.getMaxnum())) {
                        cell6.setCellValue(subSyn.getMaxnum());
                    }else {
                        cell6.setCellValue("");
                    }
                    HSSFCell cell7=hssfRow.createCell(6);
                    cell7.setCellValue(subSyn.getCount());
                    HSSFCell cell8=hssfRow.createCell(7);
                    cell8.setCellValue(subSyn.getStatus());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception( LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
    public void exportAreaToExcel(ServletOutputStream out, Integer map, String name, Integer type, Integer enable,String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,6);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
           /* String[] titles = {"序号", "区域名称", "区域类型", "是否启用", "人数上限", "检测人数", "人数状态"};*/
            String[] titles = new String[7];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.AREA_NAME);
            titles[2]= LocalUtil.get(KafukaTopics.AREA_TYPE);
            titles[3]= LocalUtil.get(KafukaTopics.AREA_ISUSED);
            titles[4]= LocalUtil.get(KafukaTopics.TOPCOUNT_PERSON);
            titles[5]= LocalUtil.get(KafukaTopics.CURRENTCOUNT_PERSON);
            titles[6]= LocalUtil.get(KafukaTopics.COUNT_STATE);
            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 3500);
            hssfSheet.setColumnWidth(2, 3500);
            hssfSheet.setColumnWidth(3, 2000);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 2000);
            hssfSheet.setColumnWidth(6, 2000);

            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<AreaVO> areaVOS=findByArea(map,name,type,enable);
            if (!NullUtils.isEmpty(areaVOS)){
                for (int i=0;i<areaVOS.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    AreaVO areaVO=areaVOS.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(areaVO.getName());
                    hssfRow.createCell(2).setCellValue(areaVO.getTypeName());
                    if (areaVO.getEnable()==0) {
                        hssfRow.createCell(3).setCellValue("×");
                    }else {
                        hssfRow.createCell(3).setCellValue("√");
                    }
                    if (!NullUtils.isEmpty(areaVO.getMaxnum())) {
                        hssfRow.createCell(4).setCellValue(areaVO.getMaxnum());
                    }else{
                        hssfRow.createCell(4).setCellValue("");
                    }
                    hssfRow.createCell(5).setCellValue(areaVO.getCount());
                    hssfRow.createCell(6).setCellValue(areaVO.getStatus());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception( LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
    public void exportPersonSub(ServletOutputStream out, String num, Integer departmentid, Integer worktypeid, Integer jobid, String keyword,String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            //居中样式
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
          //  String[] titles = {"序号", "姓名", "工号", "工种", "卡号", "职务", "部门", "进入分站时间","分站停留时间"};
            String[] titles = new String[9];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.NAME);
            titles[2]= LocalUtil.get(KafukaTopics.NUM);
            titles[3]= LocalUtil.get(KafukaTopics.WORKTYPE);
            titles[4]= LocalUtil.get(KafukaTopics.CARDNUM);
            titles[5]= LocalUtil.get(KafukaTopics.JOB);
            titles[6]= LocalUtil.get(KafukaTopics.DEPARTMENT);
            titles[7]= LocalUtil.get(KafukaTopics.INSUB_TIME);
            titles[8]= LocalUtil.get(KafukaTopics.STAYSUB_TIME);

            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 3500);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 3500);
            hssfSheet.setColumnWidth(6, 3500);
            hssfSheet.setColumnWidth(7, 5000);
            hssfSheet.setColumnWidth(8, 5000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<PersonSub> personSubs=recordMapper.findByPersonSub(num,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT));
            if (!NullUtils.isEmpty(personSubs)){
                for (int i=0;i<personSubs.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    PersonSub personSub=personSubs.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(personSub.getName());
                    hssfRow.createCell(2).setCellValue(personSub.getNum());
                    hssfRow.createCell(3).setCellValue(personSub.getWorktypeName());
                    hssfRow.createCell(4).setCellValue(personSub.getTagName());
                    hssfRow.createCell(5).setCellValue(personSub.getJobName());
                    hssfRow.createCell(6).setCellValue(personSub.getDepartmentName());
                    hssfRow.createCell(7).setCellValue(dateFormat.format(personSub.getInsubTime()));
                    hssfRow.createCell(8).setCellValue(personSub.getDuration());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception( LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
    public void exportPersonArea(ServletOutputStream out, String area, Integer departmentid, Integer worktypeid, Integer jobid, String keyword,String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {

            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            //居中样式
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //String[] titles = {"序号", "姓名", "工号", "工种", "卡号", "职务", "部门", "进入区域时间","区域停留时间"};
            String[] titles = new String[9];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.NAME);
            titles[2]= LocalUtil.get(KafukaTopics.NUM);
            titles[3]= LocalUtil.get(KafukaTopics.WORKTYPE);
            titles[4]= LocalUtil.get(KafukaTopics.CARDNUM);
            titles[5]= LocalUtil.get(KafukaTopics.JOB);
            titles[6]= LocalUtil.get(KafukaTopics.DEPARTMENT);
            titles[7]= LocalUtil.get(KafukaTopics.INAREA_TIME);
            titles[8]= LocalUtil.get(KafukaTopics.STAYAREA_TIME);
            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 3500);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 3500);
            hssfSheet.setColumnWidth(6, 3500);
            hssfSheet.setColumnWidth(7, 5000);
            hssfSheet.setColumnWidth(8, 5000);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<PersonArea> personAreas=recordMapper.findByPersonArea(area,departmentid,worktypeid,jobid,keyword,LocalUtil.get(KafukaTopics.TIMEFORMAT));
            if (!NullUtils.isEmpty(personAreas)){
                for (int i=0;i<personAreas.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    PersonArea personArea=personAreas.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(personArea.getName());
                    hssfRow.createCell(2).setCellValue(personArea.getNum());
                    hssfRow.createCell(3).setCellValue(personArea.getWorktypeName());
                    hssfRow.createCell(4).setCellValue(personArea.getTagName());
                    hssfRow.createCell(5).setCellValue(personArea.getJobName());
                    hssfRow.createCell(6).setCellValue(personArea.getDepartmentName());
                    hssfRow.createCell(7).setCellValue(dateFormat.format(personArea.getInareaTime()));
                    hssfRow.createCell(8).setCellValue(personArea.getDuration());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception( LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }
}