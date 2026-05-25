package com.example.arcadesystem.service;

import com.example.arcadesystem.dao.MachineDao;
import com.example.arcadesystem.dao.MemberDao;
import com.example.arcadesystem.dao.TransactionDao;
import com.example.arcadesystem.exception.BusinessException;
import com.example.arcadesystem.exception.NotFoundException;
import com.example.arcadesystem.model.Machine;
import com.example.arcadesystem.model.Member;
import com.example.arcadesystem.model.TokenPackage;
import com.example.arcadesystem.model.TokenTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<TokenPackage> getPackages() {
        return transactionDao.findAllPackages();
    }

    @Transactional
    public TokenTransaction recharge(int memberId, int packageId) {
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("会员不存在");
        }
        TokenPackage pkg = transactionDao.findPackageById(packageId);
        if (pkg == null) {
            throw new NotFoundException("套餐不存在");
        }

        TokenTransaction tx = new TokenTransaction();
        tx.setMemberId(memberId);
        tx.setPackageId(packageId);
        tx.setAmountPaid(pkg.getPrice());
        tx.setTokensPurchased(pkg.getTokenCount());
        transactionDao.saveTransaction(tx);

        transactionDao.addTokens(memberId, pkg.getTokenCount(), pkg.getPrice());
        return tx;
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
        List<TokenTransaction> list = transactionDao.findTransactions(page, size);
        int total = transactionDao.countTransactions();
        return Map.of("list", list, "total", total);
    }
}
