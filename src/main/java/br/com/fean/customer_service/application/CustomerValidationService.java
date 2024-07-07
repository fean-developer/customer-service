package br.com.fean.customer_service.application;

import br.com.fean.customer_service.domain.entities.Customer;
import br.com.fean.customer_service.domain.entities.Event;
import br.com.fean.customer_service.domain.entities.History;
import br.com.fean.customer_service.infrastructure.broker.producer.CustomerProducer;
import br.com.fean.customer_service.infrastructure.exceptions.ValidationException;
import br.com.fean.customer_service.infrastructure.repositories.CustomerRepository;
import br.com.fean.customer_service.utils.CpfValidator;
import br.com.fean.customer_service.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.fean.customer_service.domain.enums.ESagaStatus.*;
import static org.springframework.util.ObjectUtils.isEmpty;
@Slf4j
@Service
@AllArgsConstructor
public class CustomerValidationService {

    private static final String CURRENT_SOURCE = "CUSTOMER_SERVICE";
    private final CustomerRepository customerRepository;
    private final JsonUtil jsonUtil;
    private final CustomerProducer producer;

    public void validateCustomer(Event event) {
        try {
            checkCurrentValidation(event);
            handleSuccess(event);
        } catch (Exception ex) {
            log.error("Error trying to validate customer data: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
            realizeRefund(event);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }
    private void validateCustomerInformed(Event event) {

        boolean  notHavePayload = isEmpty(event.getPayload());
        boolean  notHaveCustomer = isEmpty(event.getPayload().getCustomer());
        boolean  notHaveName = isEmpty(event.getPayload().getCustomer().getName());
        boolean  notHaveCpf = isEmpty(event.getPayload().getCustomer().getCpf());

        if (notHavePayload || notHaveCustomer || notHaveName || notHaveCpf) {
            throw new ValidationException("Customer must be informed.");
        }

        if (isEmpty(event.getPayload().getId()) || isEmpty(event.getTransactionId())) {
            throw new ValidationException("OrderID and TransactionID must be informed!");
        }

    }

    private void validateExistingCustomer(Event event) {
        customerRepository.findByCpf(event.getPayload().getCustomer().getCpf()).orElseThrow(
                () -> new ValidationException("Customer not found.")
        );
    }

    // cretate validation for cpf at and using the class CpfValidator
    private void validateCpf(Event event) {
        if(!CpfValidator.isValid(event.getPayload().getCustomer().getCpf())) {
            throw new ValidationException("Invalid CPF.");
        }
    }


    private void checkCurrentValidation(Event event) {
        validateCustomerInformed(event);
        validateExistingCustomer(event);
        validateCpf(event);
    }

    private void handleSuccess(Event event) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Customer Validated with success!");
    }

    private void addHistory(Event event, String message) {
        var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail customer not validated: ".concat(message));
    }

    public void realizeRefund(Event event) {
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Customer not validated. Refund realized!");
        producer.sendEvent(jsonUtil.toJson(event));
    }
}
