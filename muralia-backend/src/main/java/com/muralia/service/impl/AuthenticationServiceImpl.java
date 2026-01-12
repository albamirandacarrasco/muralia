package com.muralia.service.impl;

import com.muralia.api.model.AuthResponse;
import com.muralia.api.model.Customer;
import com.muralia.api.model.LoginRequest;
import com.muralia.api.model.RegisterRequest;
import com.muralia.entity.CustomerEntity;
import com.muralia.repository.CustomerRepository;
import com.muralia.service.AuthenticationService;
import com.muralia.service.JwtService;
import com.muralia.service.mapper.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(CustomerRepository customerRepository,
                                     CustomerMapper customerMapper,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Attempting to register new customer with email: {}", registerRequest.getEmail());

        // Check if customer already exists
        if (customerRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", registerRequest.getEmail());
            throw new RuntimeException("Email already exists"); // TODO: Create proper exception
        }
        if (customerRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", registerRequest.getUsername());
            throw new RuntimeException("Username already exists"); // TODO: Create proper exception
        }

        // Create new customer
        CustomerEntity customer = CustomerEntity.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer registered successfully: {} (ID: {})", customer.getEmail(), customer.getId());

        // Generate JWT token
        String token = jwtService.generateToken(customer);

        // Build response
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(3600); // 1 hour
        response.setCustomer(customerMapper.toDto(customer));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // Find customer by email
        CustomerEntity customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: Customer not found - {}", loginRequest.getEmail());
                    return new RuntimeException("Invalid credentials"); // TODO: Create proper exception
                });

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            log.warn("Login failed: Invalid password for - {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid credentials"); // TODO: Create proper exception
        }

        // Generate JWT token
        String token = jwtService.generateToken(customer);
        log.info("Login successful for: {} (ID: {})", customer.getEmail(), customer.getId());

        // Build response
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(3600); // 1 hour
        response.setCustomer(customerMapper.toDto(customer));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCurrentCustomer() {
        // TODO: Get customer from SecurityContext/JWT
        // For now, throw exception
        throw new RuntimeException("Not implemented - need to extract customer from JWT");
    }

    @Override
    public void logout() {
        // TODO: Implement logout logic (e.g., token invalidation if using a blacklist)
        // For stateless JWT, this might be a no-op on the backend
    }
}
