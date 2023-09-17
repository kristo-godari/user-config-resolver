package org.godari.config.resolver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.godari.config.resolver.api.ConfigResolver;
import org.godari.config.resolver.api.ConfigResolverException;
import org.godari.config.resolver.dto.Config;
import org.godari.config.resolver.dto.OverrideRule;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

import static java.util.Objects.isNull;

@Service
public class JsonConfigResolverService implements ConfigResolver {

    private static final String CONFIG_NULL_EXCEPTION_MESSAGE = "Config to resolve is null. Use setConfigToResolve() method to set the config.";
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser;
    private String configToResolve;

    public JsonConfigResolverService(ObjectMapper objectMapper, ExpressionParser expressionParser) {
        this.objectMapper = objectMapper;
        this.expressionParser = expressionParser;
    }

    @Override
    public void setConfigToResolve(String configToResolve) {
        this.configToResolve = configToResolve;
    }

    @Override
    public String resolveConfig(Set<String> userGroups) throws ConfigResolverException {
        if (isNull(this.configToResolve)) throw new ConfigResolverException(CONFIG_NULL_EXCEPTION_MESSAGE);
        return applyOverrideRulesFor(this.configToResolve, userGroups, String.class);
    }

    @Override
    public <T> T resolveConfig(Set<String> userGroups, Class<T> targetClass) throws ConfigResolverException {
        if (isNull(this.configToResolve)) throw new ConfigResolverException(CONFIG_NULL_EXCEPTION_MESSAGE);
        return applyOverrideRulesFor(this.configToResolve, userGroups, targetClass);
    }

    @Override
    public String resolveConfig(String configToResolve, Set<String> userGroups) throws ConfigResolverException {
        return applyOverrideRulesFor(configToResolve, userGroups, String.class);
    }

    @Override
    public <T> T resolveConfig(String configToResolve, Set<String> userGroups, Class<T> targetClass) throws ConfigResolverException {
        return applyOverrideRulesFor(configToResolve, userGroups, targetClass);
    }

    private <T> T applyOverrideRulesFor(String configToResolve, Set<String> userGroups, Class<T> targetClass) throws ConfigResolverException {

        Config configToUpdate;
        try {
            configToUpdate = objectMapper.readValue(configToResolve, Config.class);
            Config configUpdated = applyOverrideRules(userGroups, configToUpdate);

            return transformToTargetClass(targetClass, configUpdated.getDefaultProperties());
        } catch (Exception e) {
            throw new ConfigResolverException(e);
        }
    }

    private Config applyOverrideRules(Set<String> userGroups, Config configToUpdate) {
        for (OverrideRule overrideRule : configToUpdate.getOverrideRules()) {
            if (!overrideRuleApplies(userGroups, overrideRule)) {
                continue;
            }
            overrideConfigProperties(configToUpdate, overrideRule);
        }
        return configToUpdate;
    }

    private <T> T transformToTargetClass(Class<T> targetClass, JsonNode config) throws JsonProcessingException {

        if (String.class.isAssignableFrom(targetClass)) {
            return (T) objectMapper.writeValueAsString(config);
        }

        return objectMapper.treeToValue(config, targetClass);
    }

    private Boolean overrideRuleApplies(Set<String> userGroups, OverrideRule overrideRule) {
        if (userIsInAllGroups(userGroups, overrideRule)) {
            return true;
        }

        if (userIsInAnyGroup(userGroups, overrideRule)) {
            return true;
        }

        if (userIsInNoneOfTheGroups(userGroups, overrideRule)) {
            return true;
        }

        return customExpressionMatches(userGroups, overrideRule);
    }

    private boolean userIsInAllGroups(Set<String> userGroups, OverrideRule overrideRule) {
        Set<String> requiredGroups = overrideRule.getUserIsInAllGroups();
        return requiredGroups != null && userGroups.containsAll(requiredGroups);
    }

    private boolean userIsInAnyGroup(Set<String> userGroups, OverrideRule overrideRule) {
        Set<String> anyGroups = overrideRule.getUserIsInAnyGroup();
        return anyGroups != null && containsAny(userGroups, anyGroups);
    }

    private boolean userIsInNoneOfTheGroups(Set<String> userGroups, OverrideRule overrideRule) {
        Set<String> noneGroups = overrideRule.getUserIsInNoneOfTheGroups();
        return noneGroups != null && !containsAny(userGroups, noneGroups);
    }

    private boolean customExpressionMatches(Set<String> userGroups, OverrideRule overrideRule) {
        String customExpression = overrideRule.getCustomExpression();
        if (customExpression != null) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("user", userGroups);
            return expressionParser.parseExpression(customExpression).getValue(context, Boolean.class);
        }
        return false;
    }

    public boolean containsAny(Set<String> userGroups, Set<String> overrideRuleGroups) {
        return overrideRuleGroups.stream().anyMatch(userGroups::contains);
    }

    private Config overrideConfigProperties(Config myConfig, OverrideRule overrideRule) {
        overrideRule.getOverride().fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode value = entry.getValue();
            overrideProperty(myConfig.getDefaultProperties(), fieldName, value);
        });

        return myConfig;
    }

    private void overrideProperty(JsonNode node, String propertyPath, JsonNode value) {
        String[] properties = propertyPath.split("\\.");

        if (node instanceof ObjectNode objectNode) {
            if (properties.length == 1) {
                objectNode.replace(properties[0], value);
            } else {
                recursivelyReplaceProperty(value, properties, objectNode);
            }
        }
    }

    private void recursivelyReplaceProperty(JsonNode value, String[] properties, ObjectNode objectNode) {
        JsonNode nextNode = objectNode.get(properties[0]);
        if (nextNode == null) {
            objectNode.putObject(properties[0]);
            nextNode = objectNode.get(properties[0]);
        }
        overrideProperty(nextNode, String.join(".", Arrays.copyOfRange(properties, 1, properties.length)), value);
    }
}
