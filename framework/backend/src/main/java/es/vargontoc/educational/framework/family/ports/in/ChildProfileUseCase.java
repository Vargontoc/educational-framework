package es.vargontoc.educational.framework.family.ports.in;

import es.vargontoc.educational.framework.family.model.ChildProfile;

import java.time.LocalDate;
import java.util.List;

public interface ChildProfileUseCase {

    ChildProfile createChild(
        Long familyId,
        String name,
        LocalDate birthday,
        String avatar,
        boolean ttsEnabled,
        boolean agentEnabled
    );

    ChildProfile getChild(Long id);

    List<ChildProfile> getAllChildren();

    ChildProfile updateChild(
        Long id,
        String name,
        LocalDate birthday,
        String avatar,
        boolean ttsEnabled,
        boolean agentEnabled
    );

    ChildProfile changeActiveState(Long id);

    void deleteChild(Long id);
}
