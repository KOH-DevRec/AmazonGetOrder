package jp.co.domain.global.service.function;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.domain.global.mapper.AmazonGetOrderMapper;
import jp.co.domain.global.util.ApiAuthorization;

/**
 * SQL実行
 *
 * * --< CHANGE HISTORY >--
 *     X.XXXXXX    0000/00/00 ①XXXXXXXX
 */
@Component
public class AmazonGetOrderExecSql {
  
  @Autowired
  AmazonGetOrderMapper AmazonGetOrderMapper;

   /**
   * 店舗認証情報取得
   *
   * @param      店舗パラメータ
   * @return     店舗認証情報
   */
  public List<ApiAuthorization> getTenpoInfo(int prm) {
    return AmazonGetOrderMapper.execAMZ0330(prm);
  }

  /**
   * WORKテーブル更新
   *
   * @param      店舗パラメータ
   * @return     店舗認証情報
   */
  public int updateAPIData(int prm, int cid, int oid, int tenpocd) {
    return AmazonGetOrderMapper.execAMZ0331(prm, cid, oid, tenpocd);
  }

  /**
   * 取込済み注文ID取得
   *
   * @param      店舗パラメータ
   * @return     店舗認証情報
   */
  public List<String> getImportedId(int prm, int cid, int oid, int tenpocd) {
    return AmazonGetOrderMapper.execAMZ0331_4(prm, cid, oid, tenpocd);
  }
  
  
  /**
   * TEMPテーブル更新
   *
   * @param      店舗パラメータ
   * @return     店舗認証情報
   */ // return AmazonGetOrderMapper.execAMZ0332(prm, cid, oid, tenpocd, data);
  public int insertTempOrderData(int prm, int cid, int oid, int tenpocd, String ...data) {
    return AmazonGetOrderMapper.execAMZ0332(prm, cid, oid, tenpocd, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]);
  }
  
  // 取得できない値は空
  public int insertTempAddressData(int prm, int cid, int oid, int tenpocd, String ...data) {
    return AmazonGetOrderMapper.execAMZ0332(prm, cid, oid, tenpocd, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8],  "",  "",  "");
  }
  
  // 取得できない値は空
  public int insertTempBuyerData(int prm, int cid, int oid, int tenpocd, String ...data) {
    return AmazonGetOrderMapper.execAMZ0332(prm, cid, oid, tenpocd, data[0], data[1], data[2], "", "", "", "", "", "", "", "", "");
  }
  
  public int insertTempItemData(int prm, int cid, int oid, int tenpocd, String ...data) {
    return AmazonGetOrderMapper.execAMZ0332(prm, cid, oid, tenpocd, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]);
  }
  
  /**
  * 日次処理確認
  *
  * @param      店舗パラメータ
  * @return     日次処理完了数
  */
 public int checkDailyProcess(int prm) {
   return AmazonGetOrderMapper.execVAMZ0201(prm);
 }

}
