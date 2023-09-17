package org.godari.config.resolver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Set;

@Data
public class OverrideRule {

    @JsonProperty("user-is-in-all-groups")
    private Set<String> userIsInAllGroups;

    @JsonProperty("user-is-in-any-group")
    private Set<String> userIsInAnyGroup;

    @JsonProperty("user-is-none-of-the-groups")
    private Set<String> userIsInNoneOfTheGroups;

    @JsonProperty("custom-expression")
    private String customExpression;

    @JsonProperty("override")
    private JsonNode override;
}