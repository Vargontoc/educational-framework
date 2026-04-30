package es.vargontoc.educational.framework;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.liquibase.enabled=false"
})
@ActiveProfiles("test")
class EducationalFrameworkApplicationTests {

    @Test
    void contextLoads() {
    }
}
