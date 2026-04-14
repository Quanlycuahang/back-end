package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.dto.role.RoleRequest;
import com.example.DoantotnghiepIJ.dto.role.RoleResponse;
import com.example.DoantotnghiepIJ.entity.Permission;
import com.example.DoantotnghiepIJ.entity.Role;
import com.example.DoantotnghiepIJ.mapper.RoleMapper;
import com.example.DoantotnghiepIJ.repository.PermissionRepository;
import com.example.DoantotnghiepIJ.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Role code already exists");
        }

        Role role = roleMapper.toEntity(request);
        return roleMapper.toResponse(roleRepository.save(role));
    }
    
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .filter(r -> Boolean.FALSE.equals(r.getIsDeleted()))
                .map(roleMapper::toResponse)
                .toList();
    }

    public RoleResponse getById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        return roleMapper.toResponse(role);
    }

    public RoleResponse update(UUID id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setUpdatedAt(LocalDateTime.now());

        return roleMapper.toResponse(roleRepository.save(role));
    }

    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new RuntimeException("Cannot delete system role");
        }

        role.setIsDeleted(true);
        role.setUpdatedAt(LocalDateTime.now());

        roleRepository.save(role);
    }

    public void assignPermissions(UUID roleId, List<UUID> permissionIds) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        role.setPermissions(new HashSet<>(permissions));

        roleRepository.save(role);
    }
}