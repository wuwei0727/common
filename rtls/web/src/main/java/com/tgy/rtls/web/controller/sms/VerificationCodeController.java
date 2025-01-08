package com.tgy.rtls.web.controller.sms;

import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.service.sms.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VerificationCodeController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    @PostMapping("/sendVerificationCode")
    public CommonResult<Object> sendVerificationCode(@RequestParam String phoneNumber) {
        return verificationCodeService.sendVerificationCode(phoneNumber);
    }

    @PostMapping("/verifyVerificationCode")
    public boolean verifyVerificationCode(@RequestParam String phoneNumber, @RequestParam String code) {
        return verificationCodeService.verifyVerificationCode(phoneNumber, code);
    }
}
