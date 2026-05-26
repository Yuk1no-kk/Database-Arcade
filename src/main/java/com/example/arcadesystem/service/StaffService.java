package com.example.arcadesystem.service;

import com.example.arcadesystem.dao.StaffDao;
import com.example.arcadesystem.exception.BusinessException;
import com.example.arcadesystem.exception.NotFoundException;
import com.example.arcadesystem.model.Staff;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StaffService {

    private final StaffDao staffDao;

    public StaffService(StaffDao staffDao) {
        this.staffDao = staffDao;
    }

    public Staff login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException("Password is required");
        }
        Staff staff = staffDao.findByUsernameAndPassword(username, password);
        if (staff == null) {
            throw new BusinessException(401, "Invalid username or password");
        }
        return staff;
    }

    public Staff register(String username, String password, String permissionLevel) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException("Password is required");
        }
        if (!"admin".equals(permissionLevel) && !"worker".equals(permissionLevel)) {
            throw new BusinessException("Role must be admin or worker");
        }
        if (staffDao.existsByUsername(username)) {
            throw new BusinessException("Username already exists");
        }
        Staff staff = new Staff();
        staff.setUsername(username);
        staff.setPassword(password);
        staff.setPermissionLevel(permissionLevel);
        staff.setName(username);
        return staffDao.save(staff);
    }

    // ---- list other staff ----

    public Map<String, Object> listOthers(int excludeStaffId, String keyword, String sort, String order, int page, int size) {
        List<Staff> list = staffDao.findOthers(excludeStaffId, keyword, sort, order, page, size);
        int total = staffDao.countOthers(excludeStaffId, keyword);
        return Map.of("list", list, "total", total);
    }

    public Staff getById(int id) {
        Staff staff = staffDao.findById(id);
        if (staff == null) {
            throw new NotFoundException("Staff not found");
        }
        return staff;
    }

    public void updatePermission(int id, String permissionLevel) {
        Staff existing = getById(id);
        if (!"admin".equals(permissionLevel) && !"worker".equals(permissionLevel)) {
            throw new BusinessException("Role must be admin or worker");
        }
        staffDao.updatePermission(id, permissionLevel);
    }

    public void delete(int id) {
        getById(id);
        staffDao.deleteById(id);
    }
}
