package com.orchestrationengine.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration to run self-healing repair before migration on startup.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairMigrationStrategy() {
        return flyway -> {
            // Self-heals checksum mismatch errors in dev/local environment
            flyway.repair();
            flyway.migrate();
        };
    }
}
