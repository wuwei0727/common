package com.tgy.rtls.data.service.sinopec.impl;


import com.tgy.rtls.data.service.sinopec.HttpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpClientServiceImp implements HttpClientService {
    public static final String BOS_MANAGEMENT_HOST = "http://192.168.1.95:10087/";
    public static final String CRM_MANAGEMENT_HOST = "http://192.168.1.95:10087/";
    private static final String BOS_MANAGEMENT_CONTEXT = "/bos_management";
    private static final String CRM_MANAGEMENT_CONTEXT = "/crm_management";
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public void   sendWarningToSinopec(String url, String message) {
        // 使用HttpClient调用 远程接口
        String url1 = CRM_MANAGEMENT_HOST + "/UWB/record/getSubSel";
        ResponseEntity<String> result = restTemplate.getForEntity(url1, String.class);
        HttpStatus statusCode = result.getStatusCode();
        String body = result.getBody();


        // ResponseEntity<>(body,statusCode);


    }
}
