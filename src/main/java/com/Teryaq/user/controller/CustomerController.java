package com.Teryaq.user.controller;

import com.Teryaq.user.dto.CustomerDTORequest;
import com.Teryaq.user.dto.CustomerDTOResponse;
import com.Teryaq.user.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("api/v1/customers")
@Tag(name = "Customer Management", description = "APIs for managing customers and their debts")
public class CustomerController {

    private final CustomerService customerService;
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
        logger.info("CustomerController initialized successfully");
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve all customers with their debt information")
    public ResponseEntity<List<CustomerDTOResponse>> getAllCustomers() {
        logger.info("Fetching all customers");
        List<CustomerDTOResponse> customers = customerService.getAllCustomers();
        logger.info("Retrieved {} customers", customers.size());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by ID with debt details")
    public ResponseEntity<CustomerDTOResponse> getCustomerById(
            @Parameter(description = "Customer ID", example = "1") 
            @PathVariable Long id) {
        logger.info("Fetching customer with ID: {}", id);
        CustomerDTOResponse customer = customerService.getCustomerById(id);
        logger.info("Retrieved customer: {}", customer.getName());
        return ResponseEntity.ok(customer);
    }

    @GetMapping("search")
    @Operation(summary = "Search customers by name", description = "Search customers by name (partial match)")
    public ResponseEntity<List<CustomerDTOResponse>> searchCustomersByName(
            @Parameter(description = "Customer name to search for", example = "cash") 
            @RequestParam(required = false) String name) {
        logger.info("Searching customers with name: {}", name);
        List<CustomerDTOResponse> customers = customerService.searchCustomersByName(name);
        logger.info("Found {} customers matching search criteria", customers.size());
        return ResponseEntity.ok(customers);
    }

    // @GetMapping("name/{name}")
    // @Operation(summary = "Get customer by exact name", description = "Retrieve a customer by exact name match")
    // public ResponseEntity<CustomerDTOResponse> getCustomerByName(
    //         @Parameter(description = "Exact customer name", example = "cash customer") 
    //         @PathVariable String name) {
    //     logger.info("Fetching customer with exact name: {}", name);
    //     CustomerDTOResponse customer = customerService.getCustomerByName(name);
    //     logger.info("Retrieved customer: {}", customer.getName());
    //     return ResponseEntity.ok(customer);
    // }

    @GetMapping("with-debts")
    @Operation(summary = "Get customers with debts", description = "Retrieve customers who have remaining debts")
    public ResponseEntity<List<CustomerDTOResponse>> getCustomersWithDebts() {
        logger.info("Fetching customers with debts");
        List<CustomerDTOResponse> customers = customerService.getCustomersWithDebts();
        logger.info("Found {} customers with debts", customers.size());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("with-active-debts")
    @Operation(summary = "Get customers with active debts", description = "Retrieve customers who have active (unpaid) debts")
    public ResponseEntity<List<CustomerDTOResponse>> getCustomersWithActiveDebts() {
        logger.info("Fetching customers with active debts");
        List<CustomerDTOResponse> customers = customerService.getCustomersWithActiveDebts();
        logger.info("Found {} customers with active debts", customers.size());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("debt-range")
    @Operation(summary = "Get customers by debt range", description = "Retrieve customers within a specific debt range")
    public ResponseEntity<List<CustomerDTOResponse>> getCustomersByDebtRange(
            @Parameter(description = "Minimum debt amount", example = "100.0") 
            @RequestParam Float minDebt,
            @Parameter(description = "Maximum debt amount", example = "1000.0") 
            @RequestParam Float maxDebt) {
        logger.info("Fetching customers with debt range: {} - {}", minDebt, maxDebt);
        List<CustomerDTOResponse> customers = customerService.getCustomersByDebtRange(minDebt, maxDebt);
        logger.info("Found {} customers in debt range", customers.size());
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    @Operation(
        summary = "Create customer", 
        description = "Create a new customer. If name is not provided, 'cash customer' will be used as default."
    )
    public ResponseEntity<CustomerDTOResponse> createCustomer(
            @Parameter(description = "Customer data", required = true)
            @RequestBody CustomerDTORequest dto) {
        logger.info("Creating new customer with name: {}", dto.getName());
        CustomerDTOResponse customer = customerService.createCustomer(dto);
        logger.info("Successfully created customer with ID: {}", customer.getId());
        return ResponseEntity.ok(customer);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update customer", description = "Update an existing customer's information")
    public ResponseEntity<CustomerDTOResponse> updateCustomer(
            @Parameter(description = "Customer ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Updated customer data", required = true)
            @RequestBody CustomerDTORequest dto) {
        logger.info("Updating customer with ID: {}", id);
        CustomerDTOResponse customer = customerService.updateCustomer(id, dto);
        logger.info("Successfully updated customer: {}", customer.getName());
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete customer", description = "Delete a customer. Cannot delete if customer has active debts.")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", example = "1") 
            @PathVariable Long id) {
        logger.info("Deleting customer with ID: {}", id);
        customerService.deleteCustomer(id);
        logger.info("Successfully deleted customer with ID: {}", id);
        return ResponseEntity.ok().build();
    }
} 