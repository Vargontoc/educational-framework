package es.vargontoc.educational.framework;

import es.vargontoc.educational.framework.family.ports.out.AdultProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest()
@ActiveProfiles("test")
class EducationalFrameworkApplicationTests extends TestcontainersConfiguration {

    @MockitoBean
    private FamilyRepository familyRepository;

    @MockitoBean
    private ChildProfileRepository childProfileRepository;

    @MockitoBean
    private AdultProfileRepository adultProfileRepository;

    @Test
    void contextLoads() {
    }
}
