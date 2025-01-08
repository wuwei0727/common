package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.InfraredVo;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.service.equip.importExportDeviceInfo;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.equip.impl
 * @Author: wuwei
 * @CreateTime: 2023-03-21 14:55
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class importExportDeviceInfoImpl implements importExportDeviceInfo {
    @Autowired
    private TagMapper tagMapper;
    @Override
    public void exportDetector(String map, HttpServletResponse response) throws IOException {
        List<InfraredVo> infraredInfo = tagMapper.getDetectorByMap(map);
            String mapName = tagMapper.getMapName(map);
            String fileName = mapName+"检测器设备信息"+ ".xlsx";;
            //创建HSSFWorkbook对象,  excel的文档对象
            HSSFWorkbook workbook = new HSSFWorkbook();
            //excel的表单
            HSSFSheet sheet = workbook.createSheet("检测器设备信息表");
            HSSFRow hssfRow = null;
            int rowNum = 0;
        if(!NullUtils.isEmpty(infraredInfo)) {
            String[] headers = {"设备编号", "关联地图", "楼层", "关联车位", "原设备编号","检测器状态","网络状态","电量","电压检测时间","创建时间","更新时间"};
            hssfRow = sheet.createRow(rowNum);//创建第一个单元格
            hssfRow.setHeight((short) (26.25 * 20));
            hssfRow.createCell(0).setCellValue(infraredInfo.get(0).getMapName());//为第一行单元格设值
            sheet.setColumnWidth(0, 20 * 256);
            //列宽自适应
            for (int i = 0; i <= hssfRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }
            CellRangeAddress rowRegion = new CellRangeAddress(rowNum, rowNum, 0, headers.length - 1);
            sheet.addMergedRegion(rowRegion);

            //新增数据行，并且设置单元格数据
            rowNum++;
            //headers表示excel表中第一行的表头
            hssfRow = sheet.createRow(rowNum);

            //在excel表中添加表头
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = hssfRow.createCell(i);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);
                cell.setCellValue(text);
            }
            rowNum++;
            for (InfraredVo infraredVo : infraredInfo) {
                hssfRow = sheet.createRow(rowNum++);
                if (!NullUtils.isEmpty(infraredVo.getNum())) {
                    hssfRow.createCell(0).setCellValue(infraredVo.getNum());
                } else {
                    hssfRow.createCell(0).setCellValue("");
                }
                if (!NullUtils.isEmpty(infraredVo.getMapName())) {
                    hssfRow.createCell(1).setCellValue(infraredVo.getMapName());
                } else {
                    hssfRow.createCell(1).setCellValue("");
                }
                if (!NullUtils.isEmpty(infraredVo.getFloor())) {
                    hssfRow.createCell(2).setCellValue(infraredVo.getFloor());
                } else {
                    hssfRow.createCell(2).setCellValue("");
                }
                if (!NullUtils.isEmpty(infraredVo.getPlaceName())) {
                    hssfRow.createCell(3).setCellValue(infraredVo.getPlaceName());
                } else {
                    hssfRow.createCell(3).setCellValue("");
                }

                if (!NullUtils.isEmpty(infraredVo.getRawProductId())) {
                    hssfRow.createCell(4).setCellValue(infraredVo.getRawProductId());
                } else {
                    hssfRow.createCell(4).setCellValue("");
                }

                if (!NullUtils.isEmpty(infraredVo.getStatus())) {
                    if ("0".equals(infraredVo.getNetworkstate())) {
                        hssfRow.createCell(5).setCellValue("空闲");
                    } else if ("1".equals(infraredVo.getNetworkstate())) {
                        hssfRow.createCell(5).setCellValue("占用");
                    } else {
                        hssfRow.createCell(5).setCellValue("");
                    }
                } else {
                    hssfRow.createCell(4).setCellValue("");
                }

                if (!NullUtils.isEmpty(infraredVo.getNetworkstate())) {
                    if ("0".equals(infraredVo.getNetworkstate())) {
                        hssfRow.createCell(6).setCellValue("离线");
                    } else if ("1".equals(infraredVo.getNetworkstate())) {
                        hssfRow.createCell(6).setCellValue("在线");
                    } else {
                        hssfRow.createCell(6).setCellValue("");
                    }
                } else {
                    hssfRow.createCell(6).setCellValue("");
                }

                if (!NullUtils.isEmpty(infraredVo.getPower())) {
                    hssfRow.createCell(7).setCellValue(infraredVo.getPower());
                } else {
                    hssfRow.createCell(7).setCellValue("");
                }

                if (!NullUtils.isEmpty(infraredVo.getBatteryTime())) {
                    HSSFCell cell = hssfRow.createCell(8);
                    cell.setCellValue(infraredVo.getBatteryTime());
                    HSSFCellStyle cellStyle = workbook.createCellStyle();
                    HSSFDataFormat dataFormat = workbook.createDataFormat();
                    cellStyle.setDataFormat(dataFormat.getFormat("yyyy/mm/dd hh:mm:ss"));
                    cell.setCellStyle(cellStyle);
                } else {
                    hssfRow.createCell(8).setCellValue("");
                }

                if (!NullUtils.isEmpty(infraredVo.getAddTime())) {
                    HSSFCell cell = hssfRow.createCell(9);
                    cell.setCellValue(infraredVo.getAddTime());
                    HSSFCellStyle cellStyle = workbook.createCellStyle();
                    HSSFDataFormat dataFormat = workbook.createDataFormat();
                    cellStyle.setDataFormat(dataFormat.getFormat("yyyy/mm/dd hh:mm:ss"));
                    cell.setCellStyle(cellStyle);
                } else {
                    hssfRow.createCell(9).setCellValue("");
                }

                if (!NullUtils.isEmpty(infraredVo.getUpdateTime())) {
                    HSSFCell cell = hssfRow.createCell(10);
                    cell.setCellValue(infraredVo.getUpdateTime());
                    HSSFCellStyle cellStyle = workbook.createCellStyle();
                    HSSFDataFormat dataFormat = workbook.createDataFormat();
                    cellStyle.setDataFormat(dataFormat.getFormat("yyyy/mm/dd hh:mm:ss"));
                    cell.setCellStyle(cellStyle);
                } else {
                    hssfRow.createCell(10).setCellValue("");
                }
            }
            // 将工作簿写入输出流
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.addHeader("Content-disposition", String.format("attachment; filename=\"%s\"",
                    URLEncoder.encode(fileName, "UTF-8")));
            workbook.write(response.getOutputStream());
            // 关闭工作簿
            workbook.close();

        }else {
            throw new IOException("无法导出空的设备信息！");
        }
    }
}
