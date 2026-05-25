package com.example.arcadesystem.controller;

import com.example.arcadesystem.dto.ApiResponse;
import com.example.arcadesystem.model.Staff;
import com.example.arcadesystem.service.StaffService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final StaffService staffService;

    public AuthController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping("/auth/register")
    public ApiResponse<Staff> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String permissionLevel = body.get("permissionLevel");
        Staff staff = staffService.register(username, password, permissionLevel);
        return ApiResponse.ok("注册成功", staff);
    }

    @PostMapping("/auth/login")
    public ApiResponse<Staff> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Staff staff = staffService.login(username, password);
        return ApiResponse.ok("登录成功", staff);
    }

    // ---- staff management (admin only, excludes self) ----

    @GetMapping("/staff")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam int excludeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "staff_id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(staffService.listOthers(excludeId, keyword, sort, order, page, size));
    }

    @PutMapping("/staff/{id}/permission")
    public ApiResponse<Void> updatePermission(@PathVariable int id, @RequestBody Map<String, String> body) {
        staffService.updatePermission(id, body.get("permissionLevel"));
        return ApiResponse.ok("修改成功", null);
    }

    @DeleteMapping("/staff/{id}")
    public ApiResponse<Void> delete(@PathVariable int id) {
        staffService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
