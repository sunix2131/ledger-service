CREATE TYPE entry_side AS ENUM ('DEBIT', 'CREDIT');

CREATE TABLE account (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    normal_side entry_side NOT NULL,
    allow_negative BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ledger_transaction (
    id UUID PRIMARY KEY,
    reference VARCHAR(160) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    reversal_of UUID REFERENCES ledger_transaction(id),
    created_by VARCHAR(160) NOT NULL,
    request_id VARCHAR(160) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX one_reversal_per_transaction
    ON ledger_transaction (reversal_of)
    WHERE reversal_of IS NOT NULL;

CREATE TABLE posting (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES ledger_transaction(id),
    account_id UUID NOT NULL REFERENCES account(id),
    side entry_side NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX posting_account_history ON posting (account_id, created_at, id);
CREATE INDEX posting_transaction ON posting (transaction_id);

CREATE TABLE idempotency_record (
    idempotency_key VARCHAR(200) PRIMARY KEY,
    request_hash CHAR(64) NOT NULL,
    transaction_id UUID NOT NULL REFERENCES ledger_transaction(id),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE FUNCTION reject_ledger_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION '% rows are immutable; create a reversal instead', TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER immutable_transaction
    BEFORE UPDATE OR DELETE ON ledger_transaction
    FOR EACH ROW EXECUTE FUNCTION reject_ledger_mutation();

CREATE TRIGGER immutable_posting
    BEFORE UPDATE OR DELETE ON posting
    FOR EACH ROW EXECUTE FUNCTION reject_ledger_mutation();
