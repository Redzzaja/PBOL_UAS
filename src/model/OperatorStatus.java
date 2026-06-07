package model;

/**
 * Enum untuk status operator
 */
public enum OperatorStatus {
    AVAILABLE("Available"),
    BUSY("Busy"),
    OFFLINE("Offline");
    
    private final String description;
    
    OperatorStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
