package com.example.arcadesystem.service;

import com.example.arcadesystem.dao.MemberDao;
import com.example.arcadesystem.exception.BusinessException;
import com.example.arcadesystem.exception.NotFoundException;
import com.example.arcadesystem.model.Member;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Map<String, Object> list(String keyword, String sort, String order, int page, int size) {
        List<Member> list = memberDao.findAll(keyword, sort, order, page, size);
        int total = memberDao.count(keyword);
        return Map.of("list", list, "total", total);
    }

    public Member getById(int id) {
        Member member = memberDao.findById(id);
        if (member == null) {
            throw new NotFoundException("Member not found");
        }
        return member;
    }

    public Member create(Member member) {
        if (member.getName() == null || member.getName().isBlank()) {
            throw new BusinessException("Name is required");
        }
        return memberDao.save(member);
    }

    public Member update(int id, Member member) {
        Member existing = getById(id);
        if (member.getName() == null || member.getName().isBlank()) {
            throw new BusinessException("Name is required");
        }
        existing.setName(member.getName());
        existing.setPhone(member.getPhone());
        existing.setTokenBalance(member.getTokenBalance());
        existing.setVipLevel(member.getVipLevel());
        existing.setAccumulatedSpend(member.getAccumulatedSpend());
        memberDao.update(existing);
        return existing;
    }

    public void delete(int id) {
        getById(id);
        if (memberDao.hasTransactions(id)) {
            throw new BusinessException("Cannot delete member with transaction records");
        }
        memberDao.deleteById(id);
    }
}
