package com.Teryaq.user.controller;

import com.Teryaq.user.dto.CustomerDTORequest;
import com.Teryaq.user.dto.CustomerDTOResponse;
import com.Teryaq.user.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("api/v1/customers")
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
        logger.info("CustomerController initialized successfully");
    }


    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve all customers")
    public ResponseEntity<List<CustomerDTOResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by ID")
    public ResponseEntity<CustomerDTOResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    @Operation(summary = "Create customer", description = "Create a new customer")
    public ResponseEntity<CustomerDTOResponse> createCustomer(@RequestBody CustomerDTORequest dto) {
        return ResponseEntity.ok(customerService.createCustomer(dto));
    }

    @PutMapping("{id}")
    public CustomerDTOResponse updateCustomer(@PathVariable Long id, 
                                              @RequestBody CustomerDTORequest dto) {
        return customerService.updateCustomer(id, dto);
    }

    @DeleteMapping("{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }
} 