package com.tgy.rtls.data.config;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;


/**
 * @author 许强
 * @Package com.tgy.rtls.data.config
 * @date 2020/12/21
 * 设置excel的样式
 */
public class SheetStyle {
    public static void setColumnWidth() {

    }

    /*
    * 合并单元格并填充样式
    * */
    public static void setRegionStyle(HSSFSheet sheet, CellRangeAddress region, CellStyle cs) {
        for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
            HSSFRow row = sheet.getRow(i);
            for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                HSSFCell cell = row.getCell( (short) j);
                if (cell==null){
                    continue;
                }
                cell.setCellStyle(cs);
            }
        }
    }

    /**
     * 水平居中、垂直居中
     * 字体：宋体
     * 字体大小：16号
     * 加粗
     * @param workbook
     * @return
     */
    public static CellStyle getStyle(HSSFWorkbook workbook) {
        CellStyle cellstyle=workbook.createCellStyle();
        cellstyle.setAlignment(HorizontalAlignment.CENTER);//水平居中
        cellstyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        Font font=workbook.createFont();//字体
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints((short)14);//字号
        font.setBold(true);//加粗
        cellstyle.setFont(font);
        setBorderStyle(cellstyle);
        return cellstyle;
    }

    /**
     * 水平居中、垂直居中
     * 字体：宋体
     * 字体大小：14号
     * 加粗
     * @param workbook
     * @return
     */
    public static CellStyle getStyle1(HSSFWorkbook workbook) {
        CellStyle cellstyle=workbook.createCellStyle();
        cellstyle.setAlignment(HorizontalAlignment.CENTER);//水平居中
        cellstyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        Font font=workbook.createFont();//字体
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints((short)12);//字号
        cellstyle.setFont(font);
        setBorderStyle(cellstyle);
        return cellstyle;
    }
    public static CellStyle getStyle2(HSSFWorkbook workbook) {
        CellStyle cellstyle=workbook.createCellStyle();
        cellstyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellstyle.setAlignment(HorizontalAlignment.CENTER);//水平居中
        cellstyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        Font font=workbook.createFont();//字体
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints((short)12);//字号
        cellstyle.setFont(font);
        setBorderStyle(cellstyle);
        return cellstyle;
    }
    /**
     * 边框样式
     * @param style
     */
    public static void setBorderStyle(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN); //下边框
        style.setBorderLeft(BorderStyle.THIN);//左边框
        style.setBorderTop(BorderStyle.THIN);//上边框
        style.setBorderRight(BorderStyle.THIN);//右边框
    }

    /**
     * 奇数行
     * 背景颜色为黄色
     * @param style
     */
    public static void setCellStyleYellow(CellStyle style) {
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    /**
     * 偶数行
     * 背景颜色为LIME
     * @param style
     */
    public static void setCellStyleLime(CellStyle style) {
        style.setFillForegroundColor(IndexedColors.LIME.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    /**
     * 字体设置红色
     * @param workbook
     * @param style
     */
    public static void setFontRedColor(HSSFWorkbook workbook,CellStyle style) {
        Font font=workbook.createFont();//字体
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
    }

    /**
     * 单元格设置颜色
     * @param workbook
     * @param style
     */
    public static CellStyle getStyleColor(HSSFWorkbook workbook,String style) {
        CellStyle cellstyle=workbook.createCellStyle();
        switch (style) {
            case "c36C2CF":
                cellstyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
                break;
            case "cff8f8f":
                cellstyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                break;
            case "c349434":
                cellstyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
                break;
            case "cffdb33":
                cellstyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
                break;
            case "cab8fe3":
                cellstyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
                break;
            case "cff9dfc":
                cellstyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                break;
        }
        cellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellstyle.setAlignment(HorizontalAlignment.CENTER);//水平居中
        cellstyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        Font font=workbook.createFont();//字体
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints((short)12);//字号
        cellstyle.setFont(font);
        setBorderStyle(cellstyle);
        return cellstyle;
    }
}
