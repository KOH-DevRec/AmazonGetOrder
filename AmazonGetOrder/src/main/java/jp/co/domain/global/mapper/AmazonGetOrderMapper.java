package jp.co.domain.global.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jp.co.domain.global.util.ApiAuthorization;

/**
 * マッパー
 *
 * * --< CHANGE HISTORY >--
 *     X.XXXXXX    0000/00/00 ①XXXXXXXX
 */

@Mapper
public interface AmazonGetOrderMapper {
   /**
   * 店舗認証情報取得
   *
   * @param  パラメータ
   * @return 店舗認証情報
   */
   List<ApiAuthorization> execAMZ0330(int prm);

   /**
   * 注文データ格納
   *
   * @param  パラメータ
   * @return 店舗認証情報
   */
   public int execAMZ0331(int prm, int cid, int oid, int tenpocd);
   
   /**
   * 注文データ格納
   *
   * @param  パラメータ
   * @return 店舗認証情報
   */
   public List<String> execAMZ0331_4(int prm, int cid, int oid, int tenpocd);

   /**
   * 注文データ格納
   *
   * @param  パラメータ
   * @tenpocd 
   * @data1
   * @data2
   * @data3
   * @data4
   * @data5
   * @data6
   * @data7
   * @data8
   * @data9
   * @data10
   * @data11
   * @data12
   * @return
   */
   public int execAMZ0332(int prm, int cid, int oid, int tenpocd, String data0, String data1, String data2, String data3, String data4, String data5, String data6, String data7, String data8, String data9, String data10, String data11);
   //public String execAMZ0332(int prm, int cid, int oid, int tenpocd, String ...data);
   
   /**
   * 
   *
   * @param  パラメータ
   * @return 日次処理完了数
   */
   public int execVAMZ0201(int prm);
}
