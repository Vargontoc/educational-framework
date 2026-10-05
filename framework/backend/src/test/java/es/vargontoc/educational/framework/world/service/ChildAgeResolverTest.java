package es.vargontoc.educational.framework.world.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChildAgeResolverTest {

    @Test
    void resolveEffectiveAge_returns3() {
        ChildAgeResolver resolver = new ChildAgeResolver();
        assertEquals(3, resolver.resolveEffectiveAge());
    }

    @Test
    void resolveEffectiveAgeBoxed_returns3() {
        ChildAgeResolver resolver = new ChildAgeResolver();
        assertEquals(Integer.valueOf(3), resolver.resolveEffectiveAgeBoxed());
    }
}
