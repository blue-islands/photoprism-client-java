package jp.livlog.photoprism;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchResultFile(
        @JsonProperty ("UID") String uid,
        @JsonProperty ("PhotoUID") String photoUid,
        @JsonProperty ("Name") String name,
        @JsonProperty ("Root") String root,
        @JsonProperty ("Hash") String hash,
        @JsonProperty ("Size") Long size,
        @JsonProperty ("Primary") Boolean primary,
        @JsonProperty ("Codec") String codec,
        @JsonProperty ("FileType") String fileType,
        @JsonProperty ("MediaType") String mediaType,
        @JsonProperty ("Mime") String mime,
        @JsonProperty ("Width") Integer width,
        @JsonProperty ("Height") Integer height) {
}
