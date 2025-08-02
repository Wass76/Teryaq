package com.Teryaq.user.controller;

import com.Teryaq.user.dto.SupplierDTORequest;
import com.Teryaq.user.dto.SupplierDTOResponse;
import com.Teryaq.user.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<SupplierDTOResponse> create(@RequestBody SupplierDTORequest request) {
        return ResponseEntity.ok(supplierService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTOResponse> update(@PathVariable Long id, @RequestBody SupplierDTORequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTOResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SupplierDTOResponse>> listAll() {
        return ResponseEntity.ok(supplierService.listAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SupplierDTOResponse>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchByName(name));
    }
} 