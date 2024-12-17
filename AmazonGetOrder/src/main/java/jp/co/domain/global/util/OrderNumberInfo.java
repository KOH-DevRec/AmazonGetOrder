package jp.co.domain.global.util;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 注文情報クラス
 *
 * * --< CHANGE HISTORY >--
 *     X.XXXXXX    0000/00/00 ①XXXXXXXX
 */
@Getter
@Setter
public class OrderNumberInfo {

    /**
     * 注文数
     */
    private int OrderCount;

    /**
     * 注文番号リスト
     */
    private List<String> orderNumberList;
}
