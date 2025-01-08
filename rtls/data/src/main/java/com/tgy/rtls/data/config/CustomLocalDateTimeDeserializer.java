package com.tgy.rtls.data.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;

public class CustomLocalDateTimeDeserializer implements ObjectDeserializer {

    @Override
    public LocalDateTime deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Map<String, Object> map = parser.parseObject(Map.class);
        if (map != null) {
            int year = TypeUtils.castToInt(map.get("year"));
            int month = TypeUtils.castToInt(map.get("monthValue"));
            int dayOfMonth = TypeUtils.castToInt(map.get("dayOfMonth"));
            int hour = TypeUtils.castToInt(map.get("hour"));
            int minute = TypeUtils.castToInt(map.get("minute"));
            int second = TypeUtils.castToInt(map.get("second"));
            return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}