package br.com.fean.customer_service.application.services;

import br.com.fean.customer_service.domain.entities.Event;

public interface CustomerValidateService {
    void validate(Event event);
}
