package me.deshark.yggdrasilproxy;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class YggdrasilService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PlayerCache playerCache;

    @Autowired
    private List<YggdrasilServerModel> yggdrasilServers;

    @Autowired
    private Map<Integer, YggdrasilServerModel> serverPidToServer;

    public List<Object> getPlayerProfiles(List<String> players) {
        Map<Integer, List<String>> serverToPlayerGroup = new HashMap<>();
        List<String> tryOfficialList = new ArrayList<>();

        // Group players by server from cache
        for (String player : players) {
            try {
                Integer serverPid = playerCache.get(player);
                serverToPlayerGroup.computeIfAbsent(serverPid, k -> new ArrayList<>()).add(player);
            } catch (Exception e) {
                log.warn("Player not found in cache, will try default server order: {}", player);
                tryOfficialList.add(player);
            }
        }

        List<Object> responseData = new ArrayList<>();

        // Query servers for cached players
        for (Map.Entry<Integer, List<String>> entry : serverToPlayerGroup.entrySet()) {
            Integer serverPid = entry.getKey();
            List<String> playerGroup = entry.getValue();

            YggdrasilServerModel server = serverPidToServer.get(serverPid);
            String url = server.getProfileApi();

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<List<String>> request = new HttpEntity<>(playerGroup, headers);

                log.info("Url = {} Data = {}", url, playerGroup);
                ResponseEntity<Object[]> response = restTemplate.postForEntity(url, request, Object[].class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("Get PlayerData from {}", server.getName());
                    Collections.addAll(responseData, response.getBody());
                } else {
                    log.error("Can't get PlayerData from {}", server.getName());
                }
            } catch (Exception e) {
                log.error("Error querying server {} for player data", server.getName(), e);
            }
        }

        // Query uncached players using default order
        if (!tryOfficialList.isEmpty()) {
            for (YggdrasilServerModel server : yggdrasilServers) {
                String url = server.getProfileApi();

                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<List<String>> request = new HttpEntity<>(tryOfficialList, headers);

                    log.info("Url = {} Data = {}", url, tryOfficialList);
                    ResponseEntity<Object[]> response = restTemplate.postForEntity(url, request, Object[].class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        log.info("Get PlayerData from {}", server.getName());
                        Collections.addAll(responseData, response.getBody());
                        break; // Found player data, no need to check other servers
                    } else {
                        log.error("Can't get PlayerData from {}", server.getName());
                    }
                } catch (Exception e) {
                    log.error("Error querying server {} for uncached player data", server.getName(), e);
                }
            }
        }

        log.info("All PlayerData = {}", responseData);
        return responseData;
    }

    public String hasJoined(String username, String serverId) {
        for (YggdrasilServerModel server : yggdrasilServers) {
            String url = String.format("%s/session/minecraft/hasJoined?username=%s&serverId=%s",
                    server.getUrl(), username, serverId);

            log.info("Checking hasJoined: {}", url);

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("Find player on server {}", server.getName());

                    // Save player and server to cache
                    playerCache.set(username, server.getPid());

                    return response.getBody();
                } else {
                    log.warn("Can't find player on server {}", server.getName());
                }
            } catch (HttpClientErrorException.NotFound e) {
                log.warn("Player not found on server {}", server.getName());
            } catch (Exception e) {
                log.error("Error checking hasJoined on server {}", server.getName(), e);
            }
        }

        log.error("Error 204 找不到玩家信息");
        return null; // Return null to indicate 204 No Content
    }
}
