package com.kreconomy.monitor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "app.ecos.api-key=test",
        "app.gemini.api-key=",
        "DB_HOST=localhost",
        "DB_USER=test",
        "DB_PASSWORD=test"
})
class KrEconomyMonitorApplicationTests {

    @Test
    void contextLoads() {
        // Spring Context 로딩 확인
    }
}
