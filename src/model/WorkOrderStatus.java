package model;

/**
 * Enum untuk status work order
 */
public enum WorkOrderStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    ON_HOLD("On Hold");
    
    private final String description;
    
    WorkOrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
