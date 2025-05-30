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
import java.util.concurrent.Executors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newFixedThreadPool(4))
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_2)
            .build();

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

                log.info("Player {} try to join server", username);

                for (AuthServer authServer : sortedAuthServers) {
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
                        return;
                    } else {
                        log.info("Can't find player {} in {} server", username, authServer.getName());
                    }

                }
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


}