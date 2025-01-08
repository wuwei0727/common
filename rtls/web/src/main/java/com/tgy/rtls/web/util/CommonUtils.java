package com.tgy.rtls.web.util;

import com.tgy.rtls.data.entity.park.ParkingPlace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2023-03-17 20:33
 * @Description: TODO
 * @Version: 1.0
 */
public class CommonUtils {
    /**
     * list转object
     * @param list
     * @return
     */
    public static Object speechless(List<Object> list) {
        return list = list.stream().distinct().collect(Collectors.toList());
    }

    /**
     * object转list对象
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> castList(Object obj, Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        if(obj instanceof List<?>)
        {
            for (Object o : (List<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    public static List<Object> convertParkingPlaceList(ArrayList<ParkingPlace> parkingPlaces) {
        return parkingPlaces.stream()
                .map(p -> (Object) p)
                .collect(Collectors.toList());
    }

    /**
     * 集合转对象
     * @param collection 集合
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(Collection<T> collection) {
        return new ArrayList<>(collection);
    }

    /**
     * 数组转对象
     * @param array
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(T[] array) {
        return Arrays.stream(array)
                .collect(Collectors.toList());
    }
}
