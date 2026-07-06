package com.example.inventory.service;

import com.example.inventory.model.Movement;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Holds the "backend stored" dataset in memory and on disk.
 *
 * On startup:
 *   - if a runtime data file (from a previous upload) already exists on disk, load it
 *   - otherwise seed from the bundled sample dataset on the classpath
 *
 * After a successful SHA-256 verification (see FileVerificationService), the uploaded
 * JSON bytes are written to the runtime data file and become the new source of truth
 * for GET /api/movements.
 */
@Component
public class MovementDataStore {

    private static final Logger log = LoggerFactory.getLogger(MovementDataStore.class);

    private final ObjectMapper objectMapper;
    private final String runtimeFilePath;
    private final String classpathSeedPath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile List<Movement> movements = Collections.emptyList();

    public MovementDataStore(ObjectMapper objectMapper,
                              @Value("${app.data.runtime-file}") String runtimeFilePath,
                              @Value("${app.data.classpath-seed}") String classpathSeedPath) {
        this.objectMapper = objectMapper;
        this.runtimeFilePath = runtimeFilePath;
        this.classpathSeedPath = classpathSeedPath;
    }

    @PostConstruct
    public void init() {
        try {
            Path runtimePath = Path.of(runtimeFilePath);
            if (Files.exists(runtimePath)) {
                log.info("Loading previously persisted movement data from {}", runtimePath);
                byte[] bytes = Files.readAllBytes(runtimePath);
                loadFromBytes(bytes);
            } else {
                log.info("No runtime data file found yet, seeding from bundled sample dataset: {}", classpathSeedPath);
                try (InputStream is = new ClassPathResource(classpathSeedPath).getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    loadFromBytes(bytes);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize movement data store", e);
        }
    }

    private void loadFromBytes(byte[] bytes) throws IOException {
        List<Movement> parsed = objectMapper.readValue(bytes, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Movement.class));
        lock.writeLock().lock();
        try {
            this.movements = parsed;
        } finally {
            lock.writeLock().unlock();
        }
        log.info("Loaded {} movement records into the data store", parsed.size());
    }

    /**
     * Replaces the in-memory dataset and persists the raw uploaded bytes to disk so that
     * future application restarts (and future GET /api/movements calls) use the new data.
     */
    public void replaceAndPersist(byte[] rawJsonBytes) throws IOException {
        loadFromBytes(rawJsonBytes);

        Path runtimePath = Path.of(runtimeFilePath);
        Path parentDir = runtimePath.toAbsolutePath().getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        Files.write(runtimePath, rawJsonBytes);
        log.info("Persisted uploaded dataset to {}", runtimePath.toAbsolutePath());
    }

    /** Returns an immutable snapshot of the current dataset. */
    public List<Movement> getAll() {
        lock.readLock().lock();
        try {
            return List.copyOf(movements);
        } finally {
            lock.readLock().unlock();
        }
    }
}
