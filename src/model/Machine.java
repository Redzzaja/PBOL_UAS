package model;

import java.time.LocalDateTime;

/**
 * Class Model untuk Mesin Produksi
 * Bagian B - Implementasi OOP
 */
public class Machine {
    private final String machineId;
    private final String name;
    private final String type;
    private MachineStatus status;
    private final double capacityPerHour;
    private final String location;
    private LocalDateTime lastMaintenance;
    private LocalDateTime nextMaintenance;
    
    // Counter untuk generate ID
    private static int machineCounter = 1000;
    
    /**
     * Constructor untuk Machine
     */
    public Machine(String name, String type, double capacityPerHour, String location) {
        this.machineId = generateMachineId();
        this.name = name;
        this.type = type;
        this.capacityPerHour = capacityPerHour;
        this.location = location;
        this.status = MachineStatus.AVAILABLE;
        this.lastMaintenance = LocalDateTime.now();
        this.nextMaintenance = LocalDateTime.now().plusMonths(1);
    }
    
    /**
     * Constructor dengan ID spesifik (untuk testing)
     */
    public Machine(String machineId, String name, String type, double capacityPerHour, String location) {
        this.machineId = machineId;
        this.name = name;
        this.type = type;
        this.capacityPerHour = capacityPerHour;
        this.location = location;
        this.status = MachineStatus.AVAILABLE;
        this.lastMaintenance = LocalDateTime.now();
        this.nextMaintenance = LocalDateTime.now().plusMonths(1);
    }
    
    private static synchronized String generateMachineId() {
        return "MCH-" + (++machineCounter);
    }
    
    // Getters
    public String getMachineId() { return machineId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public MachineStatus getStatus() { return status; }
    public double getCapacityPerHour() { return capacityPerHour; }
    public String getLocation() { return location; }
    public LocalDateTime getLastMaintenance() { return lastMaintenance; }
    public LocalDateTime getNextMaintenance() { return nextMaintenance; }
    
    /**
     * Set status mesin
     * Bagian E - Sinkronisasi untuk thread safety
     */
    public synchronized void setStatus(MachineStatus status) {
        MachineStatus oldStatus = this.status;
        this.status = status;
        System.out.println("\n[Machine] " + name + " (" + machineId + "): " + 
                oldStatus + " -> " + status);
    }
    
    /**
     * Cek apakah mesin tersedia
     */
    public synchronized boolean isAvailable() {
        return status == MachineStatus.AVAILABLE;
    }
    
    /**
     * Melakukan maintenance pada mesin
     */
    public synchronized void performMaintenance() {
        this.lastMaintenance = LocalDateTime.now();
        this.nextMaintenance = LocalDateTime.now().plusMonths(1);
        this.status = MachineStatus.MAINTENANCE;
        System.out.println("\n[Machine] Maintenance performed on " + name);
    }
    
    /**
     * Menyelesaikan maintenance
     */
    public synchronized void completeMaintenance() {
        this.status = MachineStatus.AVAILABLE;
        System.out.println("\n[Machine] Maintenance completed on " + name);
    }
    
    @Override
    public String toString() {
        return String.format("Machine[%s] %s (Type: %s, Status: %s, Capacity: %.1f/h, Location: %s)",
                machineId, name, type, status.getDescription(), capacityPerHour, location);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Machine)) return false;
        Machine machine = (Machine) o;
        return machineId.equals(machine.machineId);
    }
    
    @Override
    public int hashCode() {
        return machineId.hashCode();
    }
}
