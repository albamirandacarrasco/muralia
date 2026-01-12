package com.muralia.service;

import com.muralia.api.model.AuthResponse;
import com.muralia.api.model.Customer;
import com.muralia.api.model.LoginRequest;
import com.muralia.api.model.RegisterRequest;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    Customer getCurrentCustomer();
    void logout();
}
