package es.vargontoc.educational.framework.family.ports.in;

import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateFamilyRequest;
import es.vargontoc.educational.framework.family.model.Family;

public interface FamilyUseCase {

    Family createFamily(String name, String rawPin, boolean ttsEnabled, boolean agentEnabled);

    Family getFamily();

    Family updateFamily(UpdateFamilyRequest request);

    boolean familyExists();
}
