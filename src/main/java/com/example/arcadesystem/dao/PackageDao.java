package com.example.arcadesystem.dao;

import com.example.arcadesystem.model.TokenPackage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PackageDao {

    private final JdbcTemplate jdbc;

    public PackageDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<TokenPackage> rowMapper = (rs, rowNum) -> {
        TokenPackage p = new TokenPackage();
        p.setPackageId(rs.getInt("package_id"));
        p.setPackageName(rs.getString("package_name"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setTokenCount(rs.getInt("token_count"));
        return p;
    };

    public List<TokenPackage> findAll() {
        return jdbc.query("SELECT * FROM token_packages ORDER BY package_id", rowMapper);
    }

    public TokenPackage findById(int id) {
        List<TokenPackage> list = jdbc.query("SELECT * FROM token_packages WHERE package_id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public TokenPackage save(TokenPackage p) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO token_packages (package_name, price, token_count) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getPackageName());
            ps.setBigDecimal(2, p.getPrice());
            ps.setInt(3, p.getTokenCount());
            return ps;
        }, keyHolder);
        p.setPackageId(keyHolder.getKey().intValue());
        return p;
    }

    public void update(TokenPackage p) {
        jdbc.update("UPDATE token_packages SET package_name=?, price=?, token_count=? WHERE package_id=?",
                p.getPackageName(), p.getPrice(), p.getTokenCount(), p.getPackageId());
    }

    public void deleteById(int id) {
        jdbc.update("DELETE FROM token_packages WHERE package_id = ?", id);
    }
}
