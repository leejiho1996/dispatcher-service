package com.polarbookshop.dispatcherservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@FunctionalSpringBootTest
public class DispatchingFunctionsIntegrationTests {

    @Autowired
    private FunctionCatalog catalog;

    @Test
    void packOrder() {
        Function<OrderAcceptedMessage, Long> pack = catalog.lookup(Function.class, "pack");
        long order = 121;
        assertThat(pack.apply(new OrderAcceptedMessage(order))).isEqualTo(order);
    }

    @Test
    void labelOrder() {
        Function<Flux<Long>, Flux<OrderDispatchedMessage>> label = catalog.lookup(Function.class, "label");
        Flux<Long> orderId = Flux.just(121L);

        StepVerifier.create(label.apply(orderId))
                .expectNextMatches(a -> a.equals(new OrderDispatchedMessage(121L)))
                .verifyComplete();
    }

    @Test
    void packAndLabelOrder() {
        Function<OrderAcceptedMessage, Flux<OrderDispatchedMessage>> packAndLabel =
                catalog.lookup(Function.class, "pack|label");

        long orderId = 121;

        StepVerifier.create(packAndLabel.apply(new OrderAcceptedMessage(orderId)))
                .expectNextMatches(dispatchedOrder ->
                        dispatchedOrder.equals(new OrderDispatchedMessage(orderId)))
                .verifyComplete();
    }

}
