package dev.sunix.ledger.service;

import dev.sunix.ledger.domain.Account;
import dev.sunix.ledger.domain.LedgerException;
import dev.sunix.ledger.domain.Posting;
import dev.sunix.ledger.persistence.LedgerRepository;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final LedgerRepository repository;
    private final Clock clock;

    public AccountService(LedgerRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Account create(String name, String currency, dev.sunix.ledger.domain.EntrySide normalSide, boolean allowNegative) {
        Account account = new Account(
                UUID.randomUUID(),
                name.trim(),
                currency.toUpperCase(Locale.ROOT),
                normalSide,
                allowNegative,
                clock.instant());
        repository.insertAccount(account);
        return account;
    }

    public Account get(UUID id) {
        return repository.findAccount(id).orElseThrow(() -> new LedgerException(
                HttpStatus.NOT_FOUND, "account_not_found", "Account %s does not exist".formatted(id)));
    }

    public long balance(UUID id) {
        get(id);
        return repository.balance(id);
    }

    public List<Posting> postings(UUID id, int limit, int offset) {
        get(id);
        return repository.accountPostings(id, limit, offset);
    }
}
