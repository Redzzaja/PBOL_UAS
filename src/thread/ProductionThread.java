package thread;

import manager.ProductionManager;
import model.Machine;
import model.Operator;
import model.WorkOrder;
import model.WorkOrderStatus;

/**
 * Thread untuk eksekusi produksi
 * Bagian D - Multithreading
 * Bagian E - Sinkronisasi Thread
 */
public class ProductionThread extends Thread {
    private final WorkOrder workOrder;
    private final ProductionManager manager;
    private volatile boolean isRunning;
    private volatile boolean isPaused;
    private int productionSpeed; // items per second (simulated)
    
    /**
     * Constructor
     */
    public ProductionThread(WorkOrder workOrder, ProductionManager manager) {
        super("Production-" + workOrder.getWorkOrderId());
        this.workOrder = workOrder;
        this.manager = manager;
        this.isRunning = false;
        this.isPaused = false;
        this.productionSpeed = 100; // default speed
        
        // Set thread priority based on work order priority
        setPriority(calculateThreadPriority(workOrder.getPriority()));
    }
    
    /**
     * Calculate thread priority based on work order priority
     */
    private int calculateThreadPriority(int workOrderPriority) {
        switch (workOrderPriority) {
            case 1: return Thread.MAX_PRIORITY;    // Highest
            case 2: return Thread.NORM_PRIORITY + 2;
            case 3: return Thread.NORM_PRIORITY + 1;
            case 4: return Thread.NORM_PRIORITY;
            case 5: return Thread.NORM_PRIORITY - 1;
            default: return Thread.NORM_PRIORITY;
        }
    }
    
    @Override
    public void run() {
        isRunning = true;
        
        try {
            // Update status
            synchronized (workOrder) {
                workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
            }
            
            Machine machine = workOrder.getAssignedMachine();
            Operator operator = workOrder.getAssignedOperator();
            
            System.out.println("[ProductionThread] Started: " + workOrder.getWorkOrderId() +
                " | Product: " + workOrder.getProductName() +
                " | Machine: " + (machine != null ? machine.getName() : "N/A") +
                " | Operator: " + (operator != null ? operator.getName() : "N/A"));
            
            // Simulate production
            simulateProduction();
            
            // Complete production
            completeProduction();
            
        } catch (InterruptedException e) {
            System.out.println("[ProductionThread] Interrupted: " + workOrder.getWorkOrderId());
            handleProductionInterrupted();
        } catch (Exception e) {
            System.err.println("[ProductionThread] Error in production: " + e.getMessage());
            handleProductionError(e);
        } finally {
            isRunning = false;
            manager.onProductionComplete(workOrder.getWorkOrderId());
        }
    }
    
    /**
     * Simulate production process dengan sinkronisasi
     * Bagian E - Sinkronisasi Thread
     */
    private void simulateProduction() throws InterruptedException {
        int targetQuantity = workOrder.getQuantity();
        int batchSize = 10; // Update setiap 10 items
        
        while (workOrder.getCompletedQuantity() < targetQuantity && isRunning && !Thread.currentThread().isInterrupted()) {
            
            // Check if paused
            while (isPaused) {
                Thread.sleep(100);
            }
            
            // Synchronized access ke work order dan machine
            synchronized (workOrder) {
                Machine machine = workOrder.getAssignedMachine();
                
                if (machine != null) {
                    synchronized (machine) {
                        // Simulate production batch
                        int remaining = targetQuantity - workOrder.getCompletedQuantity();
                        int produce = Math.min(batchSize, remaining);
                        
                        // Simulate time based on machine capacity
                        int sleepTime = calculateProductionTime(machine.getCapacityPerHour(), produce);
                        
                        // Release lock during sleep
                        workOrder.wait(sleepTime);
                        
                        // Update completed quantity
                        workOrder.updateCompletedQuantity(produce);
                        
                        // Print progress
                        printProgress();
                    }
                } else {
                    throw new IllegalStateException("Machine not assigned to work order");
                }
            }
            
            // Small delay untuk prevent CPU overuse
            Thread.sleep(50);
        }
    }
    
    /**
     * Calculate production time based on machine capacity
     */
    private int calculateProductionTime(double capacityPerHour, int quantity) {
        // Convert capacity per hour to milliseconds per item
        double msPerItem = (3600.0 * 1000.0) / capacityPerHour;
        return (int)(msPerItem * quantity / 100); // Divided by 100 for simulation speed
    }
    
    /**
     * Print production progress
     */
    private void printProgress() {
        int completed = workOrder.getCompletedQuantity();
        int total = workOrder.getQuantity();
        double percentage = workOrder.getProgressPercentage();
        
        StringBuilder progressBar = new StringBuilder("[");
        int filled = (int)(percentage / 5);
        for (int i = 0; i < 20; i++) {
            progressBar.append(i < filled ? "█" : "░");
        }
        progressBar.append("]");
        
        System.out.printf("[Production] %s | %s | %d/%d (%.1f%%)%n",
            workOrder.getWorkOrderId(),
            progressBar.toString(),
            completed, total, percentage);
    }
    
    /**
     * Handle production completion
     */
    private void completeProduction() {
        synchronized (workOrder) {
            workOrder.setStatus(WorkOrderStatus.COMPLETED);
            
            // Release resources
            Machine machine = workOrder.getAssignedMachine();
            Operator operator = workOrder.getAssignedOperator();
            
            if (machine != null) {
                synchronized (machine) {
                    machine.setStatus(model.MachineStatus.AVAILABLE);
                }
            }
            
            if (operator != null) {
                synchronized (operator) {
                    operator.setStatus(model.OperatorStatus.AVAILABLE);
                }
            }
            
            System.out.println("[ProductionThread] Completed: " + workOrder.getWorkOrderId() +
                " | Duration: " + workOrder.getProductionDuration() +
                " | Total: " + workOrder.getCompletedQuantity() + "/" + workOrder.getQuantity());
        }
    }
    
    /**
     * Handle production interrupted
     */
    private void handleProductionInterrupted() {
        synchronized (workOrder) {
            if (workOrder.getStatus() == WorkOrderStatus.IN_PROGRESS) {
                workOrder.setStatus(WorkOrderStatus.ON_HOLD);
            }
            
            // Release resources
            Machine machine = workOrder.getAssignedMachine();
            Operator operator = workOrder.getAssignedOperator();
            
            if (machine != null) {
                synchronized (machine) {
                    machine.setStatus(model.MachineStatus.AVAILABLE);
                }
            }
            
            if (operator != null) {
                synchronized (operator) {
                    operator.setStatus(model.OperatorStatus.AVAILABLE);
                }
            }
        }
    }
    
    /**
     * Handle production error
     */
    private void handleProductionError(Exception e) {
        synchronized (workOrder) {
            workOrder.setStatus(WorkOrderStatus.ON_HOLD);
            workOrder.setNotes("Error: " + e.getMessage());
            
            // Release resources
            Machine machine = workOrder.getAssignedMachine();
            Operator operator = workOrder.getAssignedOperator();
            
            if (machine != null) {
                synchronized (machine) {
                    machine.setStatus(model.MachineStatus.AVAILABLE);
                }
            }
            
            if (operator != null) {
                synchronized (operator) {
                    operator.setStatus(model.OperatorStatus.AVAILABLE);
                }
            }
        }
    }
    
    /**
     * Check if thread is running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Pause production
     */
    public void pauseProduction() {
        isPaused = true;
        System.out.println("[ProductionThread] Paused: " + workOrder.getWorkOrderId());
    }
    
    /**
     * Resume production
     */
    public void resumeProduction() {
        isPaused = false;
        synchronized (workOrder) {
            workOrder.notify();
        }
        System.out.println("[ProductionThread] Resumed: " + workOrder.getWorkOrderId());
    }
    
    /**
     * Stop production
     */
    public void stopProduction() {
        isRunning = false;
        this.interrupt();
    }
    
    /**
     * Get work order
     */
    public WorkOrder getWorkOrder() {
        return workOrder;
    }
    
    /**
     * Set production speed
     */
    public void setProductionSpeed(int speed) {
        this.productionSpeed = speed;
    }
}
