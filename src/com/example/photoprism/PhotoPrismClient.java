package com.example.photoprism;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PhotoPrismClient {

    private final String       baseUrl;

    private final String       apiBase;

    private final String       accessToken;

    private final HttpClient   http;

    private final ObjectMapper om;

    public PhotoPrismClient(final String baseUrl, final String accessToken) {

        this.baseUrl = PhotoPrismClient.stripTrailingSlash(Objects.requireNonNull(baseUrl, "baseUrl"));
        this.apiBase = this.baseUrl + "/api/v1";
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken");
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        // ★ 未知プロパティは無視（API差分に強くする）
        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public SearchResponse searchPhotos(final SearchRequest request) {

        Objects.requireNonNull(request, "request");
        try {
            final var params = new LinkedHashMap <String, String>();

            if (PhotoPrismClient.nz(request.query())) {
                params.put("q", request.query());
            }
            if (request.count() != null) {
                params.put("count", String.valueOf(request.count()));
            }
            if (request.offset() != null) {
                params.put("offset", String.valueOf(request.offset()));
            }
            if (PhotoPrismClient.nz(request.order())) {
                params.put("order", request.order());
            }
            if (request.merged() != null) {
                params.put("merged", String.valueOf(request.merged()));
            }
            if (request.primary() != null) {
                params.put("primary", String.valueOf(request.primary()));
            }
            request.filters().forEach((k, v) -> {
                if (PhotoPrismClient.nz(k) && v != null) {
                    params.put(k, v);
                }
            });

            final var qs = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + PhotoPrismClient.urlEnc(e.getValue()))
                    .collect(Collectors.joining("&"));

            final var uri = URI.create(this.apiBase + "/photos" + (qs.isBlank() ? "" : "?" + qs));

            final var req = HttpRequest.newBuilder(uri)
                    .header("Authorization", "Bearer " + this.accessToken)
                    .header("X-Auth-Token", this.accessToken) // リバプロ対策
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            final var resp = this.http.send(req, HttpResponse.BodyHandlers.ofString());

            final var sc = resp.statusCode();
            if (sc == 401 || sc == 403) {
                throw new RuntimeException("Unauthorized / Forbidden: " + sc + " body=" + PhotoPrismClient.safe(resp.body()));
            }
            if (sc >= 400) {
                throw new RuntimeException("HTTP " + sc + " : " + PhotoPrismClient.safe(resp.body()));
            }

            final var ctype = resp.headers().firstValue("Content-Type").orElse("");
            if (!ctype.toLowerCase(Locale.ROOT).contains("application/json")) {
                throw new RuntimeException("Unexpected Content-Type: " + ctype +
                        " (Reverse proxy or subpath config issue?) body=" + PhotoPrismClient.safe(resp.body()));
            }

            final List <SearchResultItem> items = this.om.readValue(
                    resp.body(),
                    new TypeReference <List <SearchResultItem>>() {
                    });

            final var headers = resp.headers();
            final var xCount = PhotoPrismClient.parseInt(headers.firstValue("X-Count").orElse(null));
            final var xLimit = PhotoPrismClient.parseInt(headers.firstValue("X-Limit").orElse(null));
            final var xOffset = PhotoPrismClient.parseInt(headers.firstValue("X-Offset").orElse(null));
            final var previewToken = headers.firstValue("X-Preview-Token").orElse(null);
            final var downloadToken = headers.firstValue("X-Download-Token").orElse(null);

            return new SearchResponse(items, xCount, xLimit, xOffset, previewToken, downloadToken);

        } catch (final RuntimeException re) {
            throw re;
        } catch (final Exception e) {
            throw new RuntimeException("searchPhotos failed", e);
        }
    }


    public String buildThumbnailUrl(final String sha1Hash, final String tokenOrPublic, final String sizeName) {

        Objects.requireNonNull(sha1Hash, "sha1Hash");
        Objects.requireNonNull(sizeName, "sizeName");
        final var token = PhotoPrismClient.nz(tokenOrPublic) ? tokenOrPublic : "public";
        return this.apiBase + "/t/" + sha1Hash + "/" + token + "/" + sizeName;
    }


    public String buildVideoUrl(final String sha1Hash, final String tokenOrPublic, final String format) {

        Objects.requireNonNull(sha1Hash, "sha1Hash");
        final var token = PhotoPrismClient.nz(tokenOrPublic) ? tokenOrPublic : "public";
        final var fmt = PhotoPrismClient.nz(format) ? format : "avc";
        return this.apiBase + "/videos/" + sha1Hash + "/" + token + "/" + fmt;
    }


    private static String urlEnc(final String s) {

        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }


    private static String stripTrailingSlash(final String s) {

        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }


    private static Integer parseInt(final String s) {

        try {
            return (s == null) ? null : Integer.parseInt(s);
        } catch (final Exception e) {
            return null;
        }
    }


    private static boolean nz(final String s) {

        return s != null && !s.isBlank();
    }


    private static String safe(String s) {

        if (s == null) {
            return "";
        }
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
