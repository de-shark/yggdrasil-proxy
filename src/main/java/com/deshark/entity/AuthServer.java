package com.deshark.entity;

public class AuthServer {
    private int priority;
    private String name;
    private String url;
    private int timeout;

    public AuthServer() {
    }

    public AuthServer(int priority, String name, String url, int timeout) {
        this.priority = priority;
        this.name = name;
        this.url = url;
        this.timeout = timeout;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getTimeout() {
        return timeout;
    }
}
