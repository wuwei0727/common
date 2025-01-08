package com.tgy.rtls.data.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeReader implements ObjectReader<LocalDateTime> {

	public static final LocalDateTimeReader INSTANCE = new LocalDateTimeReader();

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public LocalDateTime readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        String dateTimeStr = jsonReader.readString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateTime validDateTime;
        try {
            validDateTime =  LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            throw new RuntimeException(String.format("时间格式有误，请检查时间格式：%s", DATE_PATTERN));
        }
        return validDateTime;
    }
}