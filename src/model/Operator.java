package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Model untuk Operator Produksi
 * Bagian B - Implementasi OOP
 */
public class Operator {
    private final String operatorId;
    private final String name;
    private String skillLevel;
    private OperatorStatus status;
    private String shift;
    private final List<String> certifications;
    private LocalDateTime loginTime;
    
    // Counter untuk generate ID
    private static int operatorCounter = 100;
    
    /**
     * Constructor untuk Operator
     */
    public Operator(String name, String skillLevel, String shift) {
        this.operatorId = generateOperatorId();
        this.name = name;
        this.skillLevel = skillLevel;
        this.shift = shift;
        this.status = OperatorStatus.AVAILABLE;
        this.certifications = new ArrayList<>();
        this.loginTime = LocalDateTime.now();
    }
    
    /**
     * Constructor dengan ID spesifik
     */
    public Operator(String operatorId, String name, String skillLevel, String shift) {
        this.operatorId = operatorId;
        this.name = name;
        this.skillLevel = skillLevel;
        this.shift = shift;
        this.status = OperatorStatus.AVAILABLE;
        this.certifications = new ArrayList<>();
        this.loginTime = LocalDateTime.now();
    }
    
    private static synchronized String generateOperatorId() {
        return "OPR-" + (++operatorCounter);
    }
    
    // Getters
    public String getOperatorId() { return operatorId; }
    public String getName() { return name; }
    public String getSkillLevel() { return skillLevel; }
    public OperatorStatus getStatus() { return status; }
    public String getShift() { return shift; }
    public List<String> getCertifications() { return new ArrayList<>(certifications); }
    public LocalDateTime getLoginTime() { return loginTime; }
    
    // Setters
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public void setShift(String shift) { this.shift = shift; }
    
    /**
     * Set status operator dengan thread safety
     * Bagian E - Sinkronisasi
     */
    public synchronized void setStatus(OperatorStatus status) {
        OperatorStatus oldStatus = this.status;
        this.status = status;
        System.out.println("[Operator] " + name + " (" + operatorId + "): " + 
                oldStatus + " -> " + status);
    }
    
    /**
     * Menambahkan sertifikasi
     * Bagian E - Sinkronisasi
     */
    public synchronized void addCertification(String certification) {
        this.certifications.add(certification);
        System.out.println("[Operator] Added certification '" + certification + "' to " + name);
    }
    
    /**
     * Cek apakah operator tersedia
     */
    public synchronized boolean isAvailable() {
        return status == OperatorStatus.AVAILABLE;
    }
    
    /**
     * Operator login ke sistem
     * Bagian E - Sinkronisasi
     */
    public synchronized void login() {
        this.loginTime = LocalDateTime.now();
        this.status = OperatorStatus.AVAILABLE;
        System.out.println("[Operator] " + name + " logged in at " + loginTime);
    }
    
    /**
     * Operator logout dari sistem
     * Bagian E - Sinkronisasi
     */
    public synchronized void logout() {
        this.status = OperatorStatus.OFFLINE;
        System.out.println("[Operator] " + name + " logged out");
    }
    
    @Override
    public String toString() {
        return String.format("Operator[%s] %s (Skill: %s, Status: %s, Shift: %s, Certs: %d)",
                operatorId, name, skillLevel, status.getDescription(), shift, certifications.size());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operator)) return false;
        Operator operator = (Operator) o;
        return operatorId.equals(operator.operatorId);
    }
    
    @Override
    public int hashCode() {
        return operatorId.hashCode();
    }
}
