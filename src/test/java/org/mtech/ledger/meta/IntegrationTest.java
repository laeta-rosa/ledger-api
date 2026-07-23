package org.mtech.ledger.meta;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;

/**
 * Meta-annotation for the project's integration tests, keeping their shared configuration
 * in one place. It boots the full application on a random port, enables constructor
 * injection of test dependencies ({@link TestConstructor}), and truncates every table
 * before each test via {@code cleanup.sql} so the shared in-memory H2 stays isolated.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestConstructor(autowireMode = ALL)
@Sql(scripts = "/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
public @interface IntegrationTest {}