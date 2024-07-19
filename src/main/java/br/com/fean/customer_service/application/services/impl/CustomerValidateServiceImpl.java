package br.com.fean.customer_service.application.services.impl;

import br.com.fean.customer_service.application.services.CustomerValidateService;
import br.com.fean.customer_service.domain.entities.Event;
import br.com.fean.customer_service.infrastructure.broker.producer.CustomerProducer;
import br.com.fean.customer_service.infrastructure.exceptions.ValidationException;
import br.com.fean.customer_service.infrastructure.repositories.CustomerRepository;
import br.com.fean.customer_service.utils.CpfValidator;
import br.com.fean.customer_service.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static br.com.fean.customer_service.domain.enums.ESagaStatus.*;
import static org.springframework.util.ObjectUtils.isEmpty;
@Slf4j
@Service
@AllArgsConstructor
public class CustomerValidateServiceImpl implements CustomerValidateService {

    private static final String CURRENT_SOURCE = "CUSTOMER_SERVICE";
    private final CustomerRepository customerRepository;
    private final JsonUtil jsonUtil;
    private final CustomerProducer producer;
    private final HistoryServiceImpl historyService;

    @Override
    public void validate(Event event) {
        try {
            handleCurrentValidation(event);
        } catch (Exception ex) {
            log.error("LOG :: Error trying to validate customer data: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
            realizeRefund(event);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private  void validateExistsTransactionIdAndOrderID(Event event) {

        if (isEmpty(event.getPayload().getId()) || isEmpty(event.getTransactionId())) {
            throw new ValidationException("OrderID and TransactionID must be informed!");
        }
    }

    private void validateCustomerInformed(Event event) {

        boolean  notHavePayload = isEmpty(event.getPayload());
        boolean  notHaveCustomer = isEmpty(event.getPayload().getCustomer());
        boolean  notHaveName = isEmpty(event.getPayload().getCustomer().getName());
        boolean  notHaveCpf = isEmpty(event.getPayload().getCustomer().getCpf());

        if (notHavePayload || notHaveCustomer || notHaveName || notHaveCpf) {
            throw new ValidationException("Customer must be informed.");
        }

    }

    private void validateExistingCustomer(Event event) {
        log.info("LOG :: Validating existing customer: {}", event.getPayload().getCustomer().getCpf());
        customerRepository.findByCpf(event.getPayload().getCustomer().getCpf()).orElseThrow(
                () -> new ValidationException("Customer not found.")
        );
    }

    private void validateCpf(Event event) {

        if(!CpfValidator.isValid(event.getPayload().getCustomer().getCpf())) {
            throw new ValidationException("Invalid CPF.");
        }
    }

    private void handleCurrentValidation(Event event) {
        validateCustomerInformed(event);
        validateExistsTransactionIdAndOrderID(event);
        validateExistingCustomer(event);
        validateCpf(event);
        handleSuccess(event);
    }

    private void handleSuccess(Event event) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        historyService.add(event, "Customer Validated with success!");
    }


    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        historyService.add(event, "Fail customer not validated: ".concat(message));
    }

    public void realizeRefund(Event event) {
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);
        historyService.add(event, "Customer not validated. Refund realized!");
        producer.sendEvent(jsonUtil.toJson(event));
    }
}
