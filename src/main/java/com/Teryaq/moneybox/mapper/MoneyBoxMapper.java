package com.Teryaq.moneybox.mapper;

import com.Teryaq.moneybox.dto.MoneyBoxRequestDTO;
import com.Teryaq.moneybox.dto.MoneyBoxResponseDTO;
import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.enums.MoneyBoxStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MoneyBoxMapper {
    
    /**
     * Convert MoneyBoxRequestDTO to MoneyBox entity
     * Note: pharmacyId should be set separately in service from current user context
     */
    public static MoneyBox toEntity(MoneyBoxRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        MoneyBox entity = new MoneyBox();
        entity.setInitialBalance(dto.getInitialBalance());
        entity.setCurrentBalance(dto.getInitialBalance()); // Set current balance to initial balance
        entity.setCurrency(dto.getCurrency());
        entity.setStatus(MoneyBoxStatus.PENDING); // Default status
        // pharmacyId will be set from current user context in service
        // createdAt and updatedAt will be automatically set by JPA annotations
        
        return entity;
    }
    
    /**
     * Convert MoneyBox entity to MoneyBoxResponseDTO
     */
    public static MoneyBoxResponseDTO toResponseDTO(MoneyBox entity) {
        if (entity == null) {
            return null;
        }
        
        MoneyBoxResponseDTO dto = new MoneyBoxResponseDTO();
        dto.setId(entity.getId());
        dto.setPharmacyId(entity.getPharmacyId());
        dto.setCurrentBalance(entity.getCurrentBalance());
        dto.setInitialBalance(entity.getInitialBalance());
        dto.setLastReconciled(entity.getLastReconciled());
        dto.setReconciledBalance(entity.getReconciledBalance());
        dto.setStatus(entity.getStatus());
        dto.setCurrency(entity.getCurrency());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * Convert List of MoneyBox entities to List of MoneyBoxResponseDTO
     */
    public static List<MoneyBoxResponseDTO> toResponseDTOList(List<MoneyBox> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(MoneyBoxMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Update MoneyBox entity with values from MoneyBoxRequestDTO
     * Note: pharmacyId is not updated as it should not be changed
     */
    public static void updateEntity(MoneyBox entity, MoneyBoxRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        // Only update fields that should be updatable
        if (dto.getInitialBalance() != null) {
            entity.setInitialBalance(dto.getInitialBalance());
        }
        
        if (dto.getCurrency() != null) {
            entity.setCurrency(dto.getCurrency());
        }
        
        // Note: pharmacyId should not be updated from DTO
        // Note: updatedAt will be automatically updated by JPA @UpdateTimestamp annotation
    }
}
