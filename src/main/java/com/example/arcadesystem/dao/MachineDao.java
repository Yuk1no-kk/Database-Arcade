package com.example.arcadesystem.dao;

import com.example.arcadesystem.model.Machine;
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
public class MachineDao {

    private final JdbcTemplate jdbc;

    public MachineDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Machine> rowMapper = (rs, rowNum) -> {
        Machine m = new Machine();
        m.setMachineId(rs.getInt("machine_id"));
        m.setName(rs.getString("name"));
        m.setType(rs.getString("type"));
        m.setTokensPerGame(rs.getInt("tokens_per_game"));
        m.setStatus(rs.getString("status"));
        m.setStaffId(rs.getInt("staff_id"));
        return m;
    };

    public List<Machine> findAll(String keyword, String sort, String order, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM machines WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (name LIKE ? OR type LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }

        String col = switch (sort != null ? sort : "") {
            case "name" -> "name";
            case "type" -> "type";
            case "tokensPerGame" -> "tokens_per_game";
            default -> "machine_id";
        };
        String dir = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public int count(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM machines WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (name LIKE ? OR type LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }
        Integer result = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
        return result != null ? result : 0;
    }

    public Machine findById(int id) {
        List<Machine> list = jdbc.query("SELECT * FROM machines WHERE machine_id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Machine save(Machine machine) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO machines (name, type, tokens_per_game, status, staff_id) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, machine.getName());
            ps.setString(2, machine.getType());
            ps.setInt(3, machine.getTokensPerGame() != null ? machine.getTokensPerGame() : 1);
            ps.setString(4, machine.getStatus() != null ? machine.getStatus() : "available");
            ps.setObject(5, machine.getStaffId());
            return ps;
        }, keyHolder);
        machine.setMachineId(keyHolder.getKey().intValue());
        return machine;
    }

    public void update(Machine machine) {
        jdbc.update("UPDATE machines SET name=?, type=?, tokens_per_game=?, status=?, staff_id=? WHERE machine_id=?",
                machine.getName(),
                machine.getType(),
                machine.getTokensPerGame(),
                machine.getStatus(),
                machine.getStaffId(),
                machine.getMachineId());
    }

    public void deleteById(int id) {
        jdbc.update("DELETE FROM machines WHERE machine_id = ?", id);
    }

    public boolean hasGameSessions(int machineId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM game_sessions WHERE machine_id = ?", Integer.class, machineId);
        return count != null && count > 0;
    }
}
