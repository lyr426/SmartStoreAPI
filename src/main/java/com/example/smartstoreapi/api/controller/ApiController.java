package com.example.smartstoreapi.api.controller;

import com.example.smartstoreapi.api.service.ApiService;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
        String token = getToken(clientId, clientSecretSign, String.valueOf(timestamp), "SELF");
        System.out.println("token = " + token);

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

    public static String getToken( String clientId, String client_secret_sign, String timestamp, String type_) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String apiUrl = "https://api.commerce.naver.com/external/v1/oauth2/token";

        try {
            HttpPost httpPost = new HttpPost(apiUrl);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("timestamp", timestamp));
            params.add(new BasicNameValuePair("client_secret_sign", client_secret_sign));
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("type", type_));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());

            JSONObject json = new JSONObject(responseBody);

            while (!json.has("access_token")) {
                System.out.println("[" + responseBody + "] 토큰 요청 실패");
                Thread.sleep(1000); // 1초 대기 후 재시도
                response = httpClient.execute(httpPost);
                responseBody = EntityUtils.toString(response.getEntity());
                json = new JSONObject(responseBody);
            }

            return json.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
