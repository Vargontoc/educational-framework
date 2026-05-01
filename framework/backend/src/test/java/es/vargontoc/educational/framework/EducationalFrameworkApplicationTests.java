package es.vargontoc.educational.framework;

import es.vargontoc.educational.framework.family.ports.out.AdultProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
class EducationalFrameworkApplicationTests {

    @MockBean
    private FamilyRepository familyRepository;

    @MockBean
    private ChildProfileRepository childProfileRepository;

    @MockBean
    private AdultProfileRepository adultProfileRepository;

    @Test
    void contextLoads() {
    }
}
