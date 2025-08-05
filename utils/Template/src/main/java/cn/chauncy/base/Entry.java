package cn.chauncy.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Entry {
    public record Int2IntVal(@JsonProperty("k") int k, @JsonProperty("v") int v) {
        @JsonCreator
        public Int2IntVal {}
    }

    public record Int2FloatVal(@JsonProperty("k") int k, @JsonProperty("v") float v) {
        @JsonCreator
        public Int2FloatVal {}
    }

    public record Int2LongVal(@JsonProperty("k") int k, @JsonProperty("v") long v) {
        @JsonCreator
        public Int2LongVal {}
    }
}
