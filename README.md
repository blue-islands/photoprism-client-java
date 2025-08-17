# PhotoPrism Java Client (Search & Thumbnails)

PhotoPrism の検索API・検索フィルター・サムネイル／動画エンドポイントを扱う Java 17 用の軽量クライアントです。
HTTP は `java.net.http.HttpClient`、JSON は Jackson を利用します。

## インストール方法 

Latest Version:
[![](https://jitpack.io/v/blue-islands/photoprism-client-java.svg)](https://jitpack.io/#blue-islands/photoprism-client-java)

下記の **VERSION** キーを上記の最新バージョンに必ず置き換えてください

Maven
```xml
<dependency>
    <groupId>com.github.blue-islands</groupId>
    <artifactId>photoprism-client-java</artifactId>
    <version>VERSION</version>
</dependency>
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## クイックスタート

```java
var client = new PhotoPrismClient(
    "https://your-photoprism.example.com",  // 末尾スラなし
    System.getenv("PHOTOPRISM_TOKEN")       // App Password などのアクセストークン
);

var req = SearchRequest.builder()
        .query("label:cat color:green") // 検索ボックスと同じ書式
        .count(100).offset(0)
        .order("added")
        .merged(true)                   // 同一写真のファイルを1件にマージ
        .primary(true)                  // 代表ファイルのみ（一覧向け）
        .filter("year", "2024")         // GETパラメータ経由のフィルタ
        .filter("type", "image|live")
        .build();

var res = client.searchPhotos(req);

// サムネイルURLの生成（X-Preview-Token を利用）
if (!res.results().isEmpty()) {
    var hash = res.results().get(0).hash();
    var url  = client.buildThumbnailUrl(hash, res.previewToken(), "tile_500");
    System.out.println(url);
}
```

* 検索エンドポイントは `GET /api/v1/photos`。`merged`/`primary` の振る舞いは公式ドキュメントの説明に準拠しています。([docs.photoprism.app][1])
* サムネイルURL形式は `/api/v1/t/:hash/:token/:size`、動画は `/api/v1/videos/:hash/:token/:format` です。公開モードでは token に `public` を使えます。([docs.photoprism.app][2])
* `X-Preview-Token` はレスポンスヘッダで返り、サムネイル/動画の token に使えます（既定のプレビュートークンの仕組みもこちら）。([docs.photoprism.app][3])

## よく使う検索フィルタ

検索ボックスと同じ書式（例：`label:cat color:green`）は `.query()` で渡せます。
一方で、`year=2025` 等を **個別のクエリパラメータ** として渡すことも可能です（`.filter(name, value)`）。フィルタ一覧は公式の「Filter Reference」を参照。([docs.photoprism.app][4])

例：

```java
// 地理：緯度経度＋半径50km
SearchRequest.builder()
  .filter("lat",  "35.6812")
  .filter("lng",  "139.7671")
  .filter("dist", "50")
  .build();

// 色（OR）とメディアタイプ
SearchRequest.builder()
  .filter("color", "green|blue")
  .filter("type",  "image|live")
  .build();
```

### アルバムID（UID）指定の方法

PhotoPrism の検索フィルタには **album** と **albums** があります：

* `album:` … アルバム **UID または名前** を 1 つ指定（ワイルドカード `*` 可）
  例：`album:pqbcf5j446s0futy`、`album:"Summer 2024*"`
* `albums:` … **複数のアルバム名** を `&`（AND）や `|`（OR）で組み合わせ
  例：`albums:"South Africa & Birds"`（両方に属する）

これらは検索ボックス同等のフィルタとして定義されています。([docs.photoprism.app][4])

#### 使い方（コード例）

```java
// 1) クエリ文字列で指定（最も簡単）
SearchRequest req = SearchRequest.builder()
    .query("album:pqbcf5j446s0futy label:cat")
    .primary(true)
    .build();

// 2) GETパラメータで指定（.filter を使う）
SearchRequest req2 = SearchRequest.builder()
    .filter("album", "pqbcf5j446s0futy")
    .build();
```

#### アルバムUIDの探し方

アルバム一覧は `GET /api/v1/albums` で取得できます（`q` / `count` / `offset` などの検索・並び替えパラメータ対応）。個別のアルバム詳細は `GET /api/v1/albums/{uid}`、ZIP ダウンロードは `GET /api/v1/albums/{uid}/dl` です。([pkg.go.dev][5])

* 例：`/api/v1/albums?q=Summer&count=50` で候補を列挙 → `UID` を取得 → 上記 `album:<UID>` で検索に利用。

## サムネイル／動画URLの生成

```java
String thumb = client.buildThumbnailUrl(fileHash, res.previewToken(), "tile_500");
// 代表的なサイズ: tile_50, tile_100, tile_224, tile_500, fit_720, fit_1280, …（公式列挙）
String video = client.buildVideoUrl(fileHash, res.previewToken(), "avc"); // 現状は avc のみ対応
```

サイズと動画エンドポイントの仕様はサムネイルAPIのドキュメントを参照してください。([docs.photoprism.app][2], [pkg.go.dev][6])
`fit_720` などの小さめサイズは既定で生成されています。([docs.photoprism.app][7])

## ページング

* レスポンスヘッダ `X-Count / X-Limit / X-Offset` を読み、`offset += X-Limit` で繰り返し取得してください（`count` は 1〜100000）。([docs.photoprism.app][1])

## 認証

* App Password / OAuth2 アクセストークンを **Bearer** で送信します。環境によっては `X-Auth-Token` ヘッダでも認証できます。([docs.photoprism.app][8])
* **セキュリティ注意**：トークンは環境変数やシークレット管理を使い、公開リポジトリに平文で含めないでください。

## トラブルシュート

* **200 だが JSON でなく HTML が返る**
  サブパス配備やリバプロ設定の影響で、ログインHTMLが返っている可能性があります。`Content-Type` を確認し、`/photoprism/api/v1/...` が正しく転送されるよう設定してください。
  また `Authorization` を落とす環境では `X-Auth-Token` も併用してください。([docs.photoprism.app][8])
* **サムネイルが取得できない**
  トークン（`X-Preview-Token` か `public`）とサイズ名を再確認。生成ポリシーやキャッシュ挙動はサムネイル/サイズ仕様を参照してください。([docs.photoprism.app][2])

## ライセンス

このクライアントのライセンスはリポジトリの `LICENSE` を参照してください。
PhotoPrism の API とドキュメントは PhotoPrism UG により提供されています。([docs.photoprism.app][9])

[1]: https://docs.photoprism.app/developer-guide/api/search/?utm_source=chatgpt.com "Search Endpoints"
[2]: https://docs.photoprism.app/developer-guide/api/thumbnails/?utm_source=chatgpt.com "Thumbnail Image API"
[3]: https://docs.photoprism.app/getting-started/config-options/?utm_source=chatgpt.com "Config Options"
[4]: https://docs.photoprism.app/user-guide/search/filters/ "Search Filters - PhotoPrism"
[5]: https://pkg.go.dev/github.com/photoprism/photoprism/internal/api "api package - github.com/photoprism/photoprism/internal/api - Go Packages"
[6]: https://pkg.go.dev/github.com/photoprism/photoprism/internal/api?utm_source=chatgpt.com "api package - github.com/photoprism ..."
[7]: https://docs.photoprism.app/developer-guide/media/thumbnails/?utm_source=chatgpt.com "Thumbnails"
[8]: https://docs.photoprism.app/developer-guide/api/oauth2/?utm_source=chatgpt.com "OAuth2 Grant Types"
[9]: https://docs.photoprism.app/developer-guide/documentation/?utm_source=chatgpt.com "Documentation"
