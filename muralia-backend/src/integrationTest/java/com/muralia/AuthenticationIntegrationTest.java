package com.muralia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muralia.api.model.LoginRequest;
import com.muralia.api.model.RegisterRequest;
import com.muralia.entity.CustomerEntity;
import com.muralia.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    public void testRegisterNewCustomer_Success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@test.com");
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value(not(equalTo("TODO_GENERATE_JWT_TOKEN"))))
                .andExpect(jsonPath("$.token").value(not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.customer.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.customer.username").value("newuser"))
                .andExpect(jsonPath("$.customer.firstName").value("New"))
                .andExpect(jsonPath("$.customer.lastName").value("User"));
    }

    @Test
    public void testRegisterExistingEmail_Failure() throws Exception {
        // Create existing customer
        CustomerEntity existingCustomer = CustomerEntity.builder()
                .email("existing@test.com")
                .username("existinguser")
                .password(passwordEncoder.encode("Password123"))
                .firstName("Existing")
                .lastName("User")
                .build();
        customerRepository.save(existingCustomer);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@test.com");
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testLogin_Success() throws Exception {
        // Create test customer
        CustomerEntity customer = CustomerEntity.builder()
                .email("test@test.com")
                .username("testuser")
                .password(passwordEncoder.encode("Password123"))
                .firstName("Test")
                .lastName("User")
                .build();
        customerRepository.save(customer);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value(not(equalTo("TODO_GENERATE_JWT_TOKEN"))))
                .andExpect(jsonPath("$.token").value(not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.customer.email").value("test@test.com"))
                .andExpect(jsonPath("$.customer.username").value("testuser"));
    }

    @Test
    public void testLogin_InvalidCredentials() throws Exception {
        // Create test customer
        CustomerEntity customer = CustomerEntity.builder()
                .email("test@test.com")
                .username("testuser")
                .password(passwordEncoder.encode("Password123"))
                .firstName("Test")
                .lastName("User")
                .build();
        customerRepository.save(customer);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testLogin_NonExistentUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@test.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testGeneratedJwtIsValid() throws Exception {
        // Create test customer
        CustomerEntity customer = CustomerEntity.builder()
                .email("jwt@test.com")
                .username("jwtuser")
                .password(passwordEncoder.encode("Password123"))
                .firstName("JWT")
                .lastName("User")
                .build();
        customerRepository.save(customer);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jwt@test.com");
        loginRequest.setPassword("Password123");

        // Perform login and capture the token
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                // JWT should start with "eyJ" (base64 encoded header)
                .andExpect(jsonPath("$.token").value(startsWith("eyJ")))
                // JWT should have 3 parts separated by dots
                .andExpect(jsonPath("$.token").value(matchesRegex("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")));
    }
}
