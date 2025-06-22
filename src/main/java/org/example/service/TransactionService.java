package org.example.service;

import org.example.dto.CashRequestDto;
import org.example.dto.TransactionDto;
import org.example.model.Transaction;
import org.example.model.TransactionStatus;
import org.example.model.TransactionType;
import org.example.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Instant;

@Service
public class TransactionService {
    // cashIn, cashOut, findById, etc.
    private final TransactionRepository transactionRepository;
    private final LedgerClient ledgerClient;

    public TransactionService(TransactionRepository transactionRepository, LedgerClient ledgerClient) {
        this.transactionRepository = transactionRepository;
        this.ledgerClient = ledgerClient;
    }

    public Mono<TransactionDto> cashIn(CashRequestDto requestDto){
        Transaction transaction = Transaction.builder()
                .amount(requestDto.amount())
                .currency(requestDto.currency())
                .type(TransactionType.CASH_IN.name())
                .status(TransactionStatus.PENDING.name())
                .createdAt(Instant.now().toString())
                .build();

        return transactionRepository.save(transaction)
                .flatMap(ledgerClient::postEntry)
                .map(this::toDto)
                .retryWhen(Retry.fixedDelay(4, java.time.Duration.ofSeconds(1)))
                .onErrorResume(e -> rollback(transaction, e));
    }

    public Mono<TransactionDto> cashOut(CashRequestDto requestDto){
        Transaction transaction = Transaction.builder()
                .amount(requestDto.amount())
                .currency(requestDto.currency())
                .type(TransactionType.CASH_OUT.name())
                .status(TransactionStatus.PENDING.name())
                .createdAt(Instant.now().toString())
                .build();

        return transactionRepository.save(transaction)
                .flatMap(ledgerClient::postEntry)
                .map(this::toDto)
                .retryWhen(Retry.fixedDelay(4, java.time.Duration.ofSeconds(1)))
                .onErrorResume(e -> rollback(transaction, e));
    }

    public Mono<TransactionDto> findById(String id) {
        return transactionRepository.findById(id)
                .map(this::toDto);
        // .switchIfEmpty(Mono.error(new ChangeSetPersister.NotFoundException()));
    }

    private Mono<TransactionDto> rollback(Transaction transaction, Throwable e) {
        transaction.setStatus((TransactionStatus.FAILED.name()));
        return transactionRepository.save(transaction).then(Mono.error(e));
    }

    private TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                TransactionType.valueOf(transaction.getType()),
                TransactionStatus.valueOf(transaction.getStatus()),
                Instant.parse(transaction.getCreatedAt()));
    }
}