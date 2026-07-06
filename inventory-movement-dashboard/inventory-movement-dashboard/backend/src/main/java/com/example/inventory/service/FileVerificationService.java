package com.example.inventory.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Recomputes the SHA-256 digest of uploaded file bytes on the backend, so the
 * server never simply trusts a hash sent by the client.
 */
@Service
public class FileVerificationService {

    public String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every standard JVM.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public boolean matches(byte[] bytes, String expectedHex) {
        if (expectedHex == null) {
            return false;
        }
        String computed = sha256Hex(bytes);
        return computed.equalsIgnoreCase(expectedHex.trim());
    }
}
