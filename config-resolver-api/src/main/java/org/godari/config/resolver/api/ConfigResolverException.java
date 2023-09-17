package org.godari.config.resolver.api;


public class ConfigResolverException extends Exception {
    public ConfigResolverException(Exception e) {
        super(e);
    }

    public ConfigResolverException(String message) {
        super(message);
    }
}
