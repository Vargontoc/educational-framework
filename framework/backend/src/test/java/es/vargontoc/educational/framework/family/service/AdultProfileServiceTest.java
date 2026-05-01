package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.model.AdultProfile;
import es.vargontoc.educational.framework.family.ports.out.AdultProfileRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdultProfileServiceTest {

    @Mock
    private AdultProfileRepository adultProfileRepository;

    @InjectMocks
    private AdultProfileService adultProfileService;

    @Test
    void createAdult_happyPath() {
        when(adultProfileRepository.save(any(AdultProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = adultProfileService.createAdult(1L, "Parent", LocalDate.now().minusYears(30), null);

        assertEquals("Parent", result.getName());
        assertEquals("default-adult", result.getAvatar());
    }

    @Test
    void getAdult_throws404() {
        when(adultProfileRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adultProfileService.getAdult(99L));
    }

    @Test
    void deleteAdult_throws404() {
        when(adultProfileRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adultProfileService.deleteAdult(99L));
    }

    @Test
    void deleteAdult_deletesWhenExists() {
        var adult = new AdultProfile();
        adult.setId(10L);
        when(adultProfileRepository.findById(10L)).thenReturn(Optional.of(adult));

        adultProfileService.deleteAdult(10L);

        verify(adultProfileRepository).deleteById(10L);
    }
}
