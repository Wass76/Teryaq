package com.Teryaq.user.service;

import com.Teryaq.user.dto.EmployeeCreateRequestDTO;
import com.Teryaq.user.dto.EmployeeResponseDTO;
import com.Teryaq.user.dto.EmployeeWorkingHoursDTO;
import com.Teryaq.user.dto.CreateWorkingHoursRequestDTO;
import com.Teryaq.user.dto.WorkShiftDTO;
import com.Teryaq.user.dto.EmployeeUpdateRequestDTO;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.EmployeeWorkingHours;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.Role;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.mapper.WorkShiftMapper;
import com.Teryaq.user.mapper.EmployeeMapper;
import com.Teryaq.user.repository.EmployeeRepository;
import com.Teryaq.user.repository.EmployeeWorkingHoursRepository;
import com.Teryaq.user.repository.RoleRepository;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import com.Teryaq.utils.exception.UnAuthorizedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.time.DayOfWeek;
import java.util.logging.Logger;

@Service
@Transactional
public class EmployeeService extends BaseSecurityService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkingHoursRepository employeeWorkingHoursRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    
    public EmployeeService(EmployeeRepository employeeRepository,
                         EmployeeWorkingHoursRepository employeeWorkingHoursRepository,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder,
                         UserService userService,
                         UserRepository userRepository) {
        super(userRepository);
        this.employeeRepository = employeeRepository;
        this.employeeWorkingHoursRepository = employeeWorkingHoursRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }
    
    Logger logger = Logger.getLogger(EmployeeService.class.getName());
    
    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeCreateRequestDTO dto) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create employees");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Pharmacy pharmacy = manager.getPharmacy();
        logger.info("Starting to add new employee: " + dto.getFirstName() + " " + dto.getLastName());
        logger.info("Manager pharmacy: " + pharmacy.getName() + " (ID: " + pharmacy.getId() + ")");
        
        // Generate and validate email
        String email = generateEmployeeEmail(dto, pharmacy);
        validateEmployeeEmail(email);
        
        // Create and save employee
        Employee employee = createEmployeeFromDTO(dto, email, pharmacy);
        employee = saveEmployee(employee);
        
        // Handle working hours (support both legacy and new format)
        saveEmployeeWorkingHoursRequests(employee, dto.getWorkingHoursRequests());
        
        logger.info("Employee creation completed successfully");
        return EmployeeMapper.toResponseDTO(employee);
    }
    
    public List<EmployeeResponseDTO> getAllEmployeesInPharmacy() {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access employee data");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = manager.getPharmacy().getId();
        logger.info("Getting all employees for pharmacy ID: " + pharmacyId);
        return employeeRepository.findByPharmacy_Id(pharmacyId)
                .stream()
                .map(EmployeeMapper::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public EmployeeResponseDTO updateEmployeeInPharmacy(Long employeeId, EmployeeUpdateRequestDTO dto) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can update employees");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long managerPharmacyId = manager.getPharmacy().getId();
        logger.info("Starting to update employee with ID: " + employeeId);
        
        // Validate and get employee
        Employee employee = validateAndGetEmployee(employeeId, managerPharmacyId);
        
        // Update employee fields using mapper (password is not updated for security)
        updateEmployeeFields(employee, dto);
        
        // Handle working hours update (support both legacy and new format)
        updateEmployeeWorkingHoursRequests(employee, dto.getWorkingHoursRequests());
        
        // Save and return
        employee = saveEmployee(employee);
        logger.info("Employee update completed successfully");
        return EmployeeMapper.toResponseDTO(employee);
    }
    @Transactional
    public void deleteEmployeeInPharmacy(Long employeeId) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can delete employees");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long managerPharmacyId = manager.getPharmacy().getId();
        logger.info("Starting to delete employee with ID: " + employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getPharmacy().getId().equals(managerPharmacyId)) {
            logger.warning("Manager tried to delete employee " + employeeId + " from different pharmacy");
            throw new AccessDeniedException("You can only delete employees in your own pharmacy");
        }
        
        // Delete working hours first
        employeeWorkingHoursRepository.deleteByEmployee_Id(employeeId);
        logger.info("Deleted working hours for employee");
        
        // Delete employee
        employeeRepository.delete(employee);
        logger.info("Employee deleted successfully");
    }
    
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
    
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
    
    public EmployeeResponseDTO createWorkingHoursForEmployee(Long employeeId, CreateWorkingHoursRequestDTO request) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create working hours");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long managerPharmacyId = manager.getPharmacy().getId();
        logger.info("Creating working hours for employee ID: " + employeeId);
        
        // Validate and get employee
        Employee employee = validateAndGetEmployee(employeeId, managerPharmacyId);
        
        // Create working hours for each day
        createWorkingHoursForMultipleDays(employee, request.getDaysOfWeek(), request.getShifts());
        
        logger.info("Working hours created successfully for employee");
        return EmployeeMapper.toResponseDTO(employee);
    }
    
    // Helper methods for employee creation
    private String generateEmployeeEmail(EmployeeCreateRequestDTO dto, Pharmacy pharmacy) {
        String cleanFirstName = dto.getFirstName().replaceAll("\\s+", "").toLowerCase();
        String cleanLastName = dto.getLastName().replaceAll("\\s+", "").toLowerCase();
        String cleanPharmacyName = pharmacy.getName().replaceAll("\\s+", "").toLowerCase();
        String email = cleanFirstName + "." + cleanLastName + "@" + cleanPharmacyName + ".com";
        logger.info("Generated email for employee: " + email);
        return email;
    }
    
    private void validateEmployeeEmail(String email) {
        if(employeeRepository.findByEmail(email).isPresent()) {
            throw new ResourceNotFoundException("Employee with email: " + email + " already exists");
        }
    }
    
    private Employee createEmployeeFromDTO(EmployeeCreateRequestDTO dto, String email, Pharmacy pharmacy) {
        Employee employee = EmployeeMapper.toEntity(dto);
        employee.setEmail(email);
        employee.setPharmacy(pharmacy);
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(
                () -> new ResourceNotFoundException("Invalid role id: " + dto.getRoleId())
        );
        employee.setRole(role);
        
        return employee;
    }
    
    private Employee saveEmployee(Employee employee) {
        logger.info("Saving employee to database...");
        employee = employeeRepository.save(employee);
        logger.info("Employee saved with ID: " + employee.getId());
        return employee;
    }
    
    private void saveEmployeeWorkingHours(Employee employee, List<EmployeeWorkingHoursDTO> workingHoursDTOs) {
        if (workingHoursDTOs != null && !workingHoursDTOs.isEmpty()) {
            logger.info("Processing working hours for employee (legacy format)...");
            List<EmployeeWorkingHours> workingHoursList = EmployeeMapper.createWorkingHoursFromDTO(employee, workingHoursDTOs);
            
            if (workingHoursList != null) {
                logger.info("Saving " + workingHoursList.size() + " working hours records...");
                employeeWorkingHoursRepository.saveAll(workingHoursList);
                logger.info("Working hours saved successfully");
            }
        }
    }
    
    private void saveEmployeeWorkingHoursRequests(Employee employee, List<CreateWorkingHoursRequestDTO> workingHoursRequests) {
        if (workingHoursRequests != null && !workingHoursRequests.isEmpty()) {
            logger.info("Processing working hours for employee (new format)...");
            
            for (CreateWorkingHoursRequestDTO request : workingHoursRequests) {
                createWorkingHoursForMultipleDays(employee, request.getDaysOfWeek(), request.getShifts());
            }
            
            logger.info("Working hours requests processed successfully");
        }
    }
    
    // Helper methods for employee update
    private Employee validateAndGetEmployee(Long employeeId, Long managerPharmacyId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getPharmacy().getId().equals(managerPharmacyId)) {
            logger.warning("Manager tried to update employee " + employeeId + " from different pharmacy");
            throw new AccessDeniedException("You can only update employees in your own pharmacy");
        }
        
        return employee;
    }
    
    private void updateEmployeeFields(Employee employee, EmployeeUpdateRequestDTO dto) {
        logger.info("Updating employee fields...");
        
        // Use the mapper to update the entity
        EmployeeMapper.updateEntity(employee, dto);
        
        // Handle role update separately
        if(dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(
                    () -> new ResourceNotFoundException("Invalid role id: " + dto.getRoleId())
            );
            employee.setRole(role);
        }
    }
    
    private void updateEmployeeWorkingHours(Employee employee, List<EmployeeWorkingHoursDTO> workingHoursDTOs) {
        if (workingHoursDTOs != null && !workingHoursDTOs.isEmpty()) {
            logger.info("Updating working hours for employee (legacy format)...");
            
            // Delete existing working hours
            employeeWorkingHoursRepository.deleteByEmployee_Id(employee.getId());
            logger.info("Deleted existing working hours");
            
            // Create new working hours
            List<EmployeeWorkingHours> workingHoursList = EmployeeMapper.createWorkingHoursFromDTO(employee, workingHoursDTOs);
            if (workingHoursList != null) {
                employeeWorkingHoursRepository.saveAll(workingHoursList);
                logger.info("Saved " + workingHoursList.size() + " new working hours records");
            }
        }
    }
    
    private void updateEmployeeWorkingHoursRequests(Employee employee, List<CreateWorkingHoursRequestDTO> workingHoursRequests) {
        if (workingHoursRequests != null && !workingHoursRequests.isEmpty()) {
            logger.info("Updating working hours for employee (new format)...");
            
            // Delete existing working hours for the days being updated
            for (CreateWorkingHoursRequestDTO request : workingHoursRequests) {
                if (request.getDaysOfWeek() != null) {
                    for (DayOfWeek dayOfWeek : request.getDaysOfWeek()) {
                        employeeWorkingHoursRepository.deleteByEmployee_IdAndDayOfWeek(employee.getId(), dayOfWeek);
                    }
                }
            }
            
            // Create new working hours
            for (CreateWorkingHoursRequestDTO request : workingHoursRequests) {
                createWorkingHoursForMultipleDays(employee, request.getDaysOfWeek(), request.getShifts());
            }
            
            logger.info("Working hours requests updated successfully");
        }
    }
    
    private void createWorkingHoursForMultipleDays(Employee employee, List<DayOfWeek> daysOfWeek, List<WorkShiftDTO> shifts) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            logger.warning("No days of week provided for working hours");
            return;
        }
        
        if (shifts == null || shifts.isEmpty()) {
            logger.warning("No shifts provided for working hours");
            return;
        }
        
        logger.info("Creating working hours for " + daysOfWeek.size() + " days");
        
        List<EmployeeWorkingHours> workingHoursList = new ArrayList<>();
        
        for (DayOfWeek dayOfWeek : daysOfWeek) {
            // Check if working hours already exist for this day
            Optional<EmployeeWorkingHours> existingWorkingHours = employeeWorkingHoursRepository
                    .findByEmployee_IdAndDayOfWeek(employee.getId(), dayOfWeek);
            
            if (existingWorkingHours.isPresent()) {
                logger.info("Working hours already exist for " + dayOfWeek + ", updating...");
                EmployeeWorkingHours existing = existingWorkingHours.get();
                existing.setShifts(WorkShiftMapper.toEntityList(shifts));
                workingHoursList.add(existing);
            } else {
                logger.info("Creating new working hours for " + dayOfWeek);
                EmployeeWorkingHours workingHours = new EmployeeWorkingHours();
                workingHours.setEmployee(employee);
                workingHours.setDayOfWeek(dayOfWeek);
                workingHours.setShifts(WorkShiftMapper.toEntityList(shifts));
                workingHoursList.add(workingHours);
            }
        }
        
        if (!workingHoursList.isEmpty()) {
            employeeWorkingHoursRepository.saveAll(workingHoursList);
            logger.info("Saved " + workingHoursList.size() + " working hours records");
        }
    }
} 