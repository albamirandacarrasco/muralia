package com.muralia.service;

import com.muralia.entity.CustomerEntity;

public interface JwtService {

    /**
     * Generate a JWT token for the given customer
     * @param customer the customer entity
     * @return the generated JWT token
     */
    String generateToken(CustomerEntity customer);

    /**
     * Extract the username (email) from the JWT token
     * @param token the JWT token
     * @return the username (email)
     */
    String extractUsername(String token);

    /**
     * Validate the JWT token
     * @param token the JWT token
     * @param customer the customer entity to validate against
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token, CustomerEntity customer);

    /**
     * Check if the token is expired
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    boolean isTokenExpired(String token);
}
