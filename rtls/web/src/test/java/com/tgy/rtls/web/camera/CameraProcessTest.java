//package com.tgy.rtls.web.camera;
//
//import com.tgy.rtls.data.entity.Camera.CameraConfigResponse;
//import com.tgy.rtls.data.entity.Camera.CameraPlace;
//import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
//import org.junit.jupiter.api.Test;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//// 测试类
//public class CameraProcessTest {
//
//    @Test
//    public void testProcessFlow() {
//        // 1. 准备测试数据
//        CameraProcessor processor = new CameraProcessor();
//
//        // 测试场景1: 已绑定摄像头
//        testBoundCamera(processor);
//
//        // 测试场景2: 未绑定摄像头，找到最近的摄像头
//        testUnboundCamera(processor);
//
//        // 测试场景3: 记录处理 - 单条记录
//        testSingleRecord(processor);
//
//        // 测试场景4: 记录处理 - 多条记录(n<4)
//        testMultipleRecords(processor);
//
//        // 测试场景5: 记录处理 - 大量记录(n>=4)
//        testManyRecords(processor);
//    }
//
//    private void testBoundCamera(CameraProcessor processor) {
//        // 模拟已绑定摄像头的情况
//        CameraPlace existingBinding = CameraPlace.builder()
//                .cameraVehicleCaptureId(101L)
//                .placeId(201L)
//                .build();
//
//        List<CameraVehicleCapture> records = Collections.singletonList(
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(1)
//                        .build()
//        );
//
//        processor.processWithMockData(201, 1, "P001", "1", "100", "100", existingBinding, records);
//    }
//
//    private void testUnboundCamera(CameraProcessor processor) {
//        List<CameraConfigResponse> cameras = Arrays.asList(
//            CameraConfigResponse.builder()
//                .cameraId(1L)
//                .serialNumber("101")
//                .x("90")
//                .y("90")
//                .radius("30")
//                .build(),
//            CameraConfigResponse.builder()
//                .cameraId(2L)
//                .serialNumber("102")
//                .x("110")
//                .y("110")
//                .radius("30")
//                .build()
//        );
//
//        processor.processWithMockData(202, 1, "P002", "1", "100", "100", null, cameras);
//    }
//
//    private void testSingleRecord(CameraProcessor processor) {
//        List<CameraVehicleCapture> records = Collections.singletonList(
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(0)
//                        .build()
//        );
//
//        processor.processRecords(records, 203, 101L);
//    }
//
//    private void testMultipleRecords(CameraProcessor processor) {
//        // 测试场景4: 记录处理 - 多条记录(n<4)
//        List<CameraVehicleCapture> records = Arrays.asList(
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(1)
//                        .build(),
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(2)
//                        .build(),
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(2)
//                        .build()
//        );
//
//        System.out.println("\n=== 测试场景4: 多条记录(n<4) ===");
//        processor.processRecords(records, 204, 101L);
//
//        // 验证结果
//        System.out.println("处理后的记录状态：");
//        records.forEach(record ->
//                System.out.println("记录ID: " + record.getId() +
//                        ", 标志位: " + record.getUniqueFlag() +
//                        ", 车位: " + record.getPlace())
//        );
//    }
//
//    private void testManyRecords(CameraProcessor processor) {
//        // 测试场景5: 记录处理 - 大量记录(n>=4)
//        List<CameraVehicleCapture> records = Arrays.asList(
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(1)
//                        .build(),
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(2)
//                        .build(),
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(2)
//                        .build(),
//                CameraVehicleCapture.builder()
//                        .serialNumber("101")
//                        .uniqueFlag(3)
//                        .build()
//        );
//
//        System.out.println("\n=== 测试场景5: 大量记录(n>=4) ===");
//        processor.processRecords(records, 205, 101L);
//
//        // 验证结果
//        System.out.println("处理后的记录状态：");
//        records.forEach(record ->
//                System.out.println("记录ID: " + record.getId() +
//                        ", 标志位: " + record.getUniqueFlag() +
//                        ", 车位: " + record.getPlace())
//        );
//    }
//}