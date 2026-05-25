package com.example.arcadesystem.model;

public class Machine {
    private Integer machineId;
    private String name;
    private String type;
    private Integer tokensPerGame;
    private String status;
    private Integer staffId;

    public Integer getMachineId() { return machineId; }
    public void setMachineId(Integer machineId) { this.machineId = machineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getTokensPerGame() { return tokensPerGame; }
    public void setTokensPerGame(Integer tokensPerGame) { this.tokensPerGame = tokensPerGame; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
}
