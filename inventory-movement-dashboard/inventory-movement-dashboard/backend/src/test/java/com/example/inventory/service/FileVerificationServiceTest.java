package com.example.inventory.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileVerificationServiceTest {

    private final FileVerificationService service = new FileVerificationService();

    @Test
    void sha256HexProducesKnownDigestForEmptyInput() {
        // SHA-256("") is a well-known constant, useful as a sanity check on the implementation.
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        String actual = service.sha256Hex(new byte[0]);
        assertEquals(expected, actual);
        assertEquals(64, actual.length());
    }

    @Test
    void sameBytesProduceSameHash() {
        byte[] data = "{\"id\":\"mv1\"}".getBytes(StandardCharsets.UTF_8);
        String h1 = service.sha256Hex(data);
        String h2 = service.sha256Hex(data);
        assertEquals(h1, h2);
    }

    @Test
    void matchesReturnsTrueForCorrectHashIgnoringCaseAndWhitespace() {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        String hash = service.sha256Hex(data);
        assertTrue(service.matches(data, hash.toUpperCase() + "\n"));
    }

    @Test
    void matchesReturnsFalseForIncorrectHash() {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        assertFalse(service.matches(data, "0000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    void matchesReturnsFalseWhenExpectedIsNull() {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        assertFalse(service.matches(data, null));
    }
}
