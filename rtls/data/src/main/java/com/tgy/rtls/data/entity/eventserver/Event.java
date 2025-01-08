package com.tgy.rtls.data.entity.eventserver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

// 心跳请求类
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private String operator;
    private DeviceInfo deviceInfo;
    private Info info;

    @Data
    public static class DeviceInfo {
        private String serialNumber;
        private String platformId;
        @JsonProperty("IP")
        private String IP;
        @JsonProperty("MAC")
        private String MAC;
        @JsonProperty("SN")
        private String SN;
        @JsonProperty("DID")
        private String DID;
        private String deviceType;
        private String deviceVersion;
    }

    @Data
    public static class Info {
        private Integer eventId;
        private String time;
        private String strategyVersion;
        private CarLicenseAttriInfo carLicenseAttriInfo;
//        private CarAttriInfo carAttriInfo;
        @JsonProperty("CaptureImage")
        private CaptureImage captureImage;
//        private BackgroundImage backgroundImage;
    }

    @Data
    public static class CarLicenseAttriInfo {
        //private Integer AttriClass;
//        private Integer CarAttriColor;
//        private Integer AttriOrientation;
        @JsonProperty("Number")
        private String Number;
//        private Integer CarLicenseAttriColor;
//        private Integer CarLicenseRowNum;
//        private CarLicenseRect carLicenseRect;
    }

    @Data
    public static class CarLicenseRect {
        private Integer Left;
        private Integer Right;
        private Integer Top;
        private Integer Bottom;
    }

    @Data
    public static class CarAttriInfo {
        private Integer DriveEntryDrection;
        private Integer Match;
        private Integer Score;
        private CarRect carRect;
    }

    @Data
    public static class CarRect {
        private Integer Left;
        private Integer Right;
        private Integer Top;
        private Integer Bottom;
    }

    @Data
    public static class CaptureImage {
        private Integer width;
        private Integer height;
        private Integer pictureLength;
        private String picture;
        private Integer base64PicLength;
        private String pictureMd5;
    }

    @Data
    public static class BackgroundImage {
        private Integer width;
        private Integer height;
        private Integer pictureLength;
        private String picture;
        private Integer base64PicLength;
        private String pictureMd5;
    }
}