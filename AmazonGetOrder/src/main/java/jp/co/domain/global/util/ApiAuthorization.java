package jp.co.domain.global.util;

import lombok.Getter;
import lombok.Setter;

/**
 * 店舗認証情報クラス
 *
 * * --< CHANGE HISTORY >--
 *     X.XXXXXX    0000/00/00 ①XXXXXXXX
 */
@Getter
@Setter
public class ApiAuthorization {

    /**
     * 店舗コード
     */
    private String tenpocd;

    /**
     * 店舗名
     */
    private String tenponm;

    /**
     * クライアントID
     */
    private String client_id;

    /**
     * クライアント機密情報
     */
    private String client_secret;
    
    /**
     * リフレッシュトークン
     */
    private String refesh_token;

    
    /**
     * アクセスキーID
     */
    private String access_key;
    
    /**
     * シークレットキー
     */
    private String secret_id;

}
