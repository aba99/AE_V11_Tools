package com.automic.api;

@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }

    public ConfigurationException(String s) {
        super(s);
    }
}
