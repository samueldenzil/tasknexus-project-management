package com.denzil.project_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Overrides the real PostgreSQL datasource with an in-memory H2 database so the
// context can boot in CI / local test runs without a live DB instance.
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=test-secret-key-that-is-long-enough-for-hs256-algorithm",
        "app.jwt.expiration-ms=3600000"
})
class ProjectManagementApplicationTests {

    @Test
    void contextLoads() {
    }

}
