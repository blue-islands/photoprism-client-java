package com.example.photoprism;

public class Main {

    public static void main(final String[] args) {

        // TODO 自動生成されたメソッド・スタブ
        final var client = new PhotoPrismClient(
                "https://jetson.livlog.xyz/photoprism", // ベースURL（末尾スラなし）
                "Rz7nHG-xDVDjw-hFGENl-kA8v3c" // Bearer アクセストークン
        );

        final var req = SearchRequest.builder()
                .filter("album", "at0yqoazysvzw949")
                .build();

        final var res = client.searchPhotos(req);

        // 取得した一覧のサムネイルURLを得る（X-Preview-Token を自動で利用）
        for (final SearchResultItem result : res.results()) {
            final var hash = result.hash(); // ファイルSHA1
            final var thumb500 = client.buildThumbnailUrl(hash, res.previewToken(), "tile_500");
            System.out.println("Thumb: " + thumb500);
        }

    }

}
