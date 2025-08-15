package com.example.photoprism;

public class Main {

    public static void main(final String[] args) {

        // TODO 自動生成されたメソッド・スタブ
        final var client = new PhotoPrismClient(
                "https://jetson.livlog.xyz/photoprism", // ベースURL（末尾スラなし）
                "Rz7nHG-xDVDjw-hFGENl-kA8v3c" // Bearer アクセストークン
        );

        final var req = SearchRequest.builder()
                .query("") // または空でOK（qパラメータ）
                .count(100)
                .offset(0)
                .order("added")
                .merged(true) // 同一写真のファイルをマージ
                .primary(true) // プライマリのみ（一覧用に便利）
                // 以下は「クエリ文字列ではなくGETパラメータで渡す」派生フィルタ例
                // .filter("year", "2024")
                // .filter("type", "image|live")
                .build();

        final var res = client.searchPhotos(req);

        // 1枚目のサムネイルURLを得る（X-Preview-Token を自動で利用）
        if (!res.results().isEmpty()) {
            final var first = res.results().get(0);
            final var hash = first.hash(); // ファイルSHA1
            final var thumb500 = client.buildThumbnailUrl(hash, res.previewToken(), "tile_500");
            System.out.println("Thumb: " + thumb500);
        }

    }

}
