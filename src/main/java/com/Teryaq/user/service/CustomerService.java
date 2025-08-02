package com.Teryaq.user.service;

import com.Teryaq.user.dto.CustomerDTORequest;
import com.Teryaq.user.dto.CustomerDTOResponse;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.mapper.CustomerMapper;
import com.Teryaq.user.repository.CustomerRepo;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepo customerRepo, CustomerMapper customerMapper) {
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
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id));
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse getCustomerByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ValidationException("Customer name cannot be empty");
        }
        
        Customer customer = customerRepo.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with name: " + name));
        return customerMapper.toResponse(customer);
    }

    public List<CustomerDTOResponse> searchCustomersByName(String name) {
        if (!StringUtils.hasText(name)) {
            return getAllCustomers();
        }
        
        return customerRepo.findAll()
                .stream()
                .filter(customer -> customer.getName().toLowerCase().contains(name.toLowerCase()))
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithDebts() {
        return customerRepo.findAll()
                .stream()
                .filter(customer -> customer.getDebts() != null && !customer.getDebts().isEmpty())
                .map(customerMapper::toResponse)
                .filter(response -> response.getRemainingDebt() > 0)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithActiveDebts() {
        return customerRepo.findAll()
                .stream()
                .filter(customer -> customer.getDebts() != null && !customer.getDebts().isEmpty())
                .map(customerMapper::toResponse)
                .filter(response -> response.getActiveDebtsCount() > 0)
                .collect(Collectors.toList());
    }

    public CustomerDTOResponse createCustomer(CustomerDTORequest dto) {
        validateCustomerRequest(dto);
        
        // التحقق من عدم وجود عميل بنفس الاسم إذا كان الاسم محدداً
        if (StringUtils.hasText(dto.getName()) && !"cash customer".equals(dto.getName())) {
            customerRepo.findByName(dto.getName())
                    .ifPresent(existingCustomer -> {
                        throw new ValidationException("Customer with name '" + dto.getName() + "' already exists");
                    });
        }
        
        Customer customer = customerMapper.toEntity(dto);
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse updateCustomer(Long id, CustomerDTORequest dto) {
        validateCustomerRequest(dto);
        
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " not found"));

        // التحقق من عدم وجود عميل آخر بنفس الاسم إذا تم تغيير الاسم
        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(customer.getName())) {
            customerRepo.findByName(dto.getName())
                    .ifPresent(existingCustomer -> {
                        if (!existingCustomer.getId().equals(id)) {
                            throw new ValidationException("Customer with name '" + dto.getName() + "' already exists");
                        }
                    });
        }

        customerMapper.updateEntityFromDto(customer, dto);
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " not found"));
        
        // التحقق من عدم وجود ديون نشطة قبل الحذف
        if (customer.getDebts() != null && !customer.getDebts().isEmpty()) {
            boolean hasActiveDebts = customer.getDebts().stream()
                    .anyMatch(debt -> "ACTIVE".equals(debt.getStatus()) && debt.getRemainingAmount() > 0);
            
            if (hasActiveDebts) {
                throw new ValidationException("Cannot delete customer with active debts. Please settle all debts first.");
            }
        }
        
        customerRepo.deleteById(id);
    }

    private void validateCustomerRequest(CustomerDTORequest dto) {
        if (dto == null) {
            throw new ValidationException("Customer request cannot be null");
        }
        
        // التحقق من صحة رقم الهاتف إذا تم توفيره
        if (StringUtils.hasText(dto.getPhoneNumber())) {
            if (!dto.getPhoneNumber().matches("^[0-9]{10}$")) {
                throw new ValidationException("Phone number must be 10-11 digits");
            }
        }
    }

    public List<CustomerDTOResponse> getCustomersByDebtRange(Float minDebt, Float maxDebt) {
        return customerRepo.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .filter(response -> {
                    Float remainingDebt = response.getRemainingDebt();
                    return remainingDebt >= minDebt && remainingDebt <= maxDebt;
                })
                .collect(Collectors.toList());
    }
} 