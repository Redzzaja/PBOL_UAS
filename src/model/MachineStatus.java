package model;

/**
 * Enum untuk status mesin
 */
public enum MachineStatus {
    AVAILABLE("Available"),
    BUSY("Busy"),
    MAINTENANCE("Under Maintenance"),
    OFFLINE("Offline");
    
    private final String description;
    
    MachineStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
