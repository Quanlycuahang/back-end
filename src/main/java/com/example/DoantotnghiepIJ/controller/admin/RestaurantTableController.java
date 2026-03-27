package com.example.DoantotnghiepIJ.controller.admin;

import com.example.DoantotnghiepIJ.dto.Table.RestaurantTableRequest;
import com.example.DoantotnghiepIJ.dto.Table.RestaurantTableResponse;
import com.example.DoantotnghiepIJ.service.RestaurantTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService service;

    @GetMapping
    public List<RestaurantTableResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RestaurantTableResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public RestaurantTableResponse create(@RequestBody RestaurantTableRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public RestaurantTableResponse update(@PathVariable UUID id,
                                          @RequestBody RestaurantTableRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        service.delete(id);
        return "Deleted successfully";
    }
}