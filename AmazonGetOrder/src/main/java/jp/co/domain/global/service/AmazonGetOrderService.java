package jp.co.domain.global.service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazon.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.google.common.io.Files;

import jp.co.domain.global.common.Const;
import jp.co.domain.global.service.function.AmazonGetOrderExecSql;
import jp.co.domain.global.service.function.AmazonGetOrderManager;
import jp.co.domain.global.service.function.AmazonGetOrderSendMail;
import jp.co.domain.global.util.ApiAuthorization;
import jp.co.domain.global.util.MailModel;
import jp.co.domain.global.util.OrderNumberInfo;

/**
 * サービスクラス
 *
 * * --< CHANGE HISTORY >--
 *     X.XXXXXX    0000/00/00 ①XXXXXXXX
 */
@Service
public class AmazonGetOrderService {
  @Autowired
  AmazonGetOrderExecSql AmazonGetOrderExecSql;

  @Autowired
  AmazonGetOrderManager AmazonGetOrderManager;

  @Autowired
  AmazonGetOrderSendMail AmazonGetOrderSendMail;

  /**
   * API注文取得
   * 
   */
  public void getAmazonOrder(String[] args) throws IOException{
    System.setProperty("org.slf4j.simpleLogger.log.AMZ330Service.class","INFO"); // serviceクラスのみログレベル:INFOで出力
    Logger logger = LoggerFactory.getLogger(AmazonGetOrderService.class);
    
    OrderNumberInfo orderResult = new OrderNumberInfo();
    String eMessage = "";
    String AccessCmd = Const.ACCESS_PATH + " " + Const.AMZ030_LOCAL_PATH +" /cmd 7, 9";
    
    int storeWorkResult = 0;
    int storeLogResult = 0;
    int procFlg = 0;
    int hostNum = 0;
    int processTenpocd = 0;
    int tenpoPrm = 0;
    int periodPrm = 0;
    int deleteTempPrm = 1;
    int storeLogPrm = 2;
    int storeWorkPrm = 3;
    int orderItemResult = 0;
    boolean errorFlg = false;
    boolean sendMail = false;
    
    // 起動時間を検索範囲(TO)に使用
    LocalDateTime ldtNow = LocalDateTime.now();
    
/*--------------------------------------------------------------------------------------------------------*/
/* 引数取得                                                                                               */
/*--------------------------------------------------------------------------------------------------------*/
    try {
        tenpoPrm = Integer.parseInt(args[0]);
        periodPrm = Integer.parseInt(args[1]);
        logger.info("================================ START AmazonGetOrder SYSTEM ================================");
        logger.info("START PROCESSING WITH PRM :" + tenpoPrm + "," + periodPrm);
        logger.info(String.format("Host :") + getHostName());

        hostNum = Integer.parseInt(getHostName().replaceAll("[^0-9]", ""));
    } catch (NumberFormatException ex) {
        ex.printStackTrace();
        logger.error(String.format("ERROR EXISTS IN 【Get PRM】 PROCESS: %s", ex.toString()));
        System.exit(0);
      }
      
/*--------------------------------------------------------------------------------------------------------*/
/* 店舗情報取得                                                                                           */
/*--------------------------------------------------------------------------------------------------------*/
    List<ApiAuthorization> authList = AmazonGetOrderExecSql.getTenpoInfo(tenpoPrm);

    // 空の場合は処理を行わない
    if (authList.isEmpty()) {
        logger.error("Not Exists Tenpo Info.");
        logger.info("================================ FINISH AmazonGetOrder SYSTEM ================================");
        System.exit(0);
      }

      // 1店舗ずつ処理を行う
      for (ApiAuthorization apiAuth : authList) {
        logger.info(String.format("START PROCESSING TENPOCD :%s TENPONM :%s", apiAuth.getTenpocd(), apiAuth.getTenponm()));
        processTenpocd = Integer.parseInt(apiAuth.getTenpocd());
        // 認証情報作成
        LWAAuthorizationCredentials lwaAuthorizationCredentials = LWAAuthorizationCredentials.builder()
                                                                 .clientId(apiAuth.getClient_id())
                                                                 .clientSecret(apiAuth.getClient_secret())
                                                                 .refreshToken(apiAuth.getRefesh_token())
                                                                 .endpoint(Const.END_POINT_AUTH)
                                                                 .build();
        
/*--------------------------------------------------------------------------------------------------------*/
/* 一時データ削除(TEMP/WORK)                                                                              */
/*--------------------------------------------------------------------------------------------------------*/
        try{
            AmazonGetOrderExecSql.updateAPIData(deleteTempPrm
                                       ,hostNum
                                       ,Const.SYS_OID
                                       ,processTenpocd);

            logger.info("TempData Deleted.");
        } catch (Exception e) {
          // ログに内容記載
          logger.error(String.format("ERROR EXISTS Delete TempData PROCESS: %s", e.toString()));

          // 接続エラーでない場合は通知メッセージを作成
          if(escapeError(e)) {
            eMessage += createErrorMessage(apiAuth.getTenponm(), "一時データ削除(TEMP/WORK) ", e.toString());
            errorFlg = true;
          } else {
            logger.error("NETWORK ERROR.");
          }

          continue;
        }
        
/*--------------------------------------------------------------------------------------------------------*/
/* 注文データ取得(1),格納(TEMP)                                                                           */
/*--------------------------------------------------------------------------------------------------------*/
        try{
          orderResult = AmazonGetOrderManager.getOrder(hostNum
                                              ,processTenpocd
                                              ,lwaAuthorizationCredentials
                                              ,ldtNow
                                              ,periodPrm);

          logger.info(String.format("Orders TempData Inserted Count：%s",orderResult.getOrderCount()));
        } catch (Exception e) {
          // ログに内容記載
          logger.error(String.format("ERROR EXISTS IN getOrder(1) PROCESS: %s", e.toString()));

          // 接続エラーでない場合は通知メッセージを作成
          if(escapeError(e)) {
            eMessage += createErrorMessage(apiAuth.getTenponm(), "注文データ取得(1)", e.toString());
            errorFlg = true;
          } else {
            logger.error("NETWORK ERROR.");
          }

          continue;
        }

        // 注文件数が0の場合は処理を行わない
        if (orderResult.getOrderCount() <= 0) {
          logger.warn("Not Exists Order...The Store Process Skip.");
          continue;
        }
        
        if(orderResult.getOrderNumberList().size()>0) {
/*--------------------------------------------------------------------------------------------------------*/
/* 注文データ取得(2),格納(TEMP)                                                                           */
/*--------------------------------------------------------------------------------------------------------*/
          try{
            orderItemResult = AmazonGetOrderManager.getOrderItems(hostNum
                                                         ,processTenpocd
                                                         ,lwaAuthorizationCredentials
                                                         ,orderResult.getOrderNumberList());

            logger.info(String.format("Items TempData Inserted Count：%s",orderItemResult));
          } catch (Exception e) {
            // ログに内容記載
            logger.error(String.format("ERROR EXISTS IN getOrder(2) PROCESS: %s", e.toString()));
            
            // 接続エラーでない場合は通知メッセージを作成
            if(escapeError(e)) {
              eMessage += createErrorMessage(apiAuth.getTenponm(), "注文データ取得(2)", e.toString());
              errorFlg = true;
            } else {
              logger.error("NETWORK ERROR.");
            }
          }
        
/*--------------------------------------------------------------------------------------------------------*/
/* 注文データ格納(LOG)                                                                                    */
/*--------------------------------------------------------------------------------------------------------*/
          try{
              storeLogResult = AmazonGetOrderExecSql.updateAPIData(storeLogPrm
                                                          ,hostNum
                                                          ,Const.SYS_OID
                                                          ,processTenpocd);
  
              logger.info(String.format("LogData Inserted Count：%s",storeLogResult));
          } catch (Exception e) {
            // ログに内容記載
            logger.error(String.format("ERROR EXISTS IN Store API Data PROCESS: %s", e.toString()));
  
            // 接続エラーでない場合は通知メッセージを作成
            if(escapeError(e)) {
              eMessage += createErrorMessage(apiAuth.getTenponm(), "注文データ格納(LOG)", e.toString());
              errorFlg = true;
            } else {
              logger.error("NETWORK ERROR.");
            }
  
            continue;
          }
        
/*--------------------------------------------------------------------------------------------------------*/
/* 注文データ格納(WORK)                                                                                   */
/*--------------------------------------------------------------------------------------------------------*/
          try{
              storeWorkResult = AmazonGetOrderExecSql.updateAPIData(storeWorkPrm
                                                           ,hostNum
                                                           ,Const.SYS_OID
                                                           ,processTenpocd);
  
              logger.info(String.format("WorkData Inserted Count：%s",storeWorkResult));
          } catch (Exception e) {
            // ログに内容記載
            logger.error(String.format("ERROR EXISTS IN Store Work Data PROCESS: %s", e.toString()));
  
            // 接続エラーでない場合は通知メッセージを作成
            if(escapeError(e)) {
              eMessage += createErrorMessage(apiAuth.getTenponm(), "注文データ格納(WORK)", e.toString());
              errorFlg = true;
            } else {
              logger.error("NETWORK ERROR.");
            }
  
            continue;
          }
        }
        
/*--------------------------------------------------------------------------------------------------------*/
/* エラーメッセージ取得                                                                                   */
/*--------------------------------------------------------------------------------------------------------*/
        // エラーメッセージが存在する場合、メッセージを送信
        if(!AmazonGetOrderManager.systemError.isEmpty()){
          errorFlg = true;
          
          for (String err : AmazonGetOrderManager.systemError) {
            //eMessage = eMessage + err + "\r\n";
            eMessage = eMessage + createErrorMessage(apiAuth.getTenponm(), "API処理", err);
          }
        }

/*--------------------------------------------------------------------------------------------------------*/
/* 日次処理確認                                                                                           */
/*--------------------------------------------------------------------------------------------------------*/
        try {
          procFlg = AmazonGetOrderExecSql.checkDailyProcess(0);
          
          // 日次処理中の場合は処理を行わない
          if(procFlg < 12) {
            logger.warn("Currently Daily processing...AMZ030RP(TASK) Cannot Run.");
            System.exit(0);
          } else {
            logger.info("Daily Processing Completed...Run AMZ030RP(TASK).");        
          }
          
        } catch (Exception e) {
          e.printStackTrace();
          logger.error(String.format("ERROR EXISTS CHECK DailyProcessing PROCESS: %s", e.toString()));
          System.exit(0);
        }

/*--------------------------------------------------------------------------------------------------------*/
/* 受注データ変換プログラム(Access)実行                                                                           */
/*--------------------------------------------------------------------------------------------------------*/
        try{
          File rocalFile = new File(Const.AMZ030_LOCAL_PATH);
          File fixFile = new File(Const.AMZ030_FIX_PATH);
          Runtime run = Runtime.getRuntime();
          Process p = run.exec(AccessCmd);

          // ローカルファイルを削除
          rocalFile.delete();
          // 最新PGをコピー
          Files.copy(fixFile,rocalFile);
          // Access実行
          p.waitFor();
          
          logger.info("AMZ030RP(TASK) is Completed.");
        } catch (Exception e) {
          logger.error(String.format("ERROR EXISTS AMZ030RP(TASK) PROCESS: %s", e.toString()));
        } 

/*--------------------------------------------------------------------------------------------------------*/
/* メッセージ送信                                                                                         */
/*--------------------------------------------------------------------------------------------------------*/
        if(errorFlg == true){
          sendMail = sendMail(eMessage);
    
          if (sendMail == true) {
            logger.info("Success Send Message.");
          } else {
            logger.error("FAIL SEND MESSAGE.");
          }
        }
      }
    
/*--------------------------------------------------------------------------------------------------------*/
    logger.info("================================ FINISH AmazonGetOrder SYSTEM ================================");
    System.exit(0);
  }

  
/*--------------------------------------------------------------------------------------------------------*/
/* ユーティリティ関数                                                                                     */
/*--------------------------------------------------------------------------------------------------------*/
  /* 通知有無判定 */
  private boolean escapeError(Exception e) {
    boolean eResult;
    
    if(!(e instanceof java.net.ConnectException) &&                     // ポート接続ができなかった場合
       !(e instanceof java.net.UnknownHostException) &&                 // ホストIPのDNS解決ができなかった場合
       !(e instanceof java.net.SocketTimeoutException) &&               // 通信がタイムアウトした場合
       !(e instanceof javax.net.ssl.SSLProtocolException) &&            // SSL通信が確立されなかった場合 
       !(e instanceof javax.net.ssl.SSLHandshakeException)){            // ハンドシェイクが確立されなかった場合
      /* 上記以外のエラーはシステムエラーとして通知する */
      eResult = true;
    } else {
      /* 上記通信エラーの場合は通知を送らない */
      eResult = false;
    }
    return eResult;
  }
  
  /* メッセージ送信 */
  private boolean sendMail(String message) {
    boolean sendResult =false;
    MailModel mailModel = new MailModel();
    
    mailModel.setMailFrom(Const.MAIL_FROM);
    mailModel.setMailTo(Const.MAIL_TO);              // 送信先メールアドレス
    mailModel.setMailCc("");
    mailModel.setMailSubject(Const.MAIL_SUBJECT);   // タイトル
    mailModel.setMailText(message); // 本文
    
    try {
      sendResult = AmazonGetOrderSendMail.sendMail(mailModel);
    } catch (Exception e) {
      e.printStackTrace();
      Logger logger = LoggerFactory.getLogger(AmazonGetOrderService.class);
      logger.error("ERROR SEND MESSAGE TO MAIL: %s", e.toString());
    }
    
    return sendResult;
  }

  /* エラーメッセージ作成 */
  private String createErrorMessage(String temponm, String process, String eMessage)throws IOException {
    String Message           = "";
    String normalMessage     = "以下店舗の"+ process +"時にエラーが発生しました。";
    String tenpoMessage      = "店舗：" + temponm;
    String hostMessage       = "端末：" + getHostName();
    String eCode             = "ERROR CODE: " + eMessage;

    Message = normalMessage + tenpoMessage + hostMessage + eCode + "\r\n";

    return Message;
  }
  
  /* 実行端末名取得 */
  private static String getHostName() {
    try {
        return InetAddress.getLocalHost().getHostName();
    }catch (Exception e) {
      e.printStackTrace();
      Logger logger = LoggerFactory.getLogger(AmazonGetOrderService.class);
      logger.error("ERROR GET HOST NAME: %s", e.toString());
    }
    return "UnknownHost";
  }
/*--------------------------------------------------------------------------------------------------------*/

}
