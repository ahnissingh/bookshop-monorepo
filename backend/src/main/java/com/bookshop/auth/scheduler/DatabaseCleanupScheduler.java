package com.bookshop.auth.scheduler;

import com.bookshop.auth.repository.UserRepository;
import com.bookshop.shared.repository.SecureTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


/**
 *  Daily Database Janitor
 * Runs every day at 03:00 AM server time.
 * * IMPORTANT ARCHITECTURE NOTE:
 * Currently designed for a single-instance deployment (e.g., one EC2 instance).
 * If this application is ever scaled horizontally (e.g., ECS, Kubernetes with multiple pods),
 * Will  implement a distributed lock mechanism like 'ShedLock' or 'Redis Locks'.
 * Otherwise, all nodes will fire this job simultaneously, causing severe DB deadlocks.
 * * Cleanup Strategy:
 * 1. Purges successful verification audits older than 7 days.
 * 2. Purges unused/expired tokens older than 24 hours.
 * 3. Purges ghost users (enabled=false) created more than 24 hours ago.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseCleanupScheduler {
    private final SecureTokenRepository secureTokenRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDatabase() {
        log.info("Starting daily database cleanup job...");
        Instant now = Instant.now();

        try {
            //  Delete tokens that were successfully used > 7 days ago
            Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
            int deletedAudits = secureTokenRepository.deleteValidatedTokensOlderThan(sevenDaysAgo);

            //  Delete tokens that expired > 24 hours ago and were never used
            Instant oneDayAgo = now.minus(24, ChronoUnit.HOURS);
            int deletedOrphans = secureTokenRepository.deleteExpiredUnusedTokensOlderThan(oneDayAgo);

            //  Delete  users created > 24 hours ago who never verified
            int deletedGhosts = userRepository.deleteUnverifiedUsersOlderThan(oneDayAgo);

            log.info(" Cleanup finished successfully. Purged: {} old audits, {} expired tokens, {} ghost users.",
                    deletedAudits, deletedOrphans, deletedGhosts);

        } catch (Exception e) {
            log.error(" Database cleanup job failed!", e);
            throw e;
        }
    }
}
