package br.com.fean.customer_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private UUID code;
    private String  internalCode;
    private double unitValue;
}
