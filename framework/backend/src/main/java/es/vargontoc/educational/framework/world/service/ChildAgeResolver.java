package es.vargontoc.educational.framework.world.service;

public class ChildAgeResolver {

    private static final int DEFAULT_AGE = 3;

    public int resolveEffectiveAge() {
        return DEFAULT_AGE;
    }

    public Integer resolveEffectiveAgeBoxed() {
        return DEFAULT_AGE;
    }
}
