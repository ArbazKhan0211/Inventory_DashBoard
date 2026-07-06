package com.example.inventory.service;

import com.example.inventory.model.Movement;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Applies query-param filters (date range, movement type, warehouse) to the
 * dataset currently held by {@link MovementDataStore}.
 */
@Service
public class MovementQueryService {

    private final MovementDataStore dataStore;

    public MovementQueryService(MovementDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Movement> findMovements(LocalDate from, LocalDate to, String type, String warehouse) {
        // Treat "from"/"to" as an inclusive whole-day range in UTC.
        var fromInstant = from != null ? from.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        var toInstant = to != null ? to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;

        String normalizedType = (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type))
                ? null
                : type.trim().toUpperCase();

        String normalizedWarehouse = (warehouse == null || warehouse.isBlank() || "ALL".equalsIgnoreCase(warehouse))
                ? null
                : warehouse.trim();

        return dataStore.getAll().stream()
                .filter(m -> fromInstant == null || !m.getTimestamp().isBefore(fromInstant))
                .filter(m -> toInstant == null || m.getTimestamp().isBefore(toInstant))
                .filter(m -> normalizedType == null || normalizedType.equalsIgnoreCase(m.getMovementType()))
                .filter(m -> normalizedWarehouse == null || normalizedWarehouse.equalsIgnoreCase(m.getWarehouse()))
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();
    }
}
