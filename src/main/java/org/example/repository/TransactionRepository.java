package org.example.repository;

import org.example.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    // Aquí puedes definir métodos personalizados si es necesario
    // Por ejemplo, para encontrar transacciones por tipo o estado
    // Mono<Transaction> findByType(String type);
    // Flux<Transaction> findByStatus(String status);
}
