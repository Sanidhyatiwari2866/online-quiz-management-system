package com.quiz.service.concurrent;

import com.quiz.service.QuizAttemptService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Background Concurrency Worker Service.
 * Demonstrates Multithreading, ThreadFactory, ScheduledExecutorService, and thread-safe operations.
 * Periodically monitors and auto-expires abandoned timed quiz attempts.
 */
@Component
public class QuizAttemptTimeoutMonitor {

    private static final Logger log = LoggerFactory.getLogger(QuizAttemptTimeoutMonitor.class);
    private final QuizAttemptService attemptService;
    private ScheduledExecutorService scheduler;
    private final Object lock = new Object();

    public QuizAttemptTimeoutMonitor(QuizAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostConstruct
    public void startMonitoring() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Quiz-Timeout-Worker-" + threadNumber.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };

        scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduler.scheduleAtFixedRate(this::checkAndExpireAttempts, 60, 60, TimeUnit.SECONDS);
        log.info("Started QuizAttemptTimeoutMonitor background daemon thread.");
    }

    public void checkAndExpireAttempts() {
        synchronized (lock) {
            try {
                int expired = attemptService.autoExpireTimedOutAttempts();
                if (expired > 0) {
                    log.info("QuizAttemptTimeoutMonitor: Auto-expired {} timed-out quiz attempt(s).", expired);
                }
            } catch (Exception e) {
                log.error("Error during scheduled timeout verification: {}", e.getMessage());
            }
        }
    }

    @PreDestroy
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("Shutdown QuizAttemptTimeoutMonitor background worker thread.");
        }
    }
}
