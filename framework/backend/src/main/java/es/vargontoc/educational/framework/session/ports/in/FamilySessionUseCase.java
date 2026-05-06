package es.vargontoc.educational.framework.session.ports.in;

import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.model.FamilySessionResult;

public interface FamilySessionUseCase {

    FamilySessionResult authenticate(Long familyId, String rawPin);

    void logout(String rawToken);

    void revokeAllByFamily(Long familyId);

    FamilySession getByToken(String rawToken);
}
