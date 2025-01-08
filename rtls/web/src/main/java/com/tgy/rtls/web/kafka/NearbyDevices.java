package com.tgy.rtls.web.kafka;

import java.util.ArrayList;
import java.util.List;

class Device {
    double x;
    double y;

    public Device(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // 计算当前设备与另一个设备的距离
    public double distanceTo(Device otherDevice) {
//        return Math.sqrt(Math.pow(this.x - otherDevice.x, 2) + Math.pow(this.y - otherDevice.y, 2));
        return Math.sqrt(Math.pow(otherDevice.x-this.x , 2) + Math.pow(otherDevice.y-this.y, 2));
    }
    
    @Override
    public String toString() {
        return "Device(" + "x=" + x + ", y=" + y + ")";
    }
}

public class NearbyDevices {
    
    // 查询附近30米以内的设备
    public static List<Device> getNearbyDevices(Device currentDevice, List<Device> devices, double radius) {
        List<Device> nearbyDevices = new ArrayList<>();
        
        for (Device device : devices) {
            if (currentDevice.distanceTo(device) <= radius) {
                nearbyDevices.add(device);
            }
        }
        
        return nearbyDevices;
    }

    public static void main(String[] args) {
        // 当前设备的位置
        Device currentDevice = new Device(10, 10);
        
        // 其他设备列表
        List<Device> devices = new ArrayList<>();
        devices.add(new Device(12, 12));  // 设备 1
        devices.add(new Device(15, 16));  // 设备 2
        devices.add(new Device(25, 30));  // 设备 3
        devices.add(new Device(9, 8));    // 设备 4
        
        // 查询30米以内的设备
        double radius = 30.0;
        List<Device> nearbyDevices = getNearbyDevices(currentDevice, devices, radius);
        
        // 输出结果
        System.out.println("Nearby devices within " + radius + " meters:");
        for (Device device : nearbyDevices) {
            System.out.println(device);
        }
    }
}
