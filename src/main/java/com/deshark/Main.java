package com.deshark;

import com.deshark.entity.AuthServer;
import com.deshark.entity.LocalConfig;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newFixedThreadPool(4))
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_2)
            .build();

    private static final ConcurrentHashMap<String, AuthServer> playerAuthCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            LocalConfig config = LocalConfig.loadConfig();
            List<AuthServer> sortedAuthServers = config.authServers;
            sortedAuthServers.sort(Comparator.comparing(AuthServer::getPriority));

            var app = Javalin.create().start(config.port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (app != null) {
                    app.stop();
                    log.info("Server stopped gracefully.");
                }
            }));

            app.get("/", ctx -> {
                log.info("A Minecraft server linked to Yggdrasil Proxy Server");
                ctx.contentType("application/json");
                ctx.result("{\"meta\":{\"serverName\":\"Yggdrasil Proxy Server\",\"implementationName\":\"Yggdrasil Proxy Server\",\"implementationVersion\":\"1.0\"}}");
            });

            app.get("/sessionserver/session/minecraft/hasJoined", ctx -> {
                String username = ctx.queryParam("username");
                String serverId = ctx.queryParam("serverId");

                if (username == null || serverId == null) {
                    ctx.status(400).result("Missing username or serverId");
                    return;
                }

                log.info("+ Player {} try to join server", username);

                AuthServer cachedServer = playerAuthCache.get(username);
                if (cachedServer != null) {
                    log.debug("Found cached auth server for {}: {}", username, cachedServer.getName());
                    boolean success = tryAuthWithServer(ctx, username, serverId, cachedServer);
                    if (success) {
                        return;
                    } else {
                        playerAuthCache.remove(username);
                        log.info("Cached server {} failed for {}, removed from cache", cachedServer.getName(), username);
                    }
                }

                for (AuthServer authServer : sortedAuthServers) {
                    boolean success = tryAuthWithServer(ctx, username, serverId, authServer);
                    if (success) {
                        playerAuthCache.put(username, authServer);
                        log.info("Player {} successfully authenticated with {} server, cached", username, authServer.getName());
                        return;
                    }
                }

                log.info("Player {} authentication failed on all servers", username);
                ctx.status(204);
            });

            app.error(404, ctx -> {
                String requestUrl = ctx.url();
                String method = ctx.method().name();
                log.debug("Endpoint {} {} not found", requestUrl, method);
                ctx.result("Endpoint " + method + " " + requestUrl + " not found");
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 尝试使用指定的认证服务器进行鉴权，如果成功则直接将响应写入 ctx 并返回 true，否则返回 false。
     */
    private static boolean tryAuthWithServer(io.javalin.http.Context ctx, String username, String serverId, AuthServer authServer) {
        try {
            String url = String.format(
                    "%s/session/minecraft/hasJoined?username=%s&serverId=%s",
                    authServer.getUrl(),
                    username,
                    serverId
            );
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(authServer.getTimeout()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                log.info("Find player {} in {} server", username, authServer.getName());
                ctx.result(response.body());
                return true;
            } else {
                log.info("Can't find player {} in {} server", username, authServer.getName());
                return false;
            }
        } catch (Exception e) {
            log.warn("Failed to connect to {} server ({}): {}",
                    authServer.getName(), authServer.getUrl(), e.getMessage());
            return false;
        }
    }
}
