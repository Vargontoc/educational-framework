package es.vargontoc.educational.framework;

import es.vargontoc.educational.framework.family.ports.out.AdultProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("test")
class EducationalFrameworkApplicationTests extends TestcontainersConfiguration {

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
