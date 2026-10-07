package io.github.omegasleepy;

import io.github.omegasleepy.data.UserFactory;
import io.github.omegasleepy.llm.AgentScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static App app;
    public static Logger logger;

    static {
        try {
            app = new App();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main (String[] args) throws SQLException, java.io.IOException {
        logger = LoggerFactory.getLogger(Main.class);
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        try (AgentScheduler scheduler = new AgentScheduler(app.agentService, new UserFactory(), 5, 1, TimeUnit.SECONDS, 250, TimeUnit.MILLISECONDS); ScheduledExecutorService shutdownExecutor = Executors.newSingleThreadScheduledExecutor()) {
            scheduler.start();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime shutdownTime = now.with(LocalTime.of(0, 20));
            if (!shutdownTime.isAfter(now)) {
                shutdownTime = shutdownTime.plusDays(1);
            }
            long delay = Duration.between(now, shutdownTime).toMillis();
            logger.info("Agent scheduler will shut down at {}", shutdownTime);
            shutdownExecutor.schedule(() -> {
                try {
                    logger.info("Scheduled shutdown reached.");
                    scheduler.stop();
                } finally {
                    shutdownLatch.countDown();
                }
            }, delay, TimeUnit.MILLISECONDS);
            try {
                shutdownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Main thread interrupted. Shutting down scheduler.");
                scheduler.stop();
            }
        }
    }
}