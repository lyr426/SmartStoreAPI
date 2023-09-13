package com.example.smartstoreapi.mail.controller;

import com.example.smartstoreapi.common.MailInfo;
import com.example.smartstoreapi.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    MailService mailService = new MailService();
    @PostMapping("/")
    public ResponseEntity sendMail(@RequestBody MailInfo mailInfo) throws IOException {

        String result = mailService.sendMail(mailInfo);

        return ResponseEntity.ok().body(result);
    }

}
