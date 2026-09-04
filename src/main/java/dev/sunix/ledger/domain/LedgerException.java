package dev.sunix.ledger.domain;

import org.springframework.http.HttpStatus;

public final class LedgerException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public LedgerException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
