package org.godari.config.resolver.api;

import java.util.Set;

public interface ConfigResolver {

    void setConfigToResolve(String configToResolve);

    String resolveConfig(Set<String> userGroups) throws ConfigResolverException;

    <T> T resolveConfig(Set<String> userGroups, Class<T> targetClass) throws ConfigResolverException;

    String resolveConfig(String configToResolve, Set<String> userGroups) throws ConfigResolverException;

    <T> T resolveConfig(String configToResolve, Set<String> userGroups, Class<T> targetClass) throws ConfigResolverException;

}
