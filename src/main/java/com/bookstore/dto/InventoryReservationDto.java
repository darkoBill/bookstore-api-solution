package com.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservationDto(
    @NotNull UUID bookId,
    @NotNull @Positive Integer quantity,
    Instant reservedAt,
    Instant expiresAt,
    String customerReference
) {}