package me.deshark.yggdrasilproxy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "yggdrasil")
@Data
public class YggdrasilProperties {
    private boolean enable = false;
    private boolean log = false;
    private String ip = "0.0.0.0";
    private int port = 32217;
    private List<YggdrasilServer> servers = new ArrayList<>();

    @Data
    public static class YggdrasilServer {
        private int level;
        private String name;
        private String url;
        private Map<String, String> proxies;
        private int timeout = 5;
        private Integer port;
        private String profileApi;
    }
}