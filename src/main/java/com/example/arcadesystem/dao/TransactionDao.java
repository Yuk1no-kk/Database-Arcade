package com.example.arcadesystem.dao;

import com.example.arcadesystem.model.TokenPackage;
import com.example.arcadesystem.model.TokenTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class TransactionDao {

    private final JdbcTemplate jdbc;

    public TransactionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<TokenPackage> packageRowMapper = (rs, rowNum) -> {
        TokenPackage p = new TokenPackage();
        p.setPackageId(rs.getInt("package_id"));
        p.setPackageName(rs.getString("package_name"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setTokenCount(rs.getInt("token_count"));
        return p;
    };

    private final RowMapper<TokenTransaction> transRowMapper = (rs, rowNum) -> {
        TokenTransaction t = new TokenTransaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setMemberId(rs.getInt("member_id"));
        t.setPackageId(rs.getInt("package_id"));
        t.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        t.setAmountPaid(rs.getBigDecimal("amount_paid"));
        t.setTokensPurchased(rs.getInt("tokens_purchased"));
        return t;
    };

    public List<TokenPackage> findAllPackages() {
        return jdbc.query("SELECT * FROM token_packages", packageRowMapper);
    }

    public TokenPackage findPackageById(int id) {
        List<TokenPackage> list = jdbc.query("SELECT * FROM token_packages WHERE package_id = ?", packageRowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public TokenTransaction saveTransaction(TokenTransaction tx) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO token_transactions (member_id, package_id, amount_paid, tokens_purchased) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, tx.getMemberId());
            ps.setInt(2, tx.getPackageId());
            ps.setBigDecimal(3, tx.getAmountPaid());
            ps.setInt(4, tx.getTokensPurchased());
            return ps;
        }, keyHolder);
        tx.setTransactionId(keyHolder.getKey().intValue());
        return tx;
    }

    public void addTokens(int memberId, int tokens, java.math.BigDecimal amount) {
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

    public List<TokenTransaction> findTransactions(int page, int size) {
        String sql = """
                SELECT t.*, m.name AS member_name, p.package_name
                FROM token_transactions t
                LEFT JOIN members m ON t.member_id = m.member_id
                LEFT JOIN token_packages p ON t.package_id = p.package_id
                ORDER BY t.transaction_date DESC
                LIMIT ? OFFSET ?
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            TokenTransaction t = new TokenTransaction();
            t.setTransactionId(rs.getInt("transaction_id"));
            t.setMemberId(rs.getInt("member_id"));
            t.setPackageId(rs.getInt("package_id"));
            t.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
            t.setAmountPaid(rs.getBigDecimal("amount_paid"));
            t.setTokensPurchased(rs.getInt("tokens_purchased"));
            t.setMemberName(rs.getString("member_name"));
            t.setPackageName(rs.getString("package_name"));
            return t;
        }, size, (page - 1) * size);
    }

    public int countTransactions() {
        Integer result = jdbc.queryForObject("SELECT COUNT(*) FROM token_transactions", Integer.class);
        return result != null ? result : 0;
    }
}
