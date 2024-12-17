package jp.co.domain.global.common;

import java.time.format.DateTimeFormatter;

/**
 * 定数クラス
 *
 * * --< CHANGE HISTORY >--
 *     X.XXXXXX    0000/00/00 ①XXXXXXXX
 */
public class Const {
    // SP-APIエンドポイント：認証
    public static final String END_POINT_AUTH = "https://api.amazon.com/auth/o2/token";
    // SP-APIエンドポイント：注文取得
    public static final String END_POINT_ORDER = "https://sellingpartnerapi-fe.amazon.com";
    // TODO TEST
    //public static final String END_POINT_ORDER = "https://sandbox.sellingpartnerapi-fe.amazon.com";
    //　マーケットプレイスID(日本)
    public static final String MAKET_PLACE_ID = "A1VC38T7YXB528";
    // アプリケーションID
    // TODO CHANGE
    public static final String APPLICATION_ID ="amzn1.sp.solution.************************************";
    // リージョン値(日本)
    public static final String REGION = "us-west-2";
    // システムOID
    public static final int SYS_OID =  32767;
    // トークン生成用パス
    public static final String ORDER_PATH  =  "/orders/v0/orders";
    // 受注データ変換実行PGパス
    // TODO CHANGE
    public static final String AMZ030_LOCAL_PATH = "**************\AMZOrderConversion.MDB";
    // 受注データ変換コピー元PGパス
    // TODO CHANGE
    public static final String AMZ030_FIX_PATH = "**************\AMZOrderConversion.MDB";
    // ACCESS実行ファイルパス
    // Office2016の場合
    public static final String ACCESS_PATH = "C:\\Program Files (x86)\\Microsoft Office\\Office16\\MSACCESS.exe";
    // Office365の場合
    // public static final String ACCESS_PATH = "C:\\Program Files (x86)\\Microsoft Office\\root\\Office16\\MSACCESS.exe";
    // 日時変換フォーマット
    public static final DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    // メール設定
    // TODO CHANGE
     public static final String MAIL_FROM = "*********************";
     public static final String MAIL_TO = "*********************";
    
    public static final String MAIL_SUBJECT = "Amazon注文データ取得 エラー通知";
}
