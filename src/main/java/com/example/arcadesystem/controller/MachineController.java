package com.example.arcadesystem.controller;

import com.example.arcadesystem.dto.ApiResponse;
import com.example.arcadesystem.model.Machine;
import com.example.arcadesystem.service.MachineService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "machine_id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(machineService.list(keyword, sort, order, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Machine> getById(@PathVariable int id) {
        return ApiResponse.ok(machineService.getById(id));
    }

    @PostMapping
    public ApiResponse<Machine> create(@RequestBody Machine machine) {
        return ApiResponse.ok("新增成功", machineService.create(machine));
    }

    @PutMapping("/{id}")
    public ApiResponse<Machine> update(@PathVariable int id, @RequestBody Machine machine) {
        return ApiResponse.ok("修改成功", machineService.update(id, machine));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable int id) {
        machineService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
