package br.com.fean.customer_service.domain.entities;

import br.com.fean.customer_service.domain.enums.EOrderStatus;
import br.com.fean.customer_service.domain.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document( collection = "order")
public class Order {

    @Id
    private String id;
    private Customer customer;
    private List<OrderProducts> products;
    private EOrderStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private double totalAmount;
    private int totalItems;
}
