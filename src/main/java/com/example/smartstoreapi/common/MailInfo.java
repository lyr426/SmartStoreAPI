package com.example.smartstoreapi.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
public class MailInfo {
    private String itemNo;


    private String mailAddress;

    public MailInfo(){}

    public MailInfo(String itemNo, String mailAddress) {
        this.itemNo = itemNo;
        this.mailAddress = mailAddress;
    }
    @Override
    public String toString() {
        return "MailInfo{" +
                "itemNo='" + itemNo + '\'' +
                ", mailAddress='" + mailAddress + '\'' +
                '}';
    }


}
