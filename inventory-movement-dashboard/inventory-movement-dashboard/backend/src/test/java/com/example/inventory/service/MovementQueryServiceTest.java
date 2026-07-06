package com.example.inventory.service;

import com.example.inventory.model.Movement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovementQueryServiceTest {

    private MovementQueryService queryService;

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = """
                [
                  {"id":"mv1","timestamp":"2026-01-01T10:00:00Z","sku":"SKU001","movementType":"IN","quantity":10,"warehouse":"WH-NORTH"},
                  {"id":"mv2","timestamp":"2026-01-02T10:00:00Z","sku":"SKU001","movementType":"OUT","quantity":4,"warehouse":"WH-SOUTH"},
                  {"id":"mv3","timestamp":"2026-01-10T10:00:00Z","sku":"SKU002","movementType":"IN","quantity":7,"warehouse":"WH-NORTH"}
                ]
                """;
        List<Movement> movements = mapper.readValue(json.getBytes(StandardCharsets.UTF_8),
                mapper.getTypeFactory().constructCollectionType(List.class, Movement.class));

        MovementDataStore dataStore = Mockito.mock(MovementDataStore.class);
        Mockito.when(dataStore.getAll()).thenReturn(movements);

        queryService = new MovementQueryService(dataStore);
    }

    @Test
    void filtersByInclusiveDateRange() {
        List<Movement> result = queryService.findMovements(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), null, null);
        assertEquals(2, result.size());
    }

    @Test
    void filtersByMovementType() {
        List<Movement> result = queryService.findMovements(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "IN", null);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> "IN".equals(m.getMovementType())));
    }

    @Test
    void allTypeReturnsEverythingInRange() {
        List<Movement> result = queryService.findMovements(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "ALL", null);
        assertEquals(3, result.size());
    }

    @Test
    void filtersByWarehouse() {
        List<Movement> result = queryService.findMovements(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, "WH-NORTH");
        assertEquals(2, result.size());
    }

    @Test
    void resultsAreSortedByTimestampAscending() {
        List<Movement> result = queryService.findMovements(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, null);
        assertEquals("mv1", result.get(0).getId());
        assertEquals("mv3", result.get(2).getId());
    }
}
