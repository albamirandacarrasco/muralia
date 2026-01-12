package com.muralia.service.mapper;

import com.muralia.api.model.Customer;
import com.muralia.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toDto(CustomerEntity entity) {
        Customer dto = new Customer();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setUsername(entity.getUsername());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
