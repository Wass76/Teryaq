package com.Teryaq.user.service;

import com.Teryaq.user.dto.CustomerDTORequest;
import com.Teryaq.user.dto.CustomerDTOResponse;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.mapper.CustomerMapper;
import com.Teryaq.user.repository.CustomerRepo;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.utils.exception.UnAuthorizedException;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityNotFoundException;
import com.Teryaq.utils.exception.ConflictException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService extends BaseSecurityService {

    private final CustomerRepo customerRepo;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepo customerRepo, CustomerMapper customerMapper, UserRepository userRepository) {
        super(userRepository);
        this.customerRepo = customerRepo;
        this.customerMapper = customerMapper;
    }

    public List<CustomerDTOResponse> getAllCustomers() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        return customerRepo.findByPharmacyId(currentPharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerDTOResponse getCustomerById(Long id) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        Customer customer = customerRepo.findByIdAndPharmacyId(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id + " in this pharmacy"));
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse getCustomerByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ConflictException("Customer name cannot be empty");
        }
        
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        Customer customer = customerRepo.findByNameAndPharmacyId(name, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with name: " + name + " in this pharmacy"));
        return customerMapper.toResponse(customer);
    }

    public List<CustomerDTOResponse> searchCustomersByName(String name) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        if (!StringUtils.hasText(name)) {
            return customerRepo.findByPharmacyId(currentPharmacyId)
                    .stream()
                    .map(customerMapper::toResponse)
                    .collect(Collectors.toList());
        }
        
        return customerRepo.findByNameContainingIgnoreCaseAndPharmacyId(name, currentPharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithDebts() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        return customerRepo.findByPharmacyId(currentPharmacyId)
                .stream()
                .filter(customer -> customer.getDebts() != null && !customer.getDebts().isEmpty())
                .map(customerMapper::toResponse)
                .filter(response -> response.getRemainingDebt() > 0)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithActiveDebts() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        return customerRepo.findByPharmacyId(currentPharmacyId)
                .stream()
                .filter(customer -> customer.getDebts() != null && !customer.getDebts().isEmpty())
                .map(customerMapper::toResponse)
                .filter(response -> response.getActiveDebtsCount() > 0)
                .collect(Collectors.toList());
    }

    public CustomerDTOResponse createCustomer(CustomerDTORequest dto) {
        validateCustomerRequest(dto);
        
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        if (StringUtils.hasText(dto.getName()) && !"cash customer".equals(dto.getName())) {
            customerRepo.findByNameAndPharmacyId(dto.getName(), currentPharmacyId)
                    .ifPresent(existingCustomer -> { 
                        throw new ConflictException("Customer with name '" + dto.getName() + "' already exists in this pharmacy");
                    });
        }
        
        Customer customer = customerMapper.toEntity(dto);
        customer.setPharmacy(employee.getPharmacy());
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse updateCustomer(Long id, CustomerDTORequest dto) {
        validateCustomerRequest(dto);
        
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can update customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        Customer customer = customerRepo.findByIdAndPharmacyId(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " not found in this pharmacy"));

        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(customer.getName())) {
            customerRepo.findByNameAndPharmacyId(dto.getName(), currentPharmacyId)
                    .ifPresent(existingCustomer -> {
                        if (!existingCustomer.getId().equals(id)) {
                            throw new ConflictException("Customer with name '" + dto.getName() + "' already exists in this pharmacy");
                        }
                    });
        }

        customerMapper.updateEntityFromDto(customer, dto);
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public void deleteCustomer(Long id) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can delete customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        Customer customer = customerRepo.findByIdAndPharmacyId(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " not found in this pharmacy"));
        
        if (customer.getDebts() != null && !customer.getDebts().isEmpty()) {
            boolean hasActiveDebts = customer.getDebts().stream()
                    .anyMatch(debt -> "ACTIVE".equals(debt.getStatus()) && debt.getRemainingAmount() > 0);
            
            if (hasActiveDebts) {
                throw new ConflictException("Cannot delete customer with active debts. Please settle all debts first.");
            }
        }
        
        customerRepo.deleteById(id);
    }

    private void validateCustomerRequest(CustomerDTORequest dto) {
        if (dto == null) {
            throw new ConflictException("Customer request cannot be null");
        }
        
        if (StringUtils.hasText(dto.getPhoneNumber())) {
            if (!dto.getPhoneNumber().matches("^[0-9]{10}$")) {
                throw new ConflictException("Phone number must be 10 digits");
            }
        }
    }

    public List<CustomerDTOResponse> getCustomersByDebtRange(Float minDebt, Float maxDebt) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        return customerRepo.findByPharmacyId(currentPharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .filter(response -> {
                    Float remainingDebt = response.getRemainingDebt();
                    return remainingDebt >= minDebt && remainingDebt <= maxDebt;
                })
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersByPharmacyId(Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        return customerRepo.findByPharmacyId(pharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerDTOResponse getCustomerByIdAndPharmacyId(Long id, Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        Customer customer = customerRepo.findByIdAndPharmacyId(id, pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id + " in pharmacy: " + pharmacyId));
        return customerMapper.toResponse(customer);
    }

    public List<CustomerDTOResponse> searchCustomersByNameAndPharmacyId(String name, Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        if (!StringUtils.hasText(name)) {
            return customerRepo.findByPharmacyId(pharmacyId)
                    .stream()
                    .map(customerMapper::toResponse)
                    .collect(Collectors.toList());
        }
        
        return customerRepo.findByNameContainingIgnoreCaseAndPharmacyId(name, pharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithDebtsByPharmacyId(Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        return customerRepo.findByPharmacyId(pharmacyId)
                .stream()
                .filter(customer -> customer.getDebts() != null && !customer.getDebts().isEmpty())
                .map(customerMapper::toResponse)
                .filter(response -> response.getRemainingDebt() > 0)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithActiveDebtsByPharmacyId(Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        return customerRepo.findByPharmacyId(pharmacyId)
                .stream()
                .filter(customer -> customer.getDebts() != null && !customer.getDebts().isEmpty())
                .map(customerMapper::toResponse)
                .filter(response -> response.getActiveDebtsCount() > 0)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersByDebtRangeAndPharmacyId(Float minDebt, Float maxDebt, Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        return customerRepo.findByPharmacyId(pharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .filter(response -> {
                    Float remainingDebt = response.getRemainingDebt();
                    return remainingDebt >= minDebt && remainingDebt <= maxDebt;
                })
                .collect(Collectors.toList());
    }
} 