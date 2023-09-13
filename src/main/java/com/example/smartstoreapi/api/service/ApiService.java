package com.example.smartstoreapi.api.service;

import com.example.smartstoreapi.common.MailInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApiService {

    @Value("${clientId}")
    private String clientId;
    @Value("${clientSecret}")
    private String clientSecret;

    public List<MailInfo> getMailInfos(){
        Long timestamp = System.currentTimeMillis();
        String clientSecretSign = generateSignature(clientId, clientSecret, timestamp);
        System.out.println("clientSecretSign = " + clientSecretSign);

        String token = getToken(clientId, clientSecretSign, String.valueOf(timestamp), "SELF");
        System.out.println("token = " + token);

        String[] productOrderIds = getChangedOrders(token);

        List<MailInfo> mailInfos = getOrderDetails(token, productOrderIds);

        for(MailInfo mailInfo: mailInfos) {
            System.out.println("itemNo = " + mailInfo.getItemNo());
            System.out.println("mailInfo = " + extractEmails(mailInfo.getMailAddress()));
        }

        return mailInfos;

    }

    public String extractEmails(String text) {

        // 이메일 주소의 정규표현식 패턴
        String emailRegex = "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(text);

        if(matcher.find()) {
            String email = matcher.group();
            return email;
        }
        return "";
    }


    private List<MailInfo> getOrderDetails(String token, String[] productOrderIds) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String apiUrl = "https://api.commerce.naver.com/external/v1/pay-order/seller/product-orders/query";

        try {
            HttpPost httpPost = new HttpPost(apiUrl);

            httpPost.setHeader("Authorization", token);
            httpPost.setHeader("content-type", "application/json");


            JSONObject requestBodyJson = new JSONObject();
            requestBodyJson.put("productOrderIds", productOrderIds);

            String requestBody = requestBodyJson.toString();

            StringEntity requestEntity = new StringEntity(requestBody, "UTF-8");
            requestEntity.setContentType("application/json");
            httpPost.setEntity(requestEntity);

            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());

            JSONObject json = new JSONObject(responseBody);

            JSONArray orders =  json.getJSONArray("data");
            List<MailInfo> mailInfos = new ArrayList<>();

            for(int i=0; i<orders.length(); i++) {
                String itemNo = orders.getJSONObject(i).getJSONObject("productOrder").get("itemNo").toString();
                String mailAddress = orders.getJSONObject(i).getJSONObject("productOrder").get("shippingMemo").toString();
                mailInfos.add(new MailInfo(itemNo, mailAddress));
            }

            return mailInfos;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private String[] getChangedOrders(String token) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        String apiUrl = "https://api.commerce.naver.com/external/v1/pay-order/seller/product-orders/last-changed-statuses";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        // 현재 시간을 가져오고 포맷 적용
        ZonedDateTime currentTime = ZonedDateTime.now();

        // 하루 전 시간 계산
        ZonedDateTime yesterdayTime = currentTime.minusDays(1);

        // 포맷 적용
        String formattedYesterdayTime = yesterdayTime.format(formatter);

        try {
            HttpGet httpGet = new HttpGet(apiUrl);

            URI uri = new URIBuilder(httpGet.getURI()).addParameter("lastChangedFrom", formattedYesterdayTime)
                    .addParameter("lastChangedType", "PAYED") // 결제 완료된 구매정보만 가져오기
                    .build();
            httpGet.setURI(uri);
            httpGet.setHeader("Authorization", token);

            HttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());

            JSONObject json = new JSONObject(responseBody);

            JSONArray lastChangeStatuses = json.getJSONObject("data").getJSONArray("lastChangeStatuses");

            // productOrderId 값을 배열로 추출
            String[] productOrderIds = new String[lastChangeStatuses.length()];
            for (int i = 0; i < lastChangeStatuses.length(); i++) {
                productOrderIds[i] = lastChangeStatuses.getJSONObject(i).getString("productOrderId");
            }

            return productOrderIds;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
