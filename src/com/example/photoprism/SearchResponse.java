package com.example.photoprism;

import java.util.List;

/** 検索結果本体 + レスポンスヘッダ（X-Count / X-Limit / X-Offset / X-Preview-Token / X-Download-Token） */
public record SearchResponse(
        List <SearchResultItem> results,
        Integer count,
        Integer limit,
        Integer offset,
        String previewToken,
        String downloadToken) {
}
