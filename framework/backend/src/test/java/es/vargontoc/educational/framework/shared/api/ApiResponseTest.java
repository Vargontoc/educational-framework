package es.vargontoc.educational.framework.shared.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiResponseTest {

    @Test
    void ok_setsSuccessAndData() {
        var response = ApiResponse.ok("payload");
        assertTrue(response.success());
        assertEquals("payload", response.data());
        assertNull(response.message());
        assertNull(response.errors());
    }

    @Test
    void created_setsSuccessAndData() {
        var response = ApiResponse.created(42L);
        assertTrue(response.success());
        assertEquals(42L, response.data());
    }

    @Test
    void error_setsFailureAndMessage() {
        var response = ApiResponse.error("something failed");
        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("something failed", response.message());
        assertNull(response.errors());
    }

    @Test
    void error_withErrors_setsErrorList() {
        var errors = List.of("field1 required", "field2 too long");
        var response = ApiResponse.<Void>error("validation failed", errors);
        assertFalse(response.success());
        assertEquals("validation failed", response.message());
        assertEquals(2, response.errors().size());
    }
}
