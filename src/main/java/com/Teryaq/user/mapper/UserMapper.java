package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.UserResponseDTO;
import com.Teryaq.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public UserMapper(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public UserResponseDTO toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .position(user.getPosition())
                .role(roleMapper.toResponse(user.getRole()))
                .additionalPermissions(user.getAdditionalPermissions().stream()
                        .map(permissionMapper::toResponse)
                        .collect(Collectors.toSet()))
                .build();
    }
} 