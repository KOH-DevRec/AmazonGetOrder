# AmazonSP-API
本リポジトリは、Amazon SP-API(Selling Partner API)を利用した受発注システム構築の為に作成したものです。  
2024.9開発時点のAPIを参照している為、利用APIに仕様変更があれば適宜修正が必要となります。  
SP-APIの基本的な利用方法は公式ドキュメントに記載されています。  
各種APIドキュメントは以下から確認してください。  
SP-API：https://developer-docs.amazon.com/sp-api/lang-ja_JP/  
※SP-APIを実店舗で利用するには、Amazonセラーセントラルの「デベロッパーセントラル」の設定が必要になります。  

## AmazonGetOrder
本システムはAmazon店舗の注文情報を取得するものです。  
取得した内容はDB(SQL server)へ書込後、他システムを呼び出すことで受注変換されることを想定しています。  
なお、店舗に設定されたライセンス情報は実行前にDB側で保持している想定です。  


### 開発環境
- 言語：Java8
- フレームワーク：Spring Boot
- IDE：Eclipse2022

### 利用API：
- Orders API v0