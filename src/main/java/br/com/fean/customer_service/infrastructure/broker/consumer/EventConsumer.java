package br.com.fean.customer_service.infrastructure.broker.consumer;

import br.com.fean.customer_service.application.CustomerValidationService;
import br.com.fean.customer_service.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final JsonUtil jsonUtil;
    private final CustomerValidationService customerValidationService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.validate-customer-success}"
    )
    public void consumerSuccessEvent(String payload) {

        log.info("Receiving success event {} from validate-customer-success topic", payload);
        var event = jsonUtil.toEvent(payload);
        customerValidationService.validateCustomer(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.validate-customer-fail}"
    )
    public void consumerFailEvent(String payload) {

        log.info("Receiving fail event {} from customer-validation-fail topic", payload);
        var event = jsonUtil.toEvent(payload);
        customerValidationService.realizeRefund(event);
    }

}
