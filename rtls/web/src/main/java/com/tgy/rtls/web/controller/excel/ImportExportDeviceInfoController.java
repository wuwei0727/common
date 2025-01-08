package com.tgy.rtls.web.controller.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.config.ImportUsersException;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.excel.FloorLockVo;
import com.tgy.rtls.data.entity.excel.GatewayVo;
import com.tgy.rtls.data.entity.excel.QrCodeLocationDTO;
import com.tgy.rtls.data.mapper.equip.GatewayMapper;
import com.tgy.rtls.data.mapper.vip.FloorLockMapper;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.equip.importExportDeviceInfo;
import com.tgy.rtls.data.service.map.impl.QrCodeLocationService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.equip
 * @Author: wuwei
 * @CreateTime: 2022-12-19 09:38
 * @Description: TODO
 * @Version: 1.0
 */
@Controller
@CrossOrigin
@RequestMapping(value = "/importExportDeviceInfo")
public class ImportExportDeviceInfoController {
    @Resource
    private SubService subService;
    @Resource
    private importExportDeviceInfo importExportDeviceInfo;
    @Resource
    private GatewayMapper gatewayMapper;
    @Resource
    private FloorLockMapper floorLockMapper;
    @Resource
    private QrCodeLocationService qrCodeLocationService;

    @RequestMapping(value = "importSubLocationExcel")
    @ApiOperation(value = "导入信标位置", notes = "xls文件")
    @ResponseBody
    public CommonResult<String> importSubLocationExcel(@RequestBody MultipartFile file) {
        String res = "";
        String fileName = file.getOriginalFilename();
        if (!file.isEmpty()) {
            try {
                res = subService.importSubLocationExcel(fileName, file);
                return new CommonResult<>(220, res);
            } catch (ImportUsersException ex) {
                return new CommonResult<>(400, ex.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
            }
        } else {
            return new CommonResult<String>(400, LocalUtil.get(KafukaTopics.IMPORT_FAIL_EMPTYFILE));
        }
    }

    @RequestMapping(value = "exportSubLocationExcel/{mapId}")
    public void exportSubLocationExcel(@PathVariable("mapId") String mapId,HttpServletResponse response) {
        try {
            subService.exportSubLocationExcel(mapId,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出excel探测器
     *
     * @param mapId    地图上标识
     * @param response 响应
     * @throws Exception 异常
     */
    @RequestMapping(value = "/exportDetectorExcel/{mapId}")
    @ApiOperation(value = "导出车位检测器", notes = "设备信息")
    public void exportDetectorExcel(@PathVariable("mapId") String mapId, HttpServletResponse response) throws Exception {
        try {
            importExportDeviceInfo.exportDetector(mapId, response);
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());

        }
    }

    @PostMapping(value = "/exportGatewayExcel/{mapId}")
    @ApiOperation(value = "导出网关", notes = "设备信息")
    public void exportGatewayExcel(@PathVariable("mapId") String mapId, HttpServletResponse response) throws Exception {
        try {


            List<GatewayVo> list = gatewayMapper.getGatewayInfo(mapId);
            String fileName = list.get(0).getMapName()+"网关设备信息"+ ".xlsx";
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            EasyExcel.write(response.getOutputStream(), GatewayVo.class)//对应的导出实体类
                    .excelType(ExcelTypeEnum.XLSX)//excel文件类型，包括CSV、XLS、XLSX
                    .sheet("网关设备信息表")//导出sheet页名称
                    .doWrite(list);

        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());

        }
    }


    @PostMapping(value = "exportFloorLockExcel/{mapId}")
    public void exportFloorLockExcel(@PathVariable("mapId") String mapId,HttpServletResponse response) {
        try {
            List<FloorLockVo> list = floorLockMapper.getFloorsLockInfo(mapId);
            String fileName = list.get(0).getMapName()+"地锁设备信息"+ ".xlsx";
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") );
            EasyExcel.write(response.getOutputStream(), FloorLockVo.class)//对应的导出实体类
                    .excelType(ExcelTypeEnum.XLSX)//excel文件类型，包括CSV、XLS、XLSX
                    .sheet("地锁设备信息表")//导出sheet页名称
                    .doWrite(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "exportQrCodeLocationExcel/{mapId}")
    public void exportExcel(@PathVariable("mapId") String mapId,HttpServletResponse response) {
        try {
            List<QrCodeLocationDTO> list = qrCodeLocationService.getByMapId(mapId);
            String fileName = list.get(0).getMapName()+"二维码位置信息"+ ".xlsx";
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") );
            EasyExcel.write(response.getOutputStream(), QrCodeLocationDTO.class)//对应的导出实体类
                    .excelType(ExcelTypeEnum.XLSX)//excel文件类型，包括CSV、XLS、XLSX
                    .sheet("二维码位置信息")//导出sheet页名称
                    .doWrite(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
