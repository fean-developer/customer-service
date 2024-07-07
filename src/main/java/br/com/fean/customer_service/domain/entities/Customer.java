package br.com.fean.customer_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document( collection = "customer")
public class Customer {

    @Id
    private String id;
    private String name;
    private String email;
    private String cpf;
    private String phone;
}
