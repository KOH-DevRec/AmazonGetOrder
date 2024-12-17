package jp.co.domain.global.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import jp.co.domain.global.service.AmazonGetOrderService;
/**
 *
 * コントローラー
 *
 * * --< CHANGE HISTORY >--
 *   X.XXXXXX  0000/00/00 ①XXXXXXXX
 */
@Controller
public class AmazonGetOrderController {
  
  @Autowired
  AmazonGetOrderService AmazonGetOrderService;
  

  @SuppressWarnings("javadoc")
  public void getAmazonOrder (String[] args) throws IOException {
    AmazonGetOrderService.getAmazonOrder(args);
  }
}
