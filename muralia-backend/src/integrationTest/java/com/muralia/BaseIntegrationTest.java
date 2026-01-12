package com.muralia;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that provides a shared PostgreSQL container
 * using Testcontainers with Spring Boot 3.2's @ServiceConnection.
 *
 * The singleton container pattern ensures the same container instance is reused
 * across all test classes, preventing connection issues when running the full test suite.
 *
 * All integration tests should extend this class.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgresTestContainer postgres = PostgresTestContainer.getInstance();

    static {
        postgres.start();
    }
}
