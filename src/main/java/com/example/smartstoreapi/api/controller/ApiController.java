package com.example.smartstoreapi.api.controller;

import com.example.smartstoreapi.api.service.ApiService;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/buy")
public class ApiController {
    private final ApiService apiService;
    @Value("${clientId}")
    private String clientId;
    @Value("${clientSecret}")
    private String clientSecret;
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/")
    public String getApi () throws IOException {

        Long timestamp = System.currentTimeMillis();
        String clientSecretSign = generateSignature(clientId, clientSecret, timestamp);

        System.out.println("clientSecretSign = " + clientSecretSign);
        OkHttpClient client = new OkHttpClient();


//        Request request = new Request.Builder()
//                .url("https://api.commerce.naver.com/external/v1/oauth2/token")
//                .get()
//                .build();
//
//        Response response = client.newCall(request).execute();

        return clientId;
    }

    public static String generateSignature(String clientId, String clientSecret, Long timestamp) {
        // 밑줄로 연결하여 password 생성
        String password = StringUtils.joinWith("_", clientId, timestamp);
        // bcrypt 해싱
        String hashedPw = BCrypt.hashpw(password, clientSecret);
        // base64 인코딩
        return Base64.getUrlEncoder().encodeToString(hashedPw.getBytes(StandardCharsets.UTF_8));
    }

}
