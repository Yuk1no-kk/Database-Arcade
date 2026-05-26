package com.example.arcadesystem.controller;

import com.example.arcadesystem.dto.ApiResponse;
import com.example.arcadesystem.model.TokenPackage;
import com.example.arcadesystem.service.PackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public ApiResponse<List<TokenPackage>> list() {
        return ApiResponse.ok(packageService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<TokenPackage> getById(@PathVariable int id) {
        return ApiResponse.ok(packageService.getById(id));
    }

    @PostMapping
    public ApiResponse<TokenPackage> create(@RequestBody TokenPackage p) {
        return ApiResponse.ok("Created successfully", packageService.create(p));
    }

    @PutMapping("/{id}")
    public ApiResponse<TokenPackage> update(@PathVariable int id, @RequestBody TokenPackage p) {
        return ApiResponse.ok("Updated successfully", packageService.update(id, p));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable int id) {
        packageService.delete(id);
        return ApiResponse.ok("Deleted successfully", null);
    }
}
