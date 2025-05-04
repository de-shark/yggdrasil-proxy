package me.deshark.yggdrasilproxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public PlayerCache playerCache() {
        return new PlayerCache();
    }

    @Bean
    public List<YggdrasilServerModel> yggdrasilServers(YggdrasilProperties properties) {
        List<YggdrasilServerModel> servers = properties.getServers().stream()
                .map(serverProps -> {
                    YggdrasilServerModel server = new YggdrasilServerModel(
                            serverProps.getLevel(),
                            serverProps.getName(),
                            serverProps.getUrl());

                    if (serverProps.getProxies() != null) {
                        server.setProxies(serverProps.getProxies());
                    }

                    if (serverProps.getProfileApi() != null) {
                        server.setProfileApi(serverProps.getProfileApi());
                    } else {
                        server.autoProfileApi();
                    }

                    server.setTimeout(serverProps.getTimeout());
                    server.setPort(serverProps.getPort());

                    return server;
                })
                .sorted((s1, s2) -> Integer.compare(s1.getLevel(), s2.getLevel()))
                .collect(Collectors.toList());

        log.info("Yggdrasil servers loaded: {}", servers);
        return servers;
    }

    @Bean
    public Map<Integer, YggdrasilServerModel> serverPidToServer(List<YggdrasilServerModel> servers) {
        Map<Integer, YggdrasilServerModel> pidMap = new TreeMap<>();
        for (int i = 0; i < servers.size(); i++) {
            YggdrasilServerModel server = servers.get(i);
            server.setPid(i);
            pidMap.put(i, server);
        }
        return pidMap;
    }
}
