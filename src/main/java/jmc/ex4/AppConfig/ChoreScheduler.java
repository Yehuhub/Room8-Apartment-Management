package jmc.ex4.AppConfig;

import jmc.ex4.services.ChoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChoreScheduler {

    private final ChoreService choreService;

    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
    public void updateOverdueChores() {
        choreService.markOverdueChores();
    }
}