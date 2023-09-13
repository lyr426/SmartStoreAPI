package com.example.smartstoreapi.api.controller;

import com.example.smartstoreapi.api.service.ApiService;
import com.example.smartstoreapi.common.MailInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/buy")
public class ApiController {
    private final ApiService apiService;
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/")
    public ResponseEntity getApi () throws IOException {
        List<MailInfo> mailInfos = apiService.getMailInfos();

        return ResponseEntity.ok().body(mailInfos);
    }



}
