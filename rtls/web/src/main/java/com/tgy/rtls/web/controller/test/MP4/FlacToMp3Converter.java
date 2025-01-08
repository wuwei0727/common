package com.tgy.rtls.web.controller.test.MP4;

import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import java.io.File;

public class FlacToMp3Converter {
    public static void main(String[] args) {
        // 指定FLAC文件所在的目录
        String sourceDirPath = "D:\\flac";
        String savePath="D:\\flac\\mp3";
        File sourceDir = new File(sourceDirPath);
        File saveDir = new File(savePath);

        // 如果保存目录不存在，创建它
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }


        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.out.println("指定的目录不存在或不是一个目录。");
            return;
        }

        // 获取目录下所有的FLAC文件
        File[] flacFiles = sourceDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".flac"));

        if (flacFiles == null || flacFiles.length == 0) {
            System.out.println("指定目录下没有找到FLAC文件。");
            return;
        }

        // 遍历所有FLAC文件并转换为MP3
        for (File flacFile : flacFiles) {
            String mp3FileName = flacFile.getName().replace(".flac", "").replace("_", " ") + ".mp3";
            File target = new File(saveDir, mp3FileName);

            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(128000);
            audio.setChannels(2);
            audio.setSamplingRate(48000);

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("mp3");
            attrs.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            try {
                encoder.encode(new MultimediaObject(flacFile), target, attrs);
                System.out.println("转换成功：" + flacFile.getName() + " -> " + mp3FileName);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("转换失败：" + flacFile.getName() + "，错误信息：" + e.getMessage());
            }
        }
    }
}