package com.tgy.rtls.data.service.common.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.Operationlog;
import com.tgy.rtls.data.mapper.common.OperationlogMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common.impl
 * @date 2020/11/19
 */
@Service
public class OperationlogServiceImpl implements OperationlogService {
    @Autowired(required = false)
    private OperationlogMapper operationlogMapper;
    @Autowired
    private RedisService redisService;
    @Override
    public List<Operationlog> findByAll(String userName, String ip, String startTime, String endTime) {
        return operationlogMapper.findByAll(userName,ip,startTime,endTime);
    }

    @Override
    public boolean addOperationlog(Integer uid, String incident,Integer instanceidNew) {
        Integer instanceid= instanceidNew;
        try {
            instanceid = Integer.parseInt(redisService.get("instance" + uid));
        }catch (Exception e){

        }
        return true;
        //return operationlogMapper.addOperationlog(uid,incident,instanceid)>0;
    }

    @Override
    public boolean addOperationlog(Integer uid, String incident) {
        Integer instanceid= null;
        try {
            instanceid = Integer.parseInt(redisService.get("instance" + uid));
        }catch (Exception e){

        }
        return operationlogMapper.addOperationlog(uid,incident,instanceid)>0;
    }

    @Override
    public boolean addOperationloguser(Integer userId, String incident) {
        Integer instanceid= null;
        try {
            instanceid = Integer.parseInt(redisService.get("instance" + userId));
        }catch (Exception e){

        }
        return operationlogMapper.addOperationlog(userId,incident,instanceid)>0;
    }

    @Override
    public void deleteOperationlog(Integer instanceid) {
         operationlogMapper.deleteOperationlog(instanceid);
    }

    public static void main(String[] args) {
        int[] ss={100,100,88,454};
        solution(ss);
    }

    public static  int solution(int[] A) {
        // write your code in Java SE 8

        Arrays.sort(A);

        int length=A.length;
        int small=A[0];
        int big=A[length-1];
        for(int i=0;i<length;i++){
            if(small>=A[i] ){
                small=A[i];
            }
            if(big<=A[i] ){
                big=A[i];
            }

        }
        int diff=(big-small);
        boolean contain=false;
        for(int i=1;i<=(diff+1);i++){
            int dsd=small+i;
            int  has=0;
            for(int k=0;k<length;k++){
                if(dsd==A[k]){
                    continue;
                }else {
                    has++;
                }
            }
            if(has==length&&dsd>0){
                small=dsd;
                contain=true;
                break;
            }


        }
        if(!contain){
            small=big+1;
        }



        if(small<=0){
            small=1;
        }
        return small;
    }

    @Override
    public void exportOperation(ServletOutputStream out, String startTime, String endTime,String title)throws Exception {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            //在sheet中添加表头第1行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            //居中样式
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            String[] titles = new String[4];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.PERSON);
            titles[2]= LocalUtil.get(KafukaTopics.EVENT);
            titles[3]= LocalUtil.get(KafukaTopics.TIME);
            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 3500);
            hssfSheet.setColumnWidth(2, 6000);
            hssfSheet.setColumnWidth(3, 3500);
            //写入实体数据
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<Operationlog> operationlogList=operationlogMapper.findByAll(null,null, startTime,endTime);
            if (!NullUtils.isEmpty(operationlogList)){
                for (int i=0;i<operationlogList.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    Operationlog operationlog=operationlogList.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(operationlog.getName());
                    hssfRow.createCell(2).setCellValue(operationlog.getIncident());
                    hssfRow.createCell(3).setCellValue(dateFormat.format(operationlog.getTime()));

                }
            }
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,3);
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

    @Override
    public boolean addUserOperationlog(Integer uid, String ip, String incident, LocalDateTime time) {
        return operationlogMapper.addUserOperationlog(uid, ip, incident, time);
    }
}
