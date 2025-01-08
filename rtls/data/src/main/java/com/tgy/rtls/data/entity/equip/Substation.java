package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/10/19
 * 分站信息类
 */
@Data
@ToString
public class Substation implements Serializable {
    private Integer id;
    private Integer bsid;
    private String num;//分站编号
    private Integer type=1;//分站类型 1普通分站 2出入口分站
    private Integer deviceType=1;//分站类型 1普通分站 2出入口分站
    private String map;//关联的地图id
    private Integer networkstate;//网络状态 0离线 1在线
    private Integer powerstate;//供电状态
    private Integer error;//错误码
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime addTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    private Integer instanceid;//实例id
    private Integer power;//电量百分比
    private String power1;//电量百分比
    private Integer maxnum;//人数上限
    private String word;//公司名称
    private String locationword;//基站位置信息
    private String background;//显示背景
    private String backgroundlocal;//显示背景
    private Integer exitDirection;//出口方向 1左侧 2右侧
    private String antennadelay;//天线延时
    private String disfix;//校正系数
    private String batteryVolt;//电池电压
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date batteryTime;//电压检测时间
    private int ipType;//ip类型 0动态ip 1静态ip
    private String ipAddress;//ip地址
    private String subnetMask;//子网掩码
    private String networkSegment;//网段
    private String gatewayAddress;//网关地址

    private String typeName;//分站类型 1普通分站 2出入口分站
    private String networkName;//网络状态名
    private String powerName;//供电状态名
    private String errorName;//错误码名称
    private String mapName;//关联地图名
    private Double x;
    private Double y;
    private Double z;
    private Short floor;//楼层

    /*
    * 开发者模式的字段
    * */
    private String release;//软件版本号
    private String product;//硬件版本号
    private String fru;//FRU
    private String pid;//产品编号
    private String sn;//序列号
    private int armupdatestate;//软件升级进度 -1失败 0正常 100成功 1-99升级中
    private int uwbupdatestate;//uwb升级进度
    private String core;//CORE
    private String qt;//QT
    private String ucb1;//UCB1
    private String ucb2;//UCB2


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime batteryTime1;//电压检测时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addTime1;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime1;
    private String floorName;

    private Integer lifetimeMonths;

    public static void main(String[] args) {
        // 定义输入和输出文件路径
        String inputFilePath = "C:\\Users\\Administrator\\Downloads\\demo.txt";
        String outputFilePath = "C:\\Users\\Administrator\\Downloads\\filtered_demo.txt";
        Charset charset = StandardCharsets.UTF_8;  // 如有需要，可更换为 Charset.forName("GBK")

        // 用于存储唯一的 #网关# 数字
        Set<String> uniqueGateways = new HashSet<>();

        // 正则表达式，匹配 #网关# 后面的数字
        Pattern pattern = Pattern.compile("#网关#(\\d+)");

        try (
                // 读取文件
                BufferedReader reader = Files.newBufferedReader(Paths.get(inputFilePath),charset);
                // 写入文件
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath),charset)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 去掉首尾空格
                line = line.trim();

                // 检查行长度，确保不是空行
                if (line.isEmpty()) {
                    continue; // 跳过空行
                }

                // 匹配 #网关#数字
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {  // 需要完全匹配整个行
                    String gatewayNumber = matcher.group(0); // 例如：#网关#123

                    // 如果这个 #网关#数字 是第一次出现，则写入新文件
                    if (uniqueGateways.add(gatewayNumber)) {
                        writer.write(line);
                        writer.newLine();
                    }
                } else {
                    System.out.println("未匹配格式的行: " + line);
                }
            }
            System.out.println("去重后的内容已写入：" + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
