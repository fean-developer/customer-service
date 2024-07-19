package br.com.fean.customer_service.application.interfaces;

import br.com.fean.customer_service.domain.entities.Event;

public interface History {
    void add(Event event, String message);
}
