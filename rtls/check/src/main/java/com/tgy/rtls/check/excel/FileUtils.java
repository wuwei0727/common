package com.tgy.rtls.check.excel;

import com.tgy.rtls.check.refelect.AnnotationTool;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件相关操作
 * @author Louis
 * @date Jan 14, 2019
 */
public class FileUtils {



    public static File createUserExcelFile(HttpServletResponse response,List<?> records,String filename) throws IOException {
        List<Map> res = null;

        if (records != null)
            res = AnnotationTool.getFixedVoList(records);
        Workbook workbook = new XSSFWorkbook();
        //创建一个sheet，括号里可以输入sheet名称，默认为sheet0
        Sheet sheet = workbook.createSheet();
        Row row0 = sheet.createRow(0);
        int rowlength = res.size();
        Set firstrow = res.get(0).keySet();
        int clomnlen = firstrow.size();
            int index=-1;
        for (int columnIndex = 0; columnIndex < clomnlen; columnIndex++) {
            String head = firstrow.toArray()[columnIndex] + "";
            if(head.indexOf("检卡时长")!=-1){
               index=columnIndex;
            }
            row0.createCell(columnIndex).setCellValue(head);
        }

        for (int i = 0; i < res.size(); i++) {
            Map rowContent = res.get(i);
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < clomnlen; j++) {
                Object ce = rowContent.values().toArray()[j];
                String cellContent=ce+"";
                if(ce instanceof Double){
                   Double ced=(Double) ce;
                   cellContent=ced.doubleValue()*100+"%";
                }
                if(index==j){
                    cellContent=cellContent+"毫秒";
                }
                row.createCell(j).setCellValue(cellContent);
            }

        }
        downloadFile(response,workbook, filename);

        return null;
        //调用PoiUtils工具包
        //return createExcelFile(workbook, filename);
    }

    /**
     * 关闭对象，连接
     * @param closeable
     */
    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    /**
     * 生成Excel文件
     * @param workbook
     * @param fileName
     * @return
     */
    public static File createExcelFile(Workbook workbook, String fileName) {
        OutputStream stream = null;
        File file = null;
        try {
            //用了createTempFile，这是创建临时文件，系统会自动给你的临时文件编号，所以后面有号码，你用createNewFile的话就完全按照你指定的名称来了
            file = File.createTempFile(fileName, ".xlsx");
            stream = new FileOutputStream(file.getAbsoluteFile());
            workbook.write(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //这里调用了IO工具包控制开关
            closeQuietly(workbook);
            closeQuietly(stream);
        }
        return file;
    }

    /**
     * 下载文件
     *
     * @param response
     * @param
     * @param newFileName
     */
    public static void downloadFile(HttpServletResponse response, Workbook workbook,  String newFileName) throws IOException {
        ServletOutputStream out= response.getOutputStream(); ;
        try {
            String fileName=new String((newFileName+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(),"UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + newFileName + ".csv");
            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.flush();
            out.close();
        }
    }

}