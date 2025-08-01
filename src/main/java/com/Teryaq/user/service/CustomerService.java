package com.Teryaq.user.service;

import com.Teryaq.user.dto.CustomerDTORequest;
import com.Teryaq.user.dto.CustomerDTOResponse;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.mapper.CustomerMapper;
import com.Teryaq.user.repository.CustomerRepo;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepo customerRepo    , CustomerMapper customerMapper) {
        this.customerRepo = customerRepo;
        this.customerMapper = customerMapper;
    }

    public List<CustomerDTOResponse> getAllCustomers() {
        return customerRepo.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerDTOResponse getCustomerById(Long id) {
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse createCustomer(CustomerDTORequest dto) {
        Customer customer = customerMapper.toEntity(dto);
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse updateCustomer(Long id, CustomerDTORequest dto) {
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " not found"));

        customerMapper.updateEntityFromDto(customer, dto);
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepo.existsById(id)) {
            throw new EntityNotFoundException("Customer with ID " + id + " not found");
        }
        customerRepo.deleteById(id);
    }
} 