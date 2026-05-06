package es.vargontoc.educational.framework.session.model;

public record FamilySessionResult(String rawToken, FamilySession session) {

    @Override
    public String toString() {
        return "FamilySessionResult[session=" + session + "]";
    }
}
