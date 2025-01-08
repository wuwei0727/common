package com.tgy.camerademo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

// 心跳响应类
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatResponse {
    private String operator;
    private Info info;
    private Result result;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Info {
        private int eventId;
        @JsonProperty("eventID")
        private int eventIds;
        private String time;
        private int heartbeatInterval;
        private String eventSendMode;
        private Strategy strategy;


        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Strategy {
            private int passengerStaticsInterval;
            private int heartBeatInterval;
            @JsonProperty("isEnableElectronicDefence")
            private boolean isEnableElectronicDefence;

            @JsonProperty("isCrossBorderDetectEnable")
            private boolean isCrossBorderDetectEnable;

            @JsonProperty("isOffDutyDetectEnable")
            private boolean isOffDutyDetectEnable;

            @JsonProperty("isPassengerFlowStaticsEnable")
            private boolean isPassengerFlowStaticsEnable;

            @JsonProperty("isCryScreamDetectEnable")
            private boolean isCryScreamDetectEnable;

            @JsonProperty("isPetDetectEnable")
            private boolean isPetDetectEnable;

            @JsonProperty("isFallDetectEnable")
            private boolean isFallDetectEnable;

            @JsonProperty("isSnapshotEnable")
            private boolean isSnapshotEnable;

            @JsonProperty("isPersonInfoEnable")
            private boolean isPersonInfoEnable;

            @JsonProperty("isPersonDetectEnable")
            private boolean isPersonDetectEnable;

            @JsonProperty("isCarLicenseSnapshotEnable")
            private boolean isCarLicenseSnapshotEnable;

            @JsonProperty("isCarDetectEnable")
            private boolean isCarDetectEnable;

        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private int errorNo;
        private String description;
    }
}