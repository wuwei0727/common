package com.tgy.rtls.docking.controller.park;

import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.Backend;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.Post;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-12-19 12:06
 * @Description: TODO
 * @Version: 1.0
 */
@Backend("okhttp3")
@Address(host = "localhost", port = "8080")
public interface aaa {
    // @Get(url="http://localhost:8083/test/getTime")
    @Post(url="http://61.145.96.90:60091/api/Vehiclefind/FindCarInfo",contentType = "application/json")
    String get(@Body(value = "PlateNo") String PlateNo);
}
