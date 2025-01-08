package com.tgy.rtls.data.service.view;

import com.tgy.rtls.data.entity.view.ViewVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface ViewService {
    Map<String,Object> getAllUserInfo(String month);

    CompletableFuture<List<ViewVo>> getUseCarFrequency() throws ExecutionException, InterruptedException;

}
