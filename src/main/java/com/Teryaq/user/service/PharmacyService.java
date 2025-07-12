package com.Teryaq.user.service;

import com.Teryaq.user.Enum.PharmacyType;
import com.Teryaq.user.Enum.UserStatus;
import com.Teryaq.user.config.RoleConstants;
import com.Teryaq.user.dto.PharmacyCreateRequestDTO;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.Role;
import com.Teryaq.user.entity.User;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    
    @Autowired
    private EmployeeService employeeService;

    Logger logger = Logger.getLogger(PharmacyService.class.getName());

    @Transactional
    public PharmacyResponseDTO createPharmacy(PharmacyCreateRequestDTO dto) {
        logger.info("Starting pharmacy creation for: " + dto.getPharmacyName());
        
        if (pharmacyRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            logger.warning("Pharmacy with license number " + dto.getLicenseNumber() + " already exists");
            throw new IllegalArgumentException("Pharmacy with this license number already exists");
        }
        
        // Create and save pharmacy
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(dto.getPharmacyName());
        pharmacy.setLicenseNumber(dto.getLicenseNumber());
        pharmacy.setPhoneNumber(dto.getPhoneNumber());
        pharmacy.setType(PharmacyType.MAIN);
        pharmacy = pharmacyRepository.save(pharmacy);
        logger.info("Pharmacy saved with ID: " + pharmacy.getId());

        // Create manager as Employee
        Role managerRole = roleRepository.findByName(RoleConstants.PHARMACY_MANAGER).orElseThrow();
        Employee manager = new Employee();

        // Remove spaces from licenseNumber and pharmacyName for email
        String cleanLicenseNumber = dto.getLicenseNumber().replaceAll("\\s+", "");
        String cleanPharmacyName = dto.getPharmacyName().replaceAll("\\s+", "");
        String managerEmail = "manager." + cleanLicenseNumber + "@" + cleanPharmacyName + ".com";
        logger.info("Generated manager email: " + managerEmail);

        manager.setEmail(managerEmail);
        manager.setPassword(passwordEncoder.encode(dto.getManagerPassword()));
        manager.setRole(managerRole);
        manager.setFirstName("Pharmacy Manager");
        manager.setLastName("");
        manager.setPharmacy(pharmacy);
        manager.setStatus(UserStatus.ACTIVE);
        employeeRepository.save(manager);
        logger.info("Manager created with ID: " + manager.getId());
        
        logger.info("Pharmacy creation completed successfully");
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


} 