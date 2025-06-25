package com.Teryaq.user.service;

import com.Teryaq.user.Enum.PharmacyType;
import com.Teryaq.user.Enum.UserStatus;
import com.Teryaq.user.config.RoleConstants;
import com.Teryaq.user.dto.EmployeeCreateRequestDTO;
import com.Teryaq.user.dto.PharmacyCreateRequestDTO;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.Role;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.mapper.EmployeeMapper;
import com.Teryaq.user.mapper.PharmacyMapper;
import com.Teryaq.user.repository.EmployeeRepository;
import com.Teryaq.user.repository.PharmacyRepository;
import com.Teryaq.user.repository.RoleRepository;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Teryaq.user.dto.AuthenticationRequest;
import com.Teryaq.user.dto.UserAuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import com.Teryaq.utils.exception.TooManyRequestException;
import com.Teryaq.config.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.Teryaq.config.JwtService;
import java.util.HashSet;
import com.Teryaq.user.dto.PharmacyResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;
import com.Teryaq.user.dto.EmployeeResponseDTO;

@Service
public class PharmacyService {
    @Autowired
    private PharmacyRepository pharmacyRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RateLimiterConfig rateLimiterConfig;
    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Transactional
    public PharmacyResponseDTO createPharmacy(PharmacyCreateRequestDTO dto) {
        if (pharmacyRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new IllegalArgumentException("Pharmacy with this license number already exists");
        }
        // Create and save pharmacy
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(dto.getPharmacyName());
        pharmacy.setLicenseNumber(dto.getLicenseNumber());
        pharmacy.setPhoneNumber(dto.getPhoneNumber());
        pharmacy.setType(PharmacyType.MAIN);
        pharmacy = pharmacyRepository.save(pharmacy);

        // Create manager as Employee
        Role managerRole = roleRepository.findByName(RoleConstants.PHARMACY_MANAGER).orElseThrow();
        Employee manager = new Employee();

        // Remove spaces from licenseNumber and pharmacyName for email
        String cleanLicenseNumber = dto.getLicenseNumber().replaceAll("\\s+", "");
        String cleanPharmacyName = dto.getPharmacyName().replaceAll("\\s+", "");
        String managerEmail = "manager." + cleanLicenseNumber + "@" + cleanPharmacyName + ".com";

        manager.setEmail(managerEmail);
        manager.setPassword(passwordEncoder.encode(dto.getManagerPassword()));
        manager.setRole(managerRole);
        manager.setFirstName("Pharmacy Manager");
        manager.setLastName("");
        manager.setPharmacy(pharmacy);
        manager.setStatus(UserStatus.ACTIVE);
        employeeRepository.save(manager);
        return PharmacyMapper.toResponseDTO(pharmacy, manager);
    }

    @Transactional
    public PharmacyResponseDTO completeRegistration(String newPassword, String location, String managerFirstName, String managerLastName, String pharmacyPhone, String pharmacyEmail, String openingHours) {
        Employee manager = (Employee) userService.getCurrentUser();
        Pharmacy pharmacy = manager.getPharmacy();
        // Update only non-null fields for address, email, openingHours
        PharmacyMapper.updatePharmacyFromRequest(pharmacy, location, pharmacyEmail, openingHours);
        if (pharmacyPhone != null && !pharmacyPhone.isEmpty()) {
            pharmacy.setPhoneNumber(pharmacyPhone);
        }
        pharmacyRepository.save(pharmacy);
        // Optionally update manager info
        manager.setPassword(passwordEncoder.encode(newPassword));
        if(managerFirstName != null && !managerFirstName.isEmpty()) {
            manager.setFirstName(managerFirstName);
        }
        if(managerLastName != null && !managerLastName.isEmpty()) {
            manager.setLastName(managerLastName);
        }
        manager.setPharmacy(pharmacy);
        employeeRepository.save(manager);
        return PharmacyMapper.toResponseDTO(pharmacy, manager);
    }

    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeCreateRequestDTO dto) {
        Employee manager = (Employee) userService.getCurrentUser();
        Pharmacy pharmacy = manager.getPharmacy();
        // Generate email: firstName.lastName@pharmacyName.com
        String cleanFirstName = dto.getFirstName().replaceAll("\\s+", "").toLowerCase();
        String cleanLastName = dto.getLastName().replaceAll("\\s+", "").toLowerCase();
        String cleanPharmacyName = pharmacy.getName().replaceAll("\\s+", "").toLowerCase();
        String email = cleanFirstName + "." + cleanLastName + "@" + cleanPharmacyName + ".com";
        Employee employee = EmployeeMapper.toEntity(dto);
        employee.setEmail(email);
        employee.setPharmacy(pharmacy);
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(
                () -> new ResourceNotFoundException("Invalid role id: " + dto.getRoleId())
        );
        employee.setRole(role);
        employeeRepository.save(employee);
        return EmployeeMapper.toResponseDTO(employee);
    }

    public UserAuthenticationResponse adminLogin(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        String userIp = httpServletRequest.getRemoteAddr();
        if (rateLimiterConfig.getBlockedIPs().contains(userIp)) {
            throw new TooManyRequestException("Too many login attempts. Please try again later.");
        }
        String rateLimiterKey = "adminLoginRateLimiter-" + userIp;
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey);

        if (rateLimiter.acquirePermission()) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword(),
                            new HashSet<>()
                    ));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            var user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                    () -> new RuntimeException("Admin email not found")
            );
            if (!RoleConstants.PLATFORM_ADMIN.equals(user.getRole().getName())) {
                throw new AccessDeniedException("Not a system admin");
            }
            var jwtToken = jwtService.generateToken(user);
            UserAuthenticationResponse response = new UserAuthenticationResponse();
            response.setToken(jwtToken);
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setRole(user.getRole().getName());
            return response;
        } else {
            rateLimiterConfig.blockIP(userIp);
            throw new TooManyRequestException("Too many login attempts, Please try again later.");
        }
    }

    public UserAuthenticationResponse managerLogin(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        String userIp = httpServletRequest.getRemoteAddr();
        if (rateLimiterConfig.getBlockedIPs().contains(userIp)) {
            throw new TooManyRequestException("Too many login attempts. Please try again later.");
        }
        String rateLimiterKey = "managerLoginRateLimiter-" + userIp;
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey);
        if (rateLimiter.acquirePermission()) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword(),
                            new HashSet<>()
                    ));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            var employee = employeeRepository.findByEmail(request.getEmail()).orElseThrow(
                    () -> new RuntimeException("Manager email not found")
            );
            if (!"PHARMACY_MANAGER".equals(employee.getRole().getName())) {
                throw new AccessDeniedException("Not a pharmacy manager");
            }
            var jwtToken = jwtService.generateToken(employee);
            UserAuthenticationResponse response = new UserAuthenticationResponse();
            response.setToken(jwtToken);
            response.setEmail(employee.getEmail());
            response.setFirstName(employee.getFirstName());
            response.setLastName(employee.getLastName());
            response.setRole(employee.getRole().getName());
            return response;
        } else {
            rateLimiterConfig.blockIP(userIp);
            throw new TooManyRequestException("Too many login attempts, Please try again later.");
        }
    }

    public List<PharmacyResponseDTO> getAllPharmacies() {
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        return pharmacies.stream()
                .map(pharmacy -> {
                    // Find manager for this pharmacy
                    Employee manager = employeeRepository.findAll().stream()
                        .filter(e -> e.getPharmacy() != null && e.getPharmacy().getId().equals(pharmacy.getId()) && e.getRole() != null && "PHARMACY_MANAGER".equals(e.getRole().getName()))
                        .findFirst().orElse(null);
                    return PharmacyMapper.toResponseDTO(pharmacy, manager);
                })
                .collect(Collectors.toList());
    }

    public List<EmployeeResponseDTO> getAllEmployeesInPharmacy() {
        Employee manager = (Employee) userService.getCurrentUser();
        Long pharmacyId = manager.getPharmacy().getId();
        return employeeRepository.findByPharmacy_Id(pharmacyId)
                .stream()
                .map(EmployeeMapper::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public EmployeeResponseDTO updateEmployeeInPharmacy(Long employeeId, EmployeeCreateRequestDTO dto) {
        Employee manager = (Employee) userService.getCurrentUser();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (!employee.getPharmacy().getId().equals(manager.getPharmacy().getId())) {
            throw new AccessDeniedException("You can only update employees in your own pharmacy");
        }
        // Update only allowed fields
        if (dto.getFirstName() != null) employee.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) employee.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) employee.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        if (dto.getDateOfHire() != null) employee.setDateOfHire(dto.getDateOfHire());
        if (dto.getWorkStart() != null) employee.setWorkStart(dto.getWorkStart());
        if (dto.getWorkEnd() != null) employee.setWorkEnd(dto.getWorkEnd());
        employeeRepository.save(employee);
        return EmployeeMapper.toResponseDTO(employee);
    }

    public void deleteEmployeeInPharmacy(Long employeeId) {
        Employee manager = (Employee) userService.getCurrentUser();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (!employee.getPharmacy().getId().equals(manager.getPharmacy().getId())) {
            throw new AccessDeniedException("You can only delete employees in your own pharmacy");
        }
        employeeRepository.delete(employee);
    }
} 