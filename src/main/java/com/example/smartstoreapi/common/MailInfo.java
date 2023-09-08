package com.example.smartstoreapi.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
public class MailInfo {
    String itemNo;
    String mailAddress;

    public MailInfo(String itemNo, String mailAddress) {
        this.itemNo = itemNo;
        this.mailAddress = mailAddress;
    }
}
