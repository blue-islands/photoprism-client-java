package com.example.photoprism;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record SearchRequest(
        String query,
        Integer count,
        Integer offset,
        String order,
        Boolean merged,
        Boolean primary,
        Map <String, String> filters) {

    public static Builder builder() {

        return new Builder();
    }

    public static class Builder {

        private String                     query;

        private Integer                    count   = 100;

        private Integer                    offset  = 0;

        private String                     order   = "added";

        private Boolean                    merged  = true;

        private Boolean                    primary = true;

        private final Map <String, String> filters = new LinkedHashMap <>();

        public Builder query(final String q) {

            this.query = q;
            return this;
        }


        public Builder count(final int v) {

            this.count = v;
            return this;
        }


        public Builder offset(final int v) {

            this.offset = v;
            return this;
        }


        public Builder order(final String v) {

            this.order = v;
            return this;
        }


        public Builder merged(final boolean v) {

            this.merged = v;
            return this;
        }


        public Builder primary(final boolean v) {

            this.primary = v;
            return this;
        }


        public Builder filter(final String name, final String value) {

            this.filters.put(name, value);
            return this;
        }


        public SearchRequest build() {

            return new SearchRequest(this.query, this.count, this.offset, this.order, this.merged, this.primary,
                    Collections.unmodifiableMap(new LinkedHashMap <>(this.filters)));
        }
    }
}
