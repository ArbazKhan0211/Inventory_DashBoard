package com.example.inventory.dto;

import com.example.inventory.model.Movement;

import java.util.List;

/**
 * Response returned by POST /api/verify-file.
 */
public class VerifyFileResponse {

    private boolean valid;
    private String message;
    private String expectedSha256; // hash recomputed on the backend
    private String providedSha256; // hash the frontend sent
    private int recordCount;
    private List<Movement> movements; // only populated when valid == true

    public static VerifyFileResponse invalid(String providedSha256, String computedSha256, String message) {
        VerifyFileResponse r = new VerifyFileResponse();
        r.valid = false;
        r.message = message;
        r.providedSha256 = providedSha256;
        r.expectedSha256 = computedSha256;
        r.recordCount = 0;
        r.movements = List.of();
        return r;
    }

    public static VerifyFileResponse valid(String sha256, List<Movement> movements) {
        VerifyFileResponse r = new VerifyFileResponse();
        r.valid = true;
        r.message = "SHA-256 verified. File parsed and persisted successfully.";
        r.providedSha256 = sha256;
        r.expectedSha256 = sha256;
        r.recordCount = movements.size();
        r.movements = movements;
        return r;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExpectedSha256() {
        return expectedSha256;
    }

    public void setExpectedSha256(String expectedSha256) {
        this.expectedSha256 = expectedSha256;
    }

    public String getProvidedSha256() {
        return providedSha256;
    }

    public void setProvidedSha256(String providedSha256) {
        this.providedSha256 = providedSha256;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public List<Movement> getMovements() {
        return movements;
    }

    public void setMovements(List<Movement> movements) {
        this.movements = movements;
    }
}
