package com.Teryaq.user.service;

import com.Teryaq.user.entity.User;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseSecurityService {

    protected final UserRepository userRepository;

    protected BaseSecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gets the currently authenticated user
     * @return The current user
     * @throws ResourceNotFoundException if the user is not found
     */
    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Checks if the current user is a platform admin
     * @return true if the user is a platform admin
     */
    protected boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("PLATFORM_ADMIN");
    }

    /**
     * Checks if the current user has a specific role
     * @param roleName The name of the role to check
     * @return true if the user has the specified role
     */
    protected boolean hasRole(String roleName) {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals(roleName);
    }
} 