package com.example.smartstoreapi.mail.service;

import com.example.smartstoreapi.common.MailInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

@Service
public class MailService {

    @Value("${mailAddress}")
    private String mailAddress;
    @Value("${mailPassword}")
    private String mailPassword;
    public String sendMail(MailInfo mailInfo){

        final String bodyEncoding = "UTF-8"; //콘텐츠 인코딩

        String subject = "메일 발송 테스트";
        String fromEmail = mailAddress;
        String fromUsername = "재미져스토어";
        String toEmail = mailInfo.getMailAddress(); // 콤마(,)로 여러개 나열

        // 메일에 출력할 텍스트
        StringBuffer sb = new StringBuffer();
        sb.append("<h3>안녕하세요</h3>\n");
        sb.append("<h4>재미져스토어 입니다. 학습지 구매 감사합니다.</h4>\n");
        String html = sb.toString();

        // 메일 옵션 설정
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");

        props.put("mail.smtp.quitwait", "false");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        try {
            // 메일 서버  인증 계정 설정
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailAddress, mailPassword);
                }
            };

            // 메일 세션 생성
            Session session = Session.getInstance(props, auth);

            // 메일 송/수신 옵션 설정
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromUsername));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(toEmail, false));
            message.setSubject(subject);
            message.setSentDate(new Date());

            // 메일 콘텐츠 설정
            Multipart mParts = new MimeMultipart();
            MimeBodyPart mTextPart = new MimeBodyPart();

            // 첨부파일 작성
            MimeBodyPart mFilePart = new MimeBodyPart();
            DataSource source = new FileDataSource("/Users/leeyuri/smartstoreApi/src/main/resources/jaemijyeoFile/1029393.pdf");
            mFilePart.setDataHandler(new DataHandler(source));

            //파일명칭이 깨지지 않도록 조치를 취함
            try {
                mFilePart.setFileName(MimeUtility.encodeText(source.getName(), "euc-kr","B"));
                mParts.addBodyPart(mFilePart);

            } catch (UnsupportedEncodingException e) {
                System.out.println("파일 endcode 에러 발생");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }


            // 메일 콘텐츠 - 내용
            mTextPart.setText(html, bodyEncoding, "html");
            mParts.addBodyPart(mTextPart);

            // 메일 콘텐츠 설정
            message.setContent(mParts);

            // MIME 타입 설정
            MailcapCommandMap MailcapCmdMap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            MailcapCmdMap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            MailcapCmdMap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            MailcapCmdMap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            MailcapCmdMap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            MailcapCmdMap.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(MailcapCmdMap);

            // 메일 발송
            Transport.send( message );

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return "";
    }
}

