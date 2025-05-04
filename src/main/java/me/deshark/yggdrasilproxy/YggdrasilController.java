package me.deshark.yggdrasilproxy;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class YggdrasilController {

    @Autowired
    private YggdrasilService yggdrasilService;

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("Hello", "World");
        return response;
    }

    @PostMapping("/api/profiles/minecraft")
    public ResponseEntity<Object> profilesMinecraft(@RequestBody List<String> reqBody) {
        log.info("Server try to get PlayerInfo req = {}", reqBody);
        return ResponseEntity.ok(yggdrasilService.getPlayerProfiles(reqBody));
    }

    @GetMapping("/sessionserver/session/minecraft/hasJoined")
    public ResponseEntity<Object> hasJoined(
            @RequestParam @Size(min = 3, max = 16) @Pattern(regexp = "^([a-zA-Z0-9_]+)$") String username,
            @RequestParam @Size(min = 1, max = 64) String serverId) {

        log.info("Player {} try to join Server {}", username, serverId);

        try {
            Object response = yggdrasilService.hasJoined(username, serverId);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing hasJoined request", e);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }
}