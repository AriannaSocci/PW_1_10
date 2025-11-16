package org.ancora_casini.PW_1_10.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketDataService {

    private final SimpMessagingTemplate messagingTemplate;

    private final DashboardService dashboardService;

    /**
     * Send dashboard updates to all connected WebSocket clients every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void sendDashboardUpdates() {
        try {
            var now = OffsetDateTime.now();
            Map<String, Object> dashboardData = dashboardService.getDashboardData(now.minusMinutes(1), now);

            dashboardData.put("lastUpdate", OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            messagingTemplate.convertAndSend("/topic/dashboard-updates", dashboardData);

            log.info("[{}] Dashboard update sent via WebSocket.", OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        } catch (Exception e) {
            log.error("Error sending dashboard updates via WebSocket: {}", e.getMessage());
        }
    }

}
