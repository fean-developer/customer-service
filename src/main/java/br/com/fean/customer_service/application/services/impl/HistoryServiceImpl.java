package br.com.fean.customer_service.application.services.impl;

import br.com.fean.customer_service.application.interfaces.History;
import br.com.fean.customer_service.domain.entities.Event;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HistoryServiceImpl implements History {

    @Override
    public void add(Event event, String message) {
        var history = br.com.fean.customer_service.domain.entities.History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }
}
