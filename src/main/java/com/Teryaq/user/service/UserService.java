package com.Teryaq.user.service;

import com.Teryaq.user.dto.UserCreateRequestDTO;
import com.Teryaq.user.dto.UserRequestDTO;
import com.Teryaq.user.dto.UserResponseDTO;
import com.Teryaq.user.entity.Permission;
import com.Teryaq.user.entity.Role;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.mapper.UserMapper;
import com.Teryaq.user.repository.PermissionRepository;
import com.Teryaq.user.repository.RoleRepository;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.config.JwtService;
import com.Teryaq.config.RateLimiterConfig;
import com.Teryaq.utils.exception.RequestNotValidException;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import com.Teryaq.utils.exception.TooManyRequestException;
import com.Teryaq.user.dto.AuthenticationRequest;
import com.Teryaq.user.dto.UserAuthenticationResponse;
import com.Teryaq.utils.Validator.ObjectsValidator;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RateLimiterConfig rateLimiterConfig;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final UserMapper userMapper;
    @Autowired
    private ObjectsValidator<AuthenticationRequest> authenticationRequestValidator;

    /**
     * Create a new user with territory assignments using BASE entity IDs
     */



    public UserResponseDTO createUser(UserCreateRequestDTO request) {
        User creator = getCurrentUser();
        Role targetRole = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId()));
        
        validateUserCreation(targetRole, creator.getRole());
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(targetRole);
        user.setPosition(request.getPosition());
        
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            user.setAdditionalPermissions(permissions);
        }
        
        return userMapper.toResponse(userRepository.save(user));
    }
    
    private void validateUserCreation(Role targetRole, Role creatorRole) {
        // Platform Admin can create any role except another Platform Admin
        if (creatorRole.getName().equals("PLATFORM_ADMIN")) {
            if (targetRole.getName().equals("PLATFORM_ADMIN")) {
                throw new AccessDeniedException("Cannot create another Platform Admin");
            }
            return;
        }
        
        // Sales Manager can only create Sales Agent
        if (creatorRole.getName().equals("SALES_MANAGER")) {
            if (!targetRole.getName().equals("SALES_AGENT")) {
                throw new AccessDeniedException("Sales manager can only create SALES_AGENT users");
            }
            return;
        }
        
        throw new AccessDeniedException("Only PLATFORM_ADMIN and SALES_MANAGER can create users");
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        Role targetRole = roleRepository.findById(userDetails.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + userDetails.getRoleId()));
        
        validateUserUpdate(targetRole, user.getRole());
        
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setRole(targetRole);
        user.setPosition(userDetails.getPosition());
        
        if (userDetails.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(userDetails.getPermissionIds()));
            user.setAdditionalPermissions(permissions);
        }
        
        return userMapper.toResponse(userRepository.save(user));
    }

    private void validateUserUpdate(Role targetRole, Role currentRole) {
        // Platform Admin can update any role except another Platform Admin
        if (currentRole.getName().equals("PLATFORM_ADMIN")) {
            if (targetRole.getName().equals("PLATFORM_ADMIN")) {
                throw new AccessDeniedException("Cannot update to Platform Admin role");
            }
            return;
        }
        
        // Sales Manager can only update to Sales Agent
        if (currentRole.getName().equals("SALES_MANAGER")) {
            if (!targetRole.getName().equals("SALES_AGENT")) {
                throw new AccessDeniedException("Sales manager can only update to SALES_AGENT role");
            }
            return;
        }
        
        throw new AccessDeniedException("Only PLATFORM_ADMIN and SALES_MANAGER can update users");
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public UserAuthenticationResponse login(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        String userIp = httpServletRequest.getRemoteAddr();
        if (rateLimiterConfig.getBlockedIPs().contains(userIp)) {
            throw new TooManyRequestException("Too many login attempts. Please try again later.");
        }

        String rateLimiterKey = "userLoginRateLimiter-" + userIp;
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
                    () -> new RequestNotValidException("User email not found")
            );

            var jwtToken = jwtService.generateToken(user);
            
            UserAuthenticationResponse response = userEntityToDto(user);
            response.setToken(jwtToken);

            return response;
        } else {
            rateLimiterConfig.blockIP(userIp);
            throw new TooManyRequestException("Too many login attempts, Please try again later.");
        }
    }

    private UserAuthenticationResponse userEntityToDto(User user) {
        UserAuthenticationResponse userAuthenticationResponse = new UserAuthenticationResponse();
        userAuthenticationResponse.setEmail(user.getEmail());
        userAuthenticationResponse.setFirstName(user.getFirstName());
        userAuthenticationResponse.setLastName(user.getLastName());
        userAuthenticationResponse.setRole(user.getRole().getName());
        return userAuthenticationResponse;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    public UserResponseDTO getCurrentUserResponse(){
        return userMapper.toResponse(getCurrentUser());
    }

    public UserResponseDTO updateUserPermissions(Long userId, Set<Long> permissionIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        user.setAdditionalPermissions(permissions);
        return userMapper.toResponse(userRepository.save(user));
    }
} 