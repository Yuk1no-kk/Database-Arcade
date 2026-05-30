package com.example.arcadesystem.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TransactionDao {

    private final JdbcTemplate jdbc;

    public TransactionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveTransaction(int memberId, BigDecimal amount, int tokens, Integer packageId) {
        jdbc.update("INSERT INTO token_transactions (member_id, package_id, amount_paid, tokens_purchased) VALUES (?, ?, ?, ?)",
                memberId, packageId, amount, tokens);
    }

    public void addTokens(int memberId, int tokens, BigDecimal amount) {
        jdbc.update("UPDATE members SET token_balance = token_balance + ?, accumulated_spend = accumulated_spend + ? WHERE member_id = ?",
                tokens, amount, memberId);
    }

    public void deductTokens(int memberId, int tokens) {
        jdbc.update("UPDATE members SET token_balance = token_balance - ? WHERE member_id = ?",
                tokens, memberId);
    }

    public void nullifyPackageId(int packageId) {
        jdbc.update("UPDATE token_transactions SET package_id = NULL WHERE package_id = ?", packageId);
    }

    public void recordGameSession(int memberId, int machineId, int tokens) {
        jdbc.update("INSERT INTO game_sessions (member_id, machine_id, token_consumed) VALUES (?, ?, ?)",
                memberId, machineId, tokens);
    }

    public List<Map<String, Object>> findUnifiedRecords(String keyword, int page, int size) {
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM (
                    SELECT t.transaction_id AS id, t.member_id, m.name AS member_name,
                           t.amount_paid AS amount, t.tokens_purchased AS tokens,
                           NULL AS machine_name, t.transaction_date AS time, 'recharge' AS type
                    FROM token_transactions t
                    JOIN members m ON t.member_id = m.member_id
                    UNION ALL
                    SELECT g.session_id AS id, g.member_id, m.name AS member_name,
                           NULL AS amount, -g.token_consumed AS tokens,
                           mc.name AS machine_name, g.start_time AS time, 'consume' AS type
                    FROM game_sessions g
                    JOIN members m ON g.member_id = m.member_id
                    JOIN machines mc ON g.machine_id = mc.machine_id
                ) combined
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" WHERE member_name LIKE ? ");
            params.add("%" + keyword + "%");
        }

        sql.append(" ORDER BY time DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public int countUnifiedRecords(String keyword) {
        String sql = """
                SELECT COUNT(*) FROM (
                    SELECT m.name AS member_name FROM token_transactions t
                    JOIN members m ON t.member_id = m.member_id
                    UNION ALL
                    SELECT m.name AS member_name FROM game_sessions g
                    JOIN members m ON g.member_id = m.member_id
                ) combined
                """;

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql += " WHERE member_name LIKE ? ";
            params.add("%" + keyword + "%");
        }

        Integer result = jdbc.queryForObject(sql, Integer.class, params.toArray());
        return result != null ? result : 0;
    }
}
