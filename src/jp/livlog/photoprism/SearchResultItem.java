package jp.livlog.photoprism;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** /api/v1/photos のレスポンス例に基づく最小DTO */
public record SearchResultItem(
        @JsonProperty ("ID") String id,
        @JsonProperty ("UID") String uid,
        @JsonProperty ("Type") String type,
        @JsonProperty ("TakenAt") String takenAt,
        @JsonProperty ("TakenAtLocal") String takenAtLocal,
        @JsonProperty ("TimeZone") String timeZone,
        @JsonProperty ("Path") String path,
        @JsonProperty ("Name") String name,
        @JsonProperty ("OriginalName") String originalName,
        @JsonProperty ("Title") String title,
        @JsonProperty ("Description") String description,
        @JsonProperty ("Year") Integer year,
        @JsonProperty ("Month") Integer month,
        @JsonProperty ("Day") Integer day,
        @JsonProperty ("Country") String country,
        @JsonProperty ("Favorite") Boolean favorite,
        @JsonProperty ("Private") Boolean priv,
        @JsonProperty ("Quality") Integer quality,
        @JsonProperty ("Color") Integer color,
        @JsonProperty ("Lat") Double lat,
        @JsonProperty ("Lng") Double lng,
        @JsonProperty ("PlaceLabel") String placeLabel,
        @JsonProperty ("Hash") String hash,
        @JsonProperty ("Width") Integer width,
        @JsonProperty ("Height") Integer height,
        @JsonProperty ("Portrait") Boolean portrait,
        @JsonProperty ("Merged") Boolean merged,
        @JsonProperty ("Files") List <SearchResultFile> files) {

    /** 代表ファイルのSHA1を返す（Hash があればそれ優先） */
    public String hash() {

        if (this.hash != null && !this.hash.isBlank()) {
            return this.hash;
        }
        if (this.files != null) {
            return this.files.stream().filter(SearchResultFile::primary)
                    .findFirst()
                    .map(SearchResultFile::hash)
                    .orElse(this.files.isEmpty() ? null : this.files.get(0).hash());
        }
        return null;
    }
}
