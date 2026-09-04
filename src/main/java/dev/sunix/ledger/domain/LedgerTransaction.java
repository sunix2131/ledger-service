package dev.sunix.ledger.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LedgerTransaction(
        UUID id,
        String reference,
        String description,
        UUID reversalOf,
        String createdBy,
        String requestId,
        Instant createdAt,
        List<Posting> postings) {}
