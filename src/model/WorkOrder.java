package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class Model untuk Work Order
 * Bagian B - Implementasi OOP
 * Bagian E - Sinkronisasi Thread untuk akses data bersama
 */
public class WorkOrder {
    private final String workOrderId;
    private final String productName;
    private final int quantity;
    private int completedQuantity;
    private WorkOrderStatus status;
    private Machine assignedMachine;
    private Operator assignedOperator;
    private final LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private final int priority;
    private String notes;
    
    // Counter untuk generate ID
    private static int workOrderCounter = 0;
    
    private static final DateTimeFormatter formatter = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructor untuk WorkOrder
     */
    public WorkOrder(String productName, int quantity, int priority) {
        this.workOrderId = generateWorkOrderId();
        this.productName = productName;
        this.quantity = quantity;
        this.priority = priority;
        this.completedQuantity = 0;
        this.status = WorkOrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.notes = "";
    }
    
    /**
     * Constructor dengan ID spesifik
     */
    public WorkOrder(String workOrderId, String productName, int quantity, int priority) {
        this.workOrderId = workOrderId;
        this.productName = productName;
        this.quantity = quantity;
        this.priority = priority;
        this.completedQuantity = 0;
        this.status = WorkOrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.notes = "";
    }
    
    private static synchronized String generateWorkOrderId() {
        return "WO-" + String.format("%05d", ++workOrderCounter);
    }
    
    // Getters
    public String getWorkOrderId() { return workOrderId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public synchronized int getCompletedQuantity() { return completedQuantity; }
    public synchronized WorkOrderStatus getStatus() { return status; }
    public synchronized Machine getAssignedMachine() { return assignedMachine; }
    public synchronized Operator getAssignedOperator() { return assignedOperator; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public synchronized LocalDateTime getStartedAt() { return startedAt; }
    public synchronized LocalDateTime getCompletedAt() { return completedAt; }
    public int getPriority() { return priority; }
    public synchronized String getNotes() { return notes; }
    
    // Setters dengan sinkronisasi untuk thread safety
    public synchronized void setStatus(WorkOrderStatus status) {
        WorkOrderStatus oldStatus = this.status;
        this.status = status;
        
        if (status == WorkOrderStatus.IN_PROGRESS && this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        if (status == WorkOrderStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
        
        System.out.println("\n[WorkOrder] " + workOrderId + " (" + productName + "): " + 
                oldStatus + " -> " + status);
    }
    
    public synchronized void setAssignedMachine(Machine machine) {
        this.assignedMachine = machine;
    }
    
    public synchronized void setAssignedOperator(Operator operator) {
        this.assignedOperator = operator;
    }
    
    public synchronized void setCompletedAt(LocalDateTime time) {
        this.completedAt = time;
    }
    
    public synchronized void setNotes(String notes) { this.notes = notes; }
    
    /**
     * Update completed quantity dengan thread safety
     * Bagian E - Sinkronisasi
     */
    public synchronized void updateCompletedQuantity(int amount) {
        this.completedQuantity += amount;
        if (this.completedQuantity >= this.quantity) {
            this.completedQuantity = this.quantity;
        }
    }
    
    /**
     * Get progress percentage
     */
    public synchronized double getProgressPercentage() {
        if (quantity == 0) return 0.0;
        return ((double) completedQuantity / quantity) * 100.0;
    }
    
    /**
     * Get estimated completion time
     * Bagian E - Sinkronisasi
     */
    public synchronized String getEstimatedCompletionTime() {
        if (assignedMachine == null || startedAt == null) {
            return "N/A";
        }
        
        double remaining = quantity - completedQuantity;
        double hours = remaining / assignedMachine.getCapacityPerHour();
        LocalDateTime estimated = LocalDateTime.now().plusSeconds((long)(hours * 3600));
        return estimated.format(formatter);
    }
    
    /**
     * Calculate production duration
     */
    public synchronized String getProductionDuration() {
        if (startedAt == null) {
            return "Not started";
        }
        
        LocalDateTime endTime = (completedAt != null) ? completedAt : LocalDateTime.now();
        long seconds = java.time.Duration.between(startedAt, endTime).getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("WorkOrder[%s] %s (Qty: %d/%d, Priority: %d, Status: %s)",
                workOrderId, productName, completedQuantity, quantity, priority, status));
        
        if (assignedMachine != null) {
            sb.append(String.format("\n  Machine: %s (%s)", 
                    assignedMachine.getName(), assignedMachine.getMachineId()));
        }
        if (assignedOperator != null) {
            sb.append(String.format("\n  Operator: %s (%s)", 
                    assignedOperator.getName(), assignedOperator.getOperatorId()));
        }
        
        sb.append(String.format("\n  Progress: %.1f%%", getProgressPercentage()));
        sb.append(String.format("\n  Created: %s", createdAt.format(formatter)));
        
        if (startedAt != null) {
            sb.append(String.format(", Started: %s", startedAt.format(formatter)));
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkOrder)) return false;
        WorkOrder workOrder = (WorkOrder) o;
        return workOrderId.equals(workOrder.workOrderId);
    }
    
    @Override
    public int hashCode() {
        return workOrderId.hashCode();
    }
}
