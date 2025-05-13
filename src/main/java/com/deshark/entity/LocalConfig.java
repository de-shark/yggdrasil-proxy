package com.deshark.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalConfig {
    public int port;
    public List<AuthServer> authServers;


    static File configPath = new File("config");
    static File configFile = new File(configPath, "config.yml");

    public LocalConfig() {
    }

    public LocalConfig(int port, List<AuthServer> authServers) {
        this.port = port;
        this.authServers = authServers;
    }

    public static LocalConfig loadConfig() throws IOException {
        if (!configFile.exists()) {
            configPath.mkdirs();

            List<AuthServer> authServers = new ArrayList<>();
            authServers.add(new AuthServer(0, "Mojang", "https://sessionserver.mojang.com", 5));
            authServers.add(new AuthServer(1, "LittleSkin", "https://littleskin.cn/api/yggdrasil/sessionserver", 5));

            LocalConfig defaultConfig = new LocalConfig(32217, authServers);

            YAMLFactory yamlFactory = new YAMLFactory();
            yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
            ObjectMapper mapper = new ObjectMapper(yamlFactory);

            try (OutputStream out = new FileOutputStream(configFile)) {
                mapper.writeValue(out, defaultConfig);
            }

            return defaultConfig;
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(configFile.toURI()), LocalConfig.class);
    }
}
