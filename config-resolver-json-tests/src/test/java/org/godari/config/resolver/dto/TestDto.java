package org.godari.config.resolver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TestDto {

    @JsonProperty("property1")
    private Integer property1;

    @JsonProperty("property2")
    private Property2 property2;

    @JsonProperty("property3")
    private Property3 property3;

    @Data
    @NoArgsConstructor
    private static class Property2 {

        @JsonProperty("property2-1")
        private Boolean property2dash1;
    }

    @Data
    @NoArgsConstructor
    private static class Property3 {

        @JsonProperty("property3-1")
        private Property3dash1 property3dash1;
    }

    @Data
    @NoArgsConstructor
    private static class Property3dash1 {

        @JsonProperty("property3-1-1")
        private Boolean property3dash1dash1;
    }
}