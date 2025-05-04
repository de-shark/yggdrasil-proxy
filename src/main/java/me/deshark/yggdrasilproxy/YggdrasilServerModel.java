package me.deshark.yggdrasilproxy;

import java.util.Map;

import lombok.Data;

@Data
public class YggdrasilServerModel {
    private int pid;
    private int level;
    private String name;
    private String url;
    private Map<String, String> proxies;
    private int timeout = 5; // Default timeout
    private Integer port;
    private String profileApi;

    public YggdrasilServerModel(int level, String name, String url) {
        this.level = level;
        this.name = name;
        this.url = url;
    }

    public String serverTypeCheck() {
        if (url.contains("/api/yggdrasil/sessionserver")) {
            return "unofficial";
        } else if (url.contains("sessionserver.mojang.com")) {
            return "official";
        } else {
            return "unknown";
        }
    }

    public void setUnofficialProfileApi() {
        this.profileApi = url.replace("/api/yggdrasil/sessionserver", "/api/profiles/minecraft");
    }

    public void setOfficialProfileApi() {
        this.profileApi = "https://api.mojang.com/profiles/minecraft";
    }

    public void autoProfileApi() {
        String serverType = serverTypeCheck();
        if ("official".equals(serverType)) {
            setOfficialProfileApi();
        } else if ("unofficial".equals(serverType)) {
            setUnofficialProfileApi();
        } else {
            this.profileApi = null; // Cannot auto-determine server address
        }
    }
}