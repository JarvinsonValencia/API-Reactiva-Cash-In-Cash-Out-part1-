package org.example.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class LedgerRouterHandler {

    @Bean
    public RouterFunction<ServerResponse> router(){
        return RouterFunctions.route(POST("/ledger/entries"), this::createEntry);
    }

    public Mono<ServerResponse> createEntry(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", UUID.randomUUID().toString());
                    result.put("amount", body.get("amount"));
                    result.put("currency", body.get("currency"));
                    result.put("type", body.get("type"));
                    result.put("status", "POSTED");
                    result.put("createdAt", Instant.now());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(result);
                });

    }
}