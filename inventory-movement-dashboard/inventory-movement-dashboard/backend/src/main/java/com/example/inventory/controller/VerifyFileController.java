package com.example.inventory.controller;

import com.example.inventory.dto.VerifyFileResponse;
import com.example.inventory.model.Movement;
import com.example.inventory.service.FileVerificationService;
import com.example.inventory.service.MovementDataStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class VerifyFileController {

    private static final Logger log = LoggerFactory.getLogger(VerifyFileController.class);

    private final FileVerificationService verificationService;
    private final MovementDataStore dataStore;
    private final ObjectMapper objectMapper;

    public VerifyFileController(FileVerificationService verificationService,
                                 MovementDataStore dataStore,
                                 ObjectMapper objectMapper) {
        this.verificationService = verificationService;
        this.dataStore = dataStore;
        this.objectMapper = objectMapper;
    }

    /**
     * POST /api/verify-file
     * multipart/form-data:
     *   - file: the JSON file
     *   - sha256: the SHA-256 hex digest computed by the frontend
     *
     * 1. Recomputes SHA-256 over the raw uploaded bytes.
     * 2. Compares against the client-supplied digest.
     * 3. If they match: parses the JSON, persists it as the new backend dataset,
     *    and returns the parsed records so the UI can render immediately.
     * 4. If they don't match: returns valid=false with both hashes for debugging,
     *    the stored dataset is left untouched.
     */
    @PostMapping(value = "/api/verify-file", consumes = "multipart/form-data")
    public ResponseEntity<VerifyFileResponse> verifyFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sha256") String sha256
    ) {
        try {
            byte[] bytes = file.getBytes();
            String computedHash = verificationService.sha256Hex(bytes);
            boolean matches = computedHash.equalsIgnoreCase(sha256 == null ? "" : sha256.trim());

            if (!matches) {
                log.warn("SHA-256 mismatch for uploaded file '{}': provided={} computed={}",
                        file.getOriginalFilename(), sha256, computedHash);
                return ResponseEntity.ok(VerifyFileResponse.invalid(
                        sha256, computedHash,
                        "SHA-256 mismatch: the digest computed on the server does not match the one provided by the client."));
            }

            // Hash matches -> parse JSON before persisting, so a malformed file never corrupts the store.
            List<Movement> parsed;
            try {
                parsed = objectMapper.readValue(bytes,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Movement.class));
            } catch (IOException parseError) {
                log.warn("Uploaded file passed SHA-256 verification but failed JSON parsing: {}", parseError.getMessage());
                return ResponseEntity.badRequest().body(VerifyFileResponse.invalid(
                        sha256, computedHash,
                        "SHA-256 matched, but the file could not be parsed as a JSON array of movement records: "
                                + parseError.getMessage()));
            }

            dataStore.replaceAndPersist(bytes);
            log.info("Uploaded file '{}' verified and persisted with {} records", file.getOriginalFilename(), parsed.size());

            return ResponseEntity.ok(VerifyFileResponse.valid(computedHash, parsed));

        } catch (IOException e) {
            log.error("Failed to read uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerifyFileResponse.invalid(sha256, null, "Failed to read uploaded file: " + e.getMessage()));
        }
    }
}
