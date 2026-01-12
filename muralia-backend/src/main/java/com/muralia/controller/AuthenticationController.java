package com.muralia.controller;

import com.muralia.api.AuthenticationApi;
import com.muralia.api.model.AuthResponse;
import com.muralia.api.model.Customer;
import com.muralia.api.model.LoginRequest;
import com.muralia.api.model.RegisterRequest;
import com.muralia.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController implements AuthenticationApi {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public ResponseEntity<Customer> _getCurrentCustomer() {
        Customer customer = authenticationService.getCurrentCustomer();
        return ResponseEntity.ok(customer);
    }

    @Override
    public ResponseEntity<AuthResponse> _loginCustomer(LoginRequest loginRequest) {
        AuthResponse authResponse = authenticationService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @Override
    public ResponseEntity<Void> _logoutCustomer() {
        authenticationService.logout();
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AuthResponse> _registerCustomer(RegisterRequest registerRequest) {
        AuthResponse authResponse = authenticationService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }
}
