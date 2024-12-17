package jp.co.domain.global.service.function;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.amazon.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.amazon.SellingPartnerAPIAA.RateLimitConfiguration;
import com.amazon.SellingPartnerAPIAA.RateLimitConfigurationOnRequests;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.OrdersV0Api;
import io.swagger.client.model.BuyerInfo;
import io.swagger.client.model.GetOrderAddressResponse;
import io.swagger.client.model.GetOrderBuyerInfoResponse;
import io.swagger.client.model.GetOrderItemsResponse;
import io.swagger.client.model.GetOrdersResponse;
import io.swagger.client.model.MarketplaceTaxInfo;
import io.swagger.client.model.Money;
import io.swagger.client.model.Order;
import io.swagger.client.model.OrderItem;
import io.swagger.client.model.OrderItemList;
import io.swagger.client.model.OrderList;
import jp.co.domain.global.common.Const;
import jp.co.domain.global.util.OrderNumberInfo;

/**
 * マネージャークラス
 *
 * * --< CHANGE HISTORY >--
 *     X.XXXXXX    0000/00/00 ①XXXXXXXX
 */
@Component
public class AmazonGetOrderManager {

  
  @Autowired
  ResourceLoader resourceLoader;

  @Autowired
  AmazonGetOrderExecSql AmazonGetOrderExecSql;

  Logger logger = LoggerFactory.getLogger(AmazonGetOrderManager.class);

  public String responseJson = "";
  public List<String> systemError = new ArrayList<String>();

   /**
   * 注文データ取得(1)
   *
   * @param apiAuth API認証情報
   * @param ldtNow  バッチ実行時間
   * @return 注文リスト
   */
  @SuppressWarnings("unused")
  public OrderNumberInfo getOrder(int cid, int tenpocd, LWAAuthorizationCredentials lwaAuthorizationCredentials, LocalDateTime ldtNow, int period)throws IOException, InterruptedException {
    List<String> orderNumberList = new ArrayList<>();
    List<String> getNumberList = new ArrayList<>();
    List<String> importedNumberList = new ArrayList<>();
    List<OrderList> orderLists = new ArrayList<>();
    List<Order> getOrderList = new ArrayList<>();
    List<Order> unimporedOrderList = new ArrayList<>();
    List<Integer> storeList = new ArrayList<>();
    OrderNumberInfo numList = new OrderNumberInfo();
    Collection<String> unimportedNumberList = null;
    ApiResponse<GetOrdersResponse> apiResO = null;
    ApiResponse<GetOrderAddressResponse> apiResA = null;
    ApiResponse<GetOrderBuyerInfoResponse> apiResB = null;
    GetOrderAddressResponse  address = new GetOrderAddressResponse();
    GetOrderBuyerInfoResponse  buyerInfo = new GetOrderBuyerInfoResponse();
    GetOrdersResponse orderRes = new GetOrdersResponse();
    String nextToken = "";
    int orderCount = 0;
    int lastOrderCount = 0;
    long waitTime = 1000;
    double dRateO = 0;
    double dRateA = 0;
    double dRateB = 0;
    double defaultRate = 1.0;
    
    List<String> marketplaceIds = new ArrayList<String>();
    marketplaceIds.add(Const.MAKET_PLACE_ID);
    
    OrdersV0Api ordersApi = new OrdersV0Api.Builder()
                           .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                           .endpoint(Const.END_POINT_ORDER)
                           .build();
    
    OrdersV0Api orderAddressApi = new OrdersV0Api.Builder()
        .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
        .endpoint(Const.END_POINT_ORDER)
        .build();
    
    OrdersV0Api orderBuyerApi = new OrdersV0Api.Builder()
        .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
        .endpoint(Const.END_POINT_ORDER)
        .build();
    
    // 現在日時を[引数]時間減算し、フォーマットをISO8601形式にする
    String reqDate = ldtNow.minusHours(period).format(Const.dFormatter);

    // TODO NOTE:ステータスを指定する場合、以下を利用
    /*-----------------------------------------*/
    List<String> statusList = new ArrayList<String>();
    //statusList.add("PendingAvailability"); //予約商品
    //statusList.add("Pending");  //注文済み、未払い
    statusList.add("Unshipped");  //支払済み
    //statusList.add("PartiallyShipped");  //一部出荷済み ※24.9現行のSP-APIでは「Unshipped」しか入ってきません...
    //statusList.add("Shipped");  //出荷済み
    //statusList.add("Canceled");  //キャンセル
    //statusList.add("Unfulfillable"); //出荷不可
    /*-----------------------------------------*/

    try{      
      //TODO NOTE:429エラーが頻発するなら下記コードの値を変更し制御
      //ordersApi = setDRate(lwaAuthorizationCredentials,1.0,waitTime);
      
      // 該当注文が無くなるまでループ
      while(orderCount == 0 || nextToken != null){
        // リクエストパラメータ設定
        orderRes = ordersApi.getOrders(marketplaceIds
                                      ,""  // 注文作成日
                                      ,""
                                      ,reqDate  // 注文更新日
                                      ,""
                                      ,statusList
                                      ,Collections.emptyList()
                                      ,Collections.emptyList()
                                      ,""
                                      ,""
                                      ,null
                                      ,Collections.emptyList()
                                      ,Collections.emptyList()
                                      ,nextToken
                                      ,Collections.emptyList()
                                      ,""
                                      ,null
                                      ,""
                                      ,""
                                      ,""
                                      ,""
                                      ,"");

        // 各レスポンス値格納
        orderLists.add(orderRes.getPayload().getOrders());
        nextToken = orderRes.getPayload().getNextToken();
        orderCount += orderRes.getPayload().getOrders().size();
        lastOrderCount = orderRes.getPayload().getOrders().size();
        
        /*------------------------------------------------------------------------------------*/
        // 次ループのリクエスト用データモデル作成
        if(apiResO==null) {
          apiResO = ordersApi.getOrdersWithHttpInfo(marketplaceIds
                                                   ,""  // 注文作成日
                                                   ,""
                                                   ,reqDate  // 注文更新日
                                                   ,""
                                                   ,statusList
                                                   ,Collections.emptyList()
                                                   ,Collections.emptyList()
                                                   ,""
                                                   ,""
                                                   ,null
                                                   ,Collections.emptyList()
                                                   ,Collections.emptyList()
                                                   ,nextToken
                                                   ,Collections.emptyList()
                                                   ,""
                                                   ,null
                                                   ,""
                                                   ,""
                                                   ,""
                                                   ,""
                                                   ,"");
          
          dRateO = Double.parseDouble(apiResO.getHeaders().get("x-amzn-RateLimit-Limit").get(0));
        }

        ordersApi = setDRate(lwaAuthorizationCredentials,dRateO,waitTime);     
        
        // エラーの場合、ログに内容記載
        if (orderRes.getErrors() != null){
          logger.error(orderRes.getErrors().toString());
          
          // エラーによりnextTokenの値が不正だった場合、whileループを抜けられないためbreak処理追加
          break;
        }
        /*------------------------------------------------------------------------------------*/    
      }
      logger.info(String.format("OrderCount：%s",Integer.toString(orderCount)));
      
      if(orderCount != 0) {
        // 取得IDリスト作成
        for(OrderList orderList: orderLists){
          for(Order order: orderList){ 
            getNumberList.add(order.getAmazonOrderId());
            getOrderList.add(order);
          }
        }
        
        // 取込済みID取得
        importedNumberList = AmazonGetOrderExecSql.getImportedId(4
                                                        ,cid
                                                        ,Const.SYS_OID
                                                        ,tenpocd);
        // 取得IDから取込済みIDを除外
        unimportedNumberList = CollectionUtils.subtract(getNumberList, importedNumberList);
        logger.info(String.format("UnimportedCount：%s",Integer.toString(unimportedNumberList.size())));
        // 未取込IDのみのorderList作成
        for(Order order:getOrderList) {
          for(String id : unimportedNumberList){
            if(order.getAmazonOrderId().equals(id)) {
              unimporedOrderList.add(order);
              break;
            }
          }
        }
        /*------------------------------------------------------------------------------------*/        
        ordersApi = setDRate(lwaAuthorizationCredentials,1.0,waitTime);
        
        // D-Rate取得・設定
        // 最新の値が必要な為、最後に取得された注文IDを利用
        // TODO NOTE:ここで「ArrayIndexOutOfBoundsException」が起こる=動的レートが取得できない時間がある。頻度は約1回/日
        apiResA = ordersApi.getOrderAddressWithHttpInfo(orderLists.get(orderLists.size() - 1).get(lastOrderCount -1).getAmazonOrderId());
        apiResB = ordersApi.getOrderBuyerInfoWithHttpInfo(orderLists.get(orderLists.size() - 1).get(lastOrderCount -1).getAmazonOrderId());        
        dRateA = Double.parseDouble(apiResA.getHeaders().get("x-amzn-RateLimit-Limit").get(0));
        dRateB = Double.parseDouble(apiResB.getHeaders().get("x-amzn-RateLimit-Limit").get(0));
                
        /*-----------------------------------------------------------------------------------------*/
        logger.info(String.format("OrdersRate：%s",dRateO));
        logger.info(String.format("AddressRate：%s",dRateA));
        logger.info(String.format("BuyerRate：%s",dRateB));        
        /*-----------------------------------------------------------------------------------------*/ 
        
        //TODO NOTE:レート値1.0以下は429エラーが発生する為、デフォルト値で対応
        if(dRateA < 1.0) {
          logger.info(String.format("Set DefaultRate To AddressRate."));
          orderAddressApi = setDRate(lwaAuthorizationCredentials,defaultRate,waitTime); 
        } else {
          orderAddressApi = setDRate(lwaAuthorizationCredentials,dRateA,waitTime); 
        }
        
        if(dRateB < 1.0) {
          logger.info(String.format("Set DefaultRate To BuyerInfoRate."));
          orderBuyerApi = setDRate(lwaAuthorizationCredentials,defaultRate,waitTime);
        } else {
          orderBuyerApi = setDRate(lwaAuthorizationCredentials,dRateB,waitTime); 
        }
       
      }
    } catch (ApiException e){
      // ログに内容記載
      e.printStackTrace();
      logger.error(String.format("API Exception Code: %s %s", e.getCode(),e.getMessage()));
      logger.error(String.format("API Response: %s", e.getResponseHeaders().toString()));
      // 429:Too Many Requestsなら10秒処理停止
      if(e.getCode() == 429) {
        Thread.sleep(10000);
      }
    } catch (Exception e){
      // ログに内容記載
      e.printStackTrace();
      logger.error(String.format("getOrder Exception: %s", e.toString()));
      
      if(!(e instanceof java.lang.ArrayIndexOutOfBoundsException)){
        logger.error(String.format("Unable To get Rate Value...Set DefaultRate To AddressRate."));
        orderAddressApi = setDRate(lwaAuthorizationCredentials,defaultRate,waitTime); 
        orderBuyerApi = setDRate(lwaAuthorizationCredentials,defaultRate,waitTime);
      } else {
        systemError.add(e.toString()); 
      }
    }
      // TEMPデータ格納・注文番号リスト作成
        for(Order order: unimporedOrderList){ 
          try {
            // 住所情報取得     
            address = orderAddressApi.getOrderAddress(order.getAmazonOrderId());
            order.setShippingAddress(address.getPayload().getShippingAddress());
            
            // 購入者情報取得       
            buyerInfo = orderBuyerApi.getOrderBuyerInfo(order.getAmazonOrderId());
            BuyerInfo storeBuyerInfo = new BuyerInfo();
            storeBuyerInfo.setBuyerEmail(buyerInfo.getPayload().getBuyerEmail());
            storeBuyerInfo.setBuyerName(buyerInfo.getPayload().getBuyerName());
            storeBuyerInfo.setBuyerCounty(buyerInfo.getPayload().getBuyerCounty());
            storeBuyerInfo.setBuyerTaxInfo(buyerInfo.getPayload().getBuyerTaxInfo());
            storeBuyerInfo.setPurchaseOrderNumber(buyerInfo.getPayload().getPurchaseOrderNumber());
            order.setBuyerInfo(storeBuyerInfo);
            
            // データ格納
            storeList = storeOrderDataTemp(tenpocd,cid,order);
            orderNumberList.add(order.getAmazonOrderId());
            
            logger.info(String.format("OrderId：%s",order.getAmazonOrderId()));

          } catch (ApiException e){
            // ログに内容記載
            e.printStackTrace();
            logger.error(String.format("API Exception Code: %s %s", e.getCode(),e.getMessage()));
            logger.error(String.format("API Response: %s", e.getResponseHeaders().toString()));
            // 429:Too Many Requestsなら10秒処理停止
            if(e.getCode() == 429) {
              Thread.sleep(10000);
            }
          } catch (Exception e){
            // ログに内容記載
            e.printStackTrace();
            logger.error(String.format("getOrder Exception: %s", e.toString()));
            
            systemError.add(e.toString());
          }
        }
      //}
      // 戻り値作成
      numList.setOrderCount(unimportedNumberList.size());
      numList.setOrderNumberList(orderNumberList);
    
    return numList;
  }

  /**
   * 注文データ取得(2)
   *
   * @param cid 
   * @pram lwaAuthorizationCredentials LWA認証情報
   * @param orederNumberList 注文リスト
   * @return 処理可否
   */
  @SuppressWarnings("unused")
  public int getOrderItems(int cid, int tenpocd, LWAAuthorizationCredentials lwaAuthorizationCredentials, List<String>orderNumberList)throws IOException, InterruptedException {

    List<OrderItemList> orderItemLists = new ArrayList<>();
    ApiResponse<GetOrderItemsResponse> apiResI = null;
    String nextToken ="";
    int storeret = 0;
    int orderItemCount = 0;
    int ordersItemCount = 0;
    double dRateI = 0;
    long waitTime = 10000;    
    
    OrdersV0Api orderItemsApi = new OrdersV0Api.Builder()
                      .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                      .endpoint(Const.END_POINT_ORDER)
                      .build();
    
    try{
      // D-Rate取得・設定
      // 最新の値が必要な為、最後に取得された注文IDを利用
      apiResI = orderItemsApi.getOrderItemsWithHttpInfo(orderNumberList.get(orderNumberList.size() - 1), null);   
      dRateI = Double.parseDouble(apiResI.getHeaders().get("x-amzn-RateLimit-Limit").get(0));
      
      logger.info(String.format("OrderItemsRate：%s",dRateI));
    
      orderItemsApi = setDRate(lwaAuthorizationCredentials,dRateI,waitTime);

    // 取得注文Noが無くなるまでループ
    for(String orderNum : orderNumberList){
      try{
        ordersItemCount = 0;
        // 該当商品が無くなるまでループ
        while(ordersItemCount == 0 || nextToken != null){       
            GetOrderItemsResponse orderItemsRes = orderItemsApi.getOrderItems(orderNum, nextToken);
            
            // 各値格納
            orderItemLists.add(orderItemsRes.getPayload().getOrderItems());
            nextToken = orderItemsRes.getPayload().getNextToken();
            orderItemCount += orderItemsRes.getPayload().getOrderItems().size();
           
            //商品が無くなるまでループ
            for(OrderItem item : orderItemsRes.getPayload().getOrderItems()){
              storeret = storeOrderItemDataTemp(cid, tenpocd, orderNum, orderItemsRes.getPayload().getOrderItems().get(ordersItemCount));
              ordersItemCount += 1; 
            }
            
            logger.info(String.format("OrderId：%s ItemCount：%s",orderNum, Integer.toString(orderItemsRes.getPayload().getOrderItems().size())));
            
        }
      } catch (ApiException e){
        // ログに内容記載
        e.printStackTrace();
        logger.error(String.format("API Exception Code: %s %s", e.getCode(),e.getMessage()));
        logger.error(String.format("API Response: %s", e.getResponseHeaders().toString()));
        // 429:Too Many Requestsなら10秒停止
        if(e.getCode() == 429) {
          Thread.sleep(10000);
        }
      }catch (Exception e){
        // ログに内容記載
        e.printStackTrace();
        logger.error(String.format("storeOrder Exception: %s", e.toString()));
          
        systemError.add(e.toString());
      }
    }
    
    } catch (ApiException e){
      // ログに内容記載
      e.printStackTrace();
      logger.error(String.format("API Exception Code: %s %s", e.getCode(),e.getMessage()));
      logger.error(String.format("API Response: %s", e.getResponseHeaders().toString()));
      // 429:Too Many Requestsなら10秒停止
      if(e.getCode() == 429) {
        Thread.sleep(10000);
      }
    }catch (Exception e){
      // ログに内容記載
      e.printStackTrace();
      logger.error(String.format("storeOrder Exception: %s", e.toString()));
        
      systemError.add(e.toString());
    }
    
    return orderItemCount;
  }

  /**
 * 注文データ格納(1)(TEMP)
 *
 * @param  tenpocd 店舗コード
 * @param  cid 処理端末ID
 * @param  oreder 注文データ
 * @return retList　格納件数リスト
 */
  @SuppressWarnings("unused")
  public List<Integer> storeOrderDataTemp(int tenpocd, int cid, Order order)throws IOException {
    List<Integer> retList = new ArrayList<>();  
    int retOrder = 0;
    int retAddress = 0;
    int retBuyerInfo = 0;
    
    try{
      // Order
      retOrder = AmazonGetOrderExecSql.insertTempOrderData(1
                                                  ,cid
                                                  ,Const.SYS_OID
                                                  ,tenpocd
                                                  ,order.getAmazonOrderId()
                                                  ,nullable(order.getSellerOrderId())
                                                  ,nullable(order.getPurchaseDate())
                                                  ,nullable(order.getShipServiceLevel())
                                                  ,nullableMoney(order.getOrderTotal(),1)
                                                  ,nullableMoney(order.getOrderTotal(),0)
                                                  ,nullable(order.getLatestShipDate())
                                                  ,nullable(order.getFulfillmentChannel().toString())
                                                  ,nullable(order.getLatestDeliveryDate())
                                                  ,nullable(String .valueOf(order.isIsPrime()))
                                                  ,nullableTax(order.getMarketplaceTaxInfo(),1)
                                                  ,nullableTax(order.getMarketplaceTaxInfo(),0));
      
      // Address
      if(order.getShippingAddress() != null) {
        retAddress =  AmazonGetOrderExecSql.insertTempAddressData(2
                                                         ,cid
                                                         ,Const.SYS_OID
                                                         ,tenpocd
                                                         ,order.getAmazonOrderId()
                                                         ,nullable(order.getShippingAddress().getName())
                                                         ,(nullable(order.getShippingAddress().getAddressLine1()).replace("−", "ー"))
                                                         ,(nullable(order.getShippingAddress().getAddressLine2()).replace("−", "ー"))
                                                         ,(nullable(order.getShippingAddress().getAddressLine3()).replace("−", "ー"))
                                                         ,nullable(order.getShippingAddress().getCity())
                                                         ,nullable(order.getShippingAddress().getStateOrRegion())
                                                         ,nullable(order.getShippingAddress().getPostalCode())
                                                         ,nullable(order.getShippingAddress().getPhone()));
      }else {
        logger.warn(String.format("ShippingAddress is Nothing... OrderId: %s  Status: %s", order.getAmazonOrderId(),order.getOrderStatus()));
      }
      
      // BuyerInfo
      if(order.getBuyerInfo() != null) {
        retBuyerInfo =  AmazonGetOrderExecSql.insertTempBuyerData(3
            ,cid
            ,Const.SYS_OID
            ,tenpocd
            ,order.getAmazonOrderId()
            ,nullable(order.getBuyerInfo().getBuyerEmail())
            ,nullable(order.getBuyerInfo().getBuyerName()));
      }else {
        logger.warn(String.format("BuyerInfo is Nothing... OrderId: %s", order.getAmazonOrderId()));
      }
  
      retList.add(retOrder);
      retList.add(retAddress);
      retList.add(retBuyerInfo);
      
      } catch (Exception e){
        //ログに内容記載
        logger.error(String.format("storeOrder Exception: %s", e.toString()));
        logger.error(String.format("Error OrderId: %s", order.getAmazonOrderId()));
      }
  
    return retList;
  }

/**
 * 注文データ格納(2)(TEMP)
 *
 * @param tenpocd 店舗コード
 * @param orderItem 注文商品データ
 * @param AmazonOrderId 注文ID
 * @return retItem 格納件数
 */
  @SuppressWarnings("unused")
  public int storeOrderItemDataTemp(int cid, int tenpocd, String AmazonOrderId, OrderItem orderItem)throws IOException {
    int retItem = 0;
  
    try{
      retItem = AmazonGetOrderExecSql.insertTempItemData(4
                                                ,cid
                                                ,Const.SYS_OID
                                                ,tenpocd
                                                ,AmazonOrderId
                                                ,orderItem.getSellerSKU()
                                                ,orderItem.getTitle()
                                                ,orderItem.getQuantityOrdered().toString()
                                                ,orderItem.getItemPrice().getAmount()
                                                ,orderItem.getItemTax().getAmount()
                                                ,nullableMoney(orderItem.getShippingPrice(),0)
                                                ,nullableMoney(orderItem.getShippingTax(),0)
                                                ,nullableMoney(orderItem.getShippingDiscount(),0)
                                                ,nullableMoney(orderItem.getShippingDiscountTax(),0)
                                                ,nullableMoney(orderItem.getPromotionDiscount(),0)
                                                ,nullableMoney(orderItem.getPromotionDiscountTax(),0));
    } catch(Exception e) {
      //ログに内容記載
      logger.error(String.format("storeOrder Exception: %s", e.toString()));
    }
  
    return retItem;
  }

  
/**
 * レート制限付き OrdersApi作成
 *
 * @param  lwaAuthorizationCredentials LWA認証情報
 * @param  dRate ダイナミックレート
 * @param  waitTime 待機時間
 */
  private OrdersV0Api setDRate(LWAAuthorizationCredentials lwaAuthorizationCredentials,double dRate, long waitTime){
    // D-Rateに基づきレート設定
    RateLimitConfiguration rateLimitOption = RateLimitConfigurationOnRequests.builder()
                                      .rateLimitPermit(dRate)
                                      .waitTimeOutInMilliSeconds(waitTime)
                                      .build();
    // OrdersApi作成
    OrdersV0Api ordersApi = new OrdersV0Api.Builder()
        .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
        .rateLimitConfigurationOnRequests(rateLimitOption)
        .endpoint(Const.END_POINT_ORDER)
        .build();
  
    return ordersApi;
  }
  
  /**
  * NULL変換関数(String)
  * @param  参照値
  */
  @Nullable
  private String nullable(String n) {
    Optional<String> rData = Optional.ofNullable(n);
    return rData.orElse("");
  }
  
  /**
  * NULL変換関数(Tax)
  * @param  参照値
  */  
  @Nullable
  private String nullableTax(MarketplaceTaxInfo mTax, int prm) {
    String ret= "";
    
    if(mTax != null) {
      if(prm == 1) {
       ret = mTax.getTaxClassifications().get(0).getName();
      }else {
        ret = mTax.getTaxClassifications().get(0).getValue();
      }
    }
    return ret;
  }
  
  /**
  * NULL変換関数(Money)
  * @param  参照値
  */
  @Nullable
  private String nullableMoney(Money m, int prm) {
    String ret= "";
    
    if(m != null) {
      if(prm == 1) {
       ret = m.getCurrencyCode();
      }else {
        ret = m.getAmount();
      }
    }
    return ret;
  }

}
