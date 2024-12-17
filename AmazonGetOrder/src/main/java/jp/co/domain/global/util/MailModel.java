package jp.co.domain.global.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * メールモデル
 *
 * --< CHANGE HISTORY >--
 *   X.XXXXXX    0000/00/00 ①XXXXXXXX
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class MailModel {
  /**
   * FROM
   */
  @JsonProperty("mailFrom")
  private String mailFrom;

  /**
   * TO
   */
  @JsonProperty("mailTo")
  private String mailTo;

  /**
   * CC
   */
  @JsonProperty("mailCc")
  private String mailCc;

  /**
   * BCC
   */
  @JsonProperty("mailBcc")
  private String mailBcc;

  /**
   * SUBJECT
   */
  @JsonProperty("mailSubject")
  private String mailSubject;

  /**
   * TEXT
   */
  @JsonProperty("mailText")
  private String mailText;

}
