package com.example.arcadesystem.dao;

import com.example.arcadesystem.model.Member;
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
public class MemberDao {

    private final JdbcTemplate jdbc;

    public MemberDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Member> rowMapper = (rs, rowNum) -> {
        Member m = new Member();
        m.setMemberId(rs.getInt("member_id"));
        m.setName(rs.getString("name"));
        m.setPhone(rs.getString("phone"));
        m.setTokenBalance(rs.getInt("token_balance"));
        m.setVipLevel(rs.getString("vip_level"));
        m.setAccumulatedSpend(rs.getBigDecimal("accumulated_spend"));
        return m;
    };

    public List<Member> findAll(String keyword, String sort, String order, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM members WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }

        // whitelist sort columns
        String col = switch (sort != null ? sort : "") {
            case "name" -> "name";
            case "vipLevel" -> "vip_level";
            case "tokenBalance" -> "token_balance";
            case "accumulatedSpend" -> "accumulated_spend";
            default -> "member_id";
        };
        String dir = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public int count(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM members WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }
        Integer result = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
        return result != null ? result : 0;
    }

    public Member findById(int id) {
        List<Member> list = jdbc.query("SELECT * FROM members WHERE member_id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Member save(Member member) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO members (name, phone, token_balance, vip_level, accumulated_spend) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, member.getName());
            ps.setString(2, member.getPhone());
            ps.setInt(3, member.getTokenBalance() != null ? member.getTokenBalance() : 0);
            ps.setString(4, member.getVipLevel() != null ? member.getVipLevel() : "普通会员");
            ps.setBigDecimal(5, member.getAccumulatedSpend() != null ? member.getAccumulatedSpend() : java.math.BigDecimal.ZERO);
            return ps;
        }, keyHolder);
        member.setMemberId(keyHolder.getKey().intValue());
        return member;
    }

    public void update(Member member) {
        jdbc.update("UPDATE members SET name=?, phone=?, token_balance=?, vip_level=?, accumulated_spend=? WHERE member_id=?",
                member.getName(),
                member.getPhone(),
                member.getTokenBalance(),
                member.getVipLevel(),
                member.getAccumulatedSpend(),
                member.getMemberId());
    }

    public void deleteById(int id) {
        jdbc.update("DELETE FROM members WHERE member_id = ?", id);
    }

    public boolean hasTransactions(int memberId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM token_transactions WHERE member_id = ?", Integer.class, memberId);
        return count != null && count > 0;
    }
}
