package org.example.service;

import org.example.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class LedgerClient {
    // WebClient para llamar al ledger
    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(LedgerClient.class);

    public LedgerClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Transaction> postEntry(Transaction transaction) {
        log.info("POST /ledger/entries - Request body: {}", transaction);
        return webClient.post()
                .uri("/ledger/entries")
                .bodyValue(transaction)
                .retrieve()
                .bodyToMono(Transaction.class)
                .doOnNext(response -> log.info("Response from /ledger/entries - Response body: {}", response))
                .doOnError(error -> log.error("Error calling /ledger/entries", error));
    }
}