package dev.sunix.ledger.domain;

import java.time.Instant;
import java.util.UUID;

public record Account(
        UUID id,
        String name,
        String currency,
        EntrySide normalSide,
        boolean allowNegative,
        Instant createdAt) {}
