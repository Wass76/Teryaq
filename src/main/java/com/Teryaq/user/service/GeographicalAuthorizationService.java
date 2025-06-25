//package com.Teryaq.user.service;
//
//import com.Teryaq.address.entity.SystemCity;
//import com.Teryaq.address.entity.SystemCountry;
//import com.Teryaq.address.entity.SystemProvince;
//import com.Teryaq.user.entity.User;
//import com.Teryaq.user.repository.UserRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class GeographicalAuthorizationService extends BaseSecurityService {
//
//    private final UserTerritoryRepository userTerritoryRepository;
//
//    public GeographicalAuthorizationService(UserRepository userRepository, UserTerritoryRepository userTerritoryRepository) {
//        super(userRepository);
//        this.userTerritoryRepository = userTerritoryRepository;
//    }
//
//    /**
//     * Get the current user's ID
//     * @return Current user ID
//     */
//    public Long getCurrentUserId() {
//        return getCurrentUser().getId();
//    }
//
//    /**
//     * Check if the current user has access to a specific geographical location
//     */
//    public boolean hasAccessToLocation(SystemCity city, SystemProvince province, SystemCountry country) {
//        User currentUser = getCurrentUser();
//
//        // Platform admin has access everywhere
//        if (hasRole("PLATFORM_ADMIN")) {
//            return true;
//        }
//
//        // Check user territories
//        return userTerritoryRepository.hasAccessToLocation(
//            currentUser.getId(),
//            city != null ? city.getId() : null,
//            province != null ? province.getId() : null,
//            country != null ? country.getId() : null
//        );
//    }
//
//    /**
//     * Get all city IDs that the current user has access to
//     */
//    public List<Long> getAccessibleCityIds() {
//        User currentUser = getCurrentUser();
//
//        // Platform admin has access to all cities - return empty list to indicate no filtering needed
//        if (hasRole("PLATFORM_ADMIN")) {
//            return List.of();
//        }
//
//        return userTerritoryRepository.findAccessibleCityIds(currentUser.getId());
//    }
//
//    /**
//     * Get all province IDs that the current user has access to
//     */
//    public List<Long> getAccessibleProvinceIds() {
//        User currentUser = getCurrentUser();
//
//        // Platform admin has access to all provinces - return empty list to indicate no filtering needed
//        if (hasRole("PLATFORM_ADMIN")) {
//            return List.of();
//        }
//
//        return userTerritoryRepository.findAccessibleProvinceIds(currentUser.getId());
//    }
//
//    /**
//     * Get all country IDs that the current user has access to
//     */
//    public List<Long> getAccessibleCountryIds() {
//        User currentUser = getCurrentUser();
//
//        // Platform admin has access to all countries - return empty list to indicate no filtering needed
//        if (hasRole("PLATFORM_ADMIN")) {
//            return List.of();
//        }
//
//        return userTerritoryRepository.findAccessibleCountryIds(currentUser.getId());
//    }
//
//    /**
//     * Check if the current user can only access their own leads (Sales Agent)
//     */
//    public boolean canOnlyAccessOwnLeads() {
//        return hasRole("SALES_AGENT");
//    }
//
//    /**
//     * Check if the current user can access all leads in their territory (Sales Manager)
//     */
//    public boolean canAccessAllLeadsInTerritory() {
//        return hasRole("SALES_MANAGER");
//    }
//
//    /**
//     * Get all territories for the current user
//     */
//    public List<UserTerritory> getCurrentUserTerritories() {
//        User currentUser = getCurrentUser();
//        return userTerritoryRepository.findByUser_IdAndIsActiveTrue(currentUser.getId());
//    }
//
//    /**
//     * Get territories for a specific user
//     */
//    public List<UserTerritory> getUserTerritories(Long userId) {
//        return userTerritoryRepository.findByUser_IdAndIsActiveTrue(userId);
//    }
//
//    /**
//     * Check if user has any territory assignments
//     */
//    public boolean hasTerritoryAssignments(Long userId) {
//        List<UserTerritory> territories = userTerritoryRepository.findByUser_IdAndIsActiveTrue(userId);
//        return !territories.isEmpty();
//    }
//}