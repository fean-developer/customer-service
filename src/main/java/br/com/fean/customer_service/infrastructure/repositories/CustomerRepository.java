package br.com.fean.customer_service.infrastructure.repositories;

import br.com.fean.customer_service.domain.entities.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {

        Optional<Customer> findByCpf(String cpf);
}
