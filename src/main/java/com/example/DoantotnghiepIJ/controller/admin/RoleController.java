package com.example.DoantotnghiepIJ.controller.admin;

import com.example.DoantotnghiepIJ.dto.role.RoleRequest;
import com.example.DoantotnghiepIJ.dto.role.RoleResponse;
import com.example.DoantotnghiepIJ.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponse> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable UUID id) {
        return roleService.getById(id);
    }

    @PostMapping
    public RoleResponse create(@RequestBody RoleRequest request) {
        return roleService.create(request);
    }

    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable UUID id,
                               @RequestBody RoleRequest request) {
        return roleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        roleService.delete(id);
    }

    @PostMapping("/{roleId}/permissions")
    public void assignPermissions(
            @PathVariable UUID roleId,
            @RequestBody List<UUID> permissionIds
    ) {
        roleService.assignPermissions(roleId, permissionIds);
    }
    @PutMapping("/{id}/enable")
    public String enable(@PathVariable UUID id) {
        roleService.enable(id);
        return "Role enabled successfully";
    }
    @GetMapping("/{roleId}/permissions")
    public List<UUID> getPermissionsByRole(@PathVariable UUID roleId) {
        return roleService.getPermissionIdsByRole(roleId);
    }
    //  Disable role
    @PutMapping("/{id}/disable")
    public String disable(@PathVariable UUID id) {
        roleService.disable(id);
        return "Role disabled successfully";
    }
}
