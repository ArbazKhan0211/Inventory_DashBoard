package com.example.inventory.controller;

import com.example.inventory.model.Movement;
import com.example.inventory.service.MovementQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class MovementController {

    private final MovementQueryService queryService;

    public MovementController(MovementQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * GET /api/movements?from=YYYY-MM-DD&to=YYYY-MM-DD&type=IN|OUT&warehouse=WH-NORTH
     *
     * Reads from the backend's currently stored dataset (seed data, or the last
     * successfully uploaded/verified file) and applies the requested filters.
     */
    @GetMapping("/api/movements")
    public List<Movement> getMovements(
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String warehouse
    ) {
        return queryService.findMovements(from, to, type, warehouse);
    }
}
