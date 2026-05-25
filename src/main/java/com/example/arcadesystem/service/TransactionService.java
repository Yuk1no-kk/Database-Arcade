package com.example.arcadesystem.service;

import com.example.arcadesystem.dao.MachineDao;
import com.example.arcadesystem.dao.MemberDao;
import com.example.arcadesystem.dao.TransactionDao;
import com.example.arcadesystem.exception.BusinessException;
import com.example.arcadesystem.exception.NotFoundException;
import com.example.arcadesystem.model.Machine;
import com.example.arcadesystem.model.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionDao transactionDao;
    private final MemberDao memberDao;
    private final MachineDao machineDao;

    public TransactionService(TransactionDao transactionDao, MemberDao memberDao, MachineDao machineDao) {
        this.transactionDao = transactionDao;
        this.memberDao = memberDao;
        this.machineDao = machineDao;
    }

    @Transactional
    public void recharge(int memberId, BigDecimal amount) {
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("会员不存在");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }
        int tokens = amount.intValue() * 10;
        transactionDao.saveTransaction(memberId, amount, tokens);
        transactionDao.addTokens(memberId, tokens, amount);
    }

    @Transactional
    public void consume(int memberId, int machineId, int tokens) {
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("会员不存在");
        }
        Machine machine = machineDao.findById(machineId);
        if (machine == null) {
            throw new NotFoundException("游戏机不存在");
        }
        if (member.getTokenBalance() < tokens) {
            throw new BusinessException("余额不足，无法消费");
        }
        if (tokens <= 0) {
            throw new BusinessException("消耗代币数必须大于0");
        }

        transactionDao.deductTokens(memberId, tokens);
        transactionDao.recordGameSession(memberId, machineId, tokens);
    }

    public Map<String, Object> listTransactions(int page, int size) {
        List<Map<String, Object>> list = transactionDao.findUnifiedRecords(page, size);
        int total = transactionDao.countUnifiedRecords();
        return Map.of("list", list, "total", total);
    }
}
