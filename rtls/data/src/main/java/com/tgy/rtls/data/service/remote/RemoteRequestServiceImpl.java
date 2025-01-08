package com.tgy.rtls.data.service.remote;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.tgy.rtls.data.entity.common.CommonResult;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.remote
 * @Author: wuwei
 * @CreateTime: 2024-06-16 19:04
 * @Description: TODO
 * @Version: 1.0
 */
public interface RemoteRequestServiceImpl {
    @Backend("okhttp3")
    @Address(host = "192.168.1.95", port = "39621")
    @Post(value = "{url}")
    CommonResult<Object> checkAndCall(@Var("url") String url);


    @Post("http://61.145.96.90:60091/api/Vehiclefind/FindCarInfo")
    ForestResponse<String> findCarInfo(@JSONBody String PlateNo);


}
