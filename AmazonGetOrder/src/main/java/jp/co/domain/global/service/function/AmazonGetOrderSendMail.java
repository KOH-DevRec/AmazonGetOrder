package jp.co.domain.global.service.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import jp.co.domain.global.util.MailModel;
@Component
public class AmazonGetOrderSendMail {
      Logger logger = LoggerFactory.getLogger(AmazonGetOrderSendMail.class);

  private final MailSender mailSender;

  public AmazonGetOrderSendMail(MailSender mailSender) {
    this.mailSender = mailSender;
  }

  public boolean sendMail(MailModel mailModel) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(mailModel.getMailFrom()); // 送信元メールアドレス
    msg.setTo(mailModel.getMailTo()); // 送信先メールアドレス
    
    if(!mailModel.getMailCc().equals("")) {
      msg.setCc(mailModel.getMailCc()); // Cc用
    }
    //    msg.setBcc();               // Bcc用
    msg.setSubject(mailModel.getMailSubject()); // タイトル
    msg.setText(mailModel.getMailText()); // 本文

    try {
      mailSender.send(msg);
    } catch (MailException e) {
      e.printStackTrace();
      logger.error(String.format("ERROR EXISTS IN SEND MAIL PROCESS: %s", e.toString()));
    }
    return true;
  }
}
