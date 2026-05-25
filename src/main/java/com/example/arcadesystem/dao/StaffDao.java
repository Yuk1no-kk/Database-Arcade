package com.example.arcadesystem.dao;

import com.example.arcadesystem.model.Staff;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StaffDao {

    private final JdbcTemplate jdbc;

    public StaffDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Staff> rowMapper = (rs, rowNum) -> {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setUsername(rs.getString("username"));
        s.setPassword(rs.getString("password"));
        s.setName(rs.getString("name"));
        s.setPermissionLevel(rs.getString("permission_level"));
        return s;
    };

    public Staff findByUsername(String username) {
        List<Staff> list = jdbc.query("SELECT * FROM staff WHERE username = ?", rowMapper, username);
        return list.isEmpty() ? null : list.get(0);
    }

    public Staff findByUsernameAndPassword(String username, String password) {
        List<Staff> list = jdbc.query(
                "SELECT * FROM staff WHERE username = ? AND password = ?", rowMapper, username, password);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM staff WHERE username = ?", Integer.class, username);
        return count != null && count > 0;
    }

    public Staff save(Staff staff) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO staff (username, password, name, permission_level) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, staff.getUsername());
            ps.setString(2, staff.getPassword());
            ps.setString(3, staff.getName());
            ps.setString(4, staff.getPermissionLevel());
            return ps;
        }, keyHolder);
        staff.setStaffId(keyHolder.getKey().intValue());
        return staff;
    }

    // ---- list all staff except self ----

    public List<Staff> findOthers(int excludeStaffId, String keyword, String sort, String order, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM staff WHERE staff_id != ?");
        List<Object> params = new ArrayList<>();
        params.add(excludeStaffId);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (username LIKE ? OR name LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }

        String col = switch (sort != null ? sort : "") {
            case "username" -> "username";
            default -> "staff_id";
        };
        String dir = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public int countOthers(int excludeStaffId, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM staff WHERE staff_id != ?");
        List<Object> params = new ArrayList<>();
        params.add(excludeStaffId);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (username LIKE ? OR name LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }
        Integer result = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
        return result != null ? result : 0;
    }

    public Staff findById(int id) {
        List<Staff> list = jdbc.query("SELECT * FROM staff WHERE staff_id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public void updatePermission(int staffId, String permissionLevel) {
        jdbc.update("UPDATE staff SET permission_level=? WHERE staff_id=?", permissionLevel, staffId);
    }

    public void deleteById(int id) {
        jdbc.update("DELETE FROM staff WHERE staff_id = ?", id);
    }
}
