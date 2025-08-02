package com.Teryaq.utils.service;

import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.User;
import com.Teryaq.utils.exception.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PharmacyContextService {

    /**
     * Get the pharmacy associated with the currently authenticated user
     * @return Pharmacy object
     * @throws UnAuthorizedException if user is not authenticated or has no pharmacy
     */
    public Pharmacy getCurrentUserPharmacy() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof Employee employee) {
            if (employee.getPharmacy() == null) {
                throw new UnAuthorizedException("User is not associated with any pharmacy");
            }
            return employee.getPharmacy();
        }
        
        throw new UnAuthorizedException("User is not an employee or not properly authenticated");
    }

    /**
     * Get the pharmacy ID of the currently authenticated user
     * @return Pharmacy ID
     */
    public Long getCurrentUserPharmacyId() {
        return getCurrentUserPharmacy().getId();
    }

    /**
     * Validate that the current user has access to the specified pharmacy
     * @param pharmacyId The pharmacy ID to validate access for
     * @throws UnAuthorizedException if user doesn't have access
     */
    public void validatePharmacyAccess(Long pharmacyId) {
        Long currentUserPharmacyId = getCurrentUserPharmacyId();
        
        if (!currentUserPharmacyId.equals(pharmacyId)) {
            throw new UnAuthorizedException("User does not have access to pharmacy with ID: " + pharmacyId);
        }
    }

    /**
     * Validate that the current user has access to the specified pharmacy
     * @param pharmacy The pharmacy object to validate access for
     * @throws UnAuthorizedException if user doesn't have access
     */
    public void validatePharmacyAccess(Pharmacy pharmacy) {
        if (pharmacy == null) {
            throw new UnAuthorizedException("Pharmacy is null");
        }
        validatePharmacyAccess(pharmacy.getId());
    }

    /**
     * Get the current authenticated user
     * @return User object
     * @throws UnAuthorizedException if user is not authenticated
     */
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof User user) {
            return user;
        }
        
        throw new UnAuthorizedException("User is not properly authenticated");
    }

    /**
     * Check if the current user is an employee
     * @return true if user is an employee
     */
    public boolean isCurrentUserEmployee() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return principal instanceof Employee;
        } catch (Exception e) {
            return false;
        }
    }
} 