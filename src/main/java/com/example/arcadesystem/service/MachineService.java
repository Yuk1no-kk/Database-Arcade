package com.example.arcadesystem.service;

import com.example.arcadesystem.dao.MachineDao;
import com.example.arcadesystem.exception.BusinessException;
import com.example.arcadesystem.exception.NotFoundException;
import com.example.arcadesystem.model.Machine;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MachineService {

    private final MachineDao machineDao;

    public MachineService(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

    public Map<String, Object> list(String keyword, String sort, String order, int page, int size) {
        List<Machine> list = machineDao.findAll(keyword, sort, order, page, size);
        int total = machineDao.count(keyword);
        return Map.of("list", list, "total", total);
    }

    public Machine getById(int id) {
        Machine machine = machineDao.findById(id);
        if (machine == null) {
            throw new NotFoundException("Machine not found");
        }
        return machine;
    }

    public Machine create(Machine machine) {
        if (machine.getName() == null || machine.getName().isBlank()) {
            throw new BusinessException("Name is required");
        }
        return machineDao.save(machine);
    }

    public Machine update(int id, Machine machine) {
        Machine existing = getById(id);
        if (machine.getName() == null || machine.getName().isBlank()) {
            throw new BusinessException("Name is required");
        }
        existing.setName(machine.getName());
        existing.setType(machine.getType());
        existing.setTokensPerGame(machine.getTokensPerGame());
        existing.setStatus(machine.getStatus());
        existing.setStaffId(machine.getStaffId());
        machineDao.update(existing);
        return existing;
    }

    public void delete(int id) {
        getById(id);
        if (machineDao.hasGameSessions(id)) {
            throw new BusinessException("Cannot delete machine with game session records");
        }
        machineDao.deleteById(id);
    }
}
