package com.tgy.rtls.data.service.common.impl;

import com.tgy.rtls.data.service.common.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Service("mail")
public class MailServiceImp implements MailService {
    @Autowired(required = false)
    private JavaMailSender mailSender;

    private String from;

    //TODO 设置发送邮件重载方式



    @Override
    public Map<String, Object> sendAttachmentsMail(String from,String to, String subject, String content, String[] filePath) {

        System.out.println(filePath);
        MimeMessage message = mailSender.createMimeMessage();
        Map<String, Object> map = new HashMap<>();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
//            FileSystemResource file = new FileSystemResource(new File(filePath));
            // 发送附件
            if(filePath!=null) {
                for (int i = 0; i < filePath.length; i++) {
                    File file = new File(filePath[i]);
                    file = ResourceUtils.getFile(file.getAbsolutePath());
                    String fileName = filePath[i].substring(filePath[i].lastIndexOf(File.separator));
//            String fileName = filePath.substring(filePath.lastIndexOf("/"));
//            String fileName = "附件";
                    System.out.println(fileName);
                    //添加多个附件可以使用多条 helper.addAttachment(fileName,file);
                    //helper.addAttachment(fileName,file);
                    helper.addAttachment(fileName, file);
                }
            }
            mailSender.send(message);
            map.put("code", 1);
            map.put("message", "发送成功");
            return map;
        } catch (MessagingException e) {
          //  logger.error(ExceptionUtils.getFullStackTrace(e));  // 记录错误信息
            e.printStackTrace();
            map.put("code",0);
            map.put("message","发送失败");
            return map;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            map.put("code",-1);
            map.put("message","没有文件");
            return map;
        } finally {
        }


    }
}
