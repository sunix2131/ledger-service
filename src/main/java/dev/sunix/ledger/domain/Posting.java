package dev.sunix.ledger.domain;

import java.time.Instant;
import java.util.UUID;

public record Posting(
        UUID id,
        UUID transactionId,
        UUID accountId,
        EntrySide side,
        long amountMinor,
        String currency,
        Instant createdAt) {}
