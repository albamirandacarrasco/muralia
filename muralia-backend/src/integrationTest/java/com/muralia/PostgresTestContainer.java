package com.muralia;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton PostgreSQL container shared across all integration tests.
 * This ensures the same container instance is reused across all test classes,
 * preventing connection issues when running the full test suite.
 */
public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

    private static final String IMAGE_VERSION = "postgres:15-alpine";
    private static PostgresTestContainer container;

    private PostgresTestContainer() {
        super(IMAGE_VERSION);
    }

    public static PostgresTestContainer getInstance() {
        if (container == null) {
            container = new PostgresTestContainer()
                    .withDatabaseName("muralia_test")
                    .withUsername("test")
                    .withPassword("test");
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        // Do nothing, JVM handles shut down
    }
}
