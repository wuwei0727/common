package com.tgy.rtls.data.entity.Camera;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CarInfoResponse {
    private int Code;
    private List<CarInfo> Describe;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CarInfo {
        private String ParkingNo;
        private String CarPlateNo;
    }
}
