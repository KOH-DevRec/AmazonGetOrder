package jp.co.domain.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jp.co.domain.global.controller.AmazonGetOrderController;

@SpringBootApplication
public class AmazonGetOrderApplication implements CommandLineRunner {

  @Autowired
  AmazonGetOrderController AmazonGetOrderController;
  
	public static void main(String[] args) {
		SpringApplication.run(AmazonGetOrderApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
	  AmazonGetOrderController.getAmazonOrder(args);
	  }

}