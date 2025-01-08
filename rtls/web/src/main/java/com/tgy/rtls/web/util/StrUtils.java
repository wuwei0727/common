package com.tgy.rtls.web.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2024-07-16 16:07
 * @Description: TODO
 * @Version: 1.0
 */
public class StrUtils {
    public static List<Integer> convertStringToList(String ids) {
        return Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
