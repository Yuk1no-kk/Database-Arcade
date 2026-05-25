package com.example.arcadesystem.dao;

import com.example.arcadesystem.model.TokenTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TransactionDao {

    private final JdbcTemplate jdbc;

    public TransactionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveTransaction(int memberId, BigDecimal amount, int tokens) {
        jdbc.update("INSERT INTO token_transactions (member_id, amount_paid, tokens_purchased) VALUES (?, ?, ?)",
                memberId, amount, tokens);
    }

    public void addTokens(int memberId, int tokens, BigDecimal amount) {
        jdbc.update("UPDATE members SET token_balance = token_balance + ?, accumulated_spend = accumulated_spend + ? WHERE member_id = ?",
                tokens, amount, memberId);
    }

    public void deductTokens(int memberId, int tokens) {
        jdbc.update("UPDATE members SET token_balance = token_balance - ? WHERE member_id = ?",
                tokens, memberId);
    }

    public void recordGameSession(int memberId, int machineId, int tokens) {
        jdbc.update("INSERT INTO game_sessions (member_id, machine_id, token_consumed) VALUES (?, ?, ?)",
                memberId, machineId, tokens);
    }

    public List<Map<String, Object>> findUnifiedRecords(int page, int size) {
        String sql = """
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
                ORDER BY time DESC
                LIMIT ? OFFSET ?
                """;
        return jdbc.queryForList(sql, size, (page - 1) * size);
    }

    public int countUnifiedRecords() {
        Integer recharge = jdbc.queryForObject("SELECT COUNT(*) FROM token_transactions", Integer.class);
        Integer consume = jdbc.queryForObject("SELECT COUNT(*) FROM game_sessions", Integer.class);
        return (recharge != null ? recharge : 0) + (consume != null ? consume : 0);
    }
}
