package dev.sunix.ledger.domain;

public enum EntrySide {
    DEBIT,
    CREDIT;

    public EntrySide opposite() {
        return this == DEBIT ? CREDIT : DEBIT;
    }
}
