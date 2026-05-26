package com.example.arcadesystem.service;

import com.example.arcadesystem.dao.PackageDao;
import com.example.arcadesystem.exception.BusinessException;
import com.example.arcadesystem.exception.NotFoundException;
import com.example.arcadesystem.model.TokenPackage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PackageService {

    private final PackageDao packageDao;

    public PackageService(PackageDao packageDao) {
        this.packageDao = packageDao;
    }

    public List<TokenPackage> listAll() {
        return packageDao.findAll();
    }

    public TokenPackage getById(int id) {
        TokenPackage p = packageDao.findById(id);
        if (p == null) {
            throw new NotFoundException("Package not found");
        }
        return p;
    }

    public TokenPackage create(TokenPackage p) {
        if (p.getPackageName() == null || p.getPackageName().isBlank()) {
            throw new BusinessException("Package name is required");
        }
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be greater than 0");
        }
        if (p.getTokenCount() == null || p.getTokenCount() <= 0) {
            throw new BusinessException("Token count must be greater than 0");
        }
        return packageDao.save(p);
    }

    public TokenPackage update(int id, TokenPackage p) {
        TokenPackage existing = getById(id);
        if (p.getPackageName() == null || p.getPackageName().isBlank()) {
            throw new BusinessException("Package name is required");
        }
        existing.setPackageName(p.getPackageName());
        existing.setPrice(p.getPrice());
        existing.setTokenCount(p.getTokenCount());
        packageDao.update(existing);
        return existing;
    }

    public void delete(int id) {
        getById(id);
        packageDao.deleteById(id);
    }
}
