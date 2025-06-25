package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.PermissionResponseDTO;
import com.Teryaq.user.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponseDTO toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionResponseDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .isActive(permission.isActive())
                .isSystemGenerated(permission.isSystemGenerated())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .createdBy(permission.getCreatedBy())
                .updatedBy(permission.getLastModifiedBy())
                .build();
    }
} 