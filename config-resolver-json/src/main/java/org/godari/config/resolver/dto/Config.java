package org.godari.config.resolver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class Config {

    @JsonProperty("override-rules")
    private List<OverrideRule> overrideRules;

    @JsonProperty("default-properties")
    private JsonNode defaultProperties;

}