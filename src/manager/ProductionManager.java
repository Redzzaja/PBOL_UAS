package manager;

import exception.DuplicateWorkOrderException;
import exception.InvalidQuantityException;
import exception.MachineUnavailableException;
import model.*;
import thread.ProductionThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton Production Manager
 * Bagian B - Implementasi OOP
 * Bagian D - Multithreading
 * Bagian E - Sinkronisasi Thread
 */
public class ProductionManager {
    // Singleton instance
    private static volatile ProductionManager instance;
    private static final Object lock = new Object();
    
    // Data collections
    private final List<Machine> machines;
    private final List<Operator> operators;
    private final List<WorkOrder> workOrders;
    private final Map<String, ProductionThread> activeThreads;
    
    // Thread synchronization locks
    private final ReentrantLock machineLock;
    private final ReentrantLock operatorLock;
    private final ReentrantLock workOrderLock;
    
    /**
     * Private constructor untuk Singleton
     */
    private ProductionManager() {
        this.machines = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.workOrders = new ArrayList<>();
        this.activeThreads = new HashMap<>();
        this.machineLock = new ReentrantLock(true); // fair lock
        this.operatorLock = new ReentrantLock(true);
        this.workOrderLock = new ReentrantLock(true);
    }
    
    /**
     * Get singleton instance dengan thread safety
     * Bagian D - Multithreading synchronization
     */
    public static ProductionManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ProductionManager();
                }
            }
        }
        return instance;
    }
    
    // ==================== Machine Management ====================
    
    /**
     * Menambahkan mesin baru
     */
    public void addMachine(Machine machine) {
        machineLock.lock();
        try {
            machines.add(machine);
            System.out.println("[ProductionManager] Machine added: " + machine.getName());
        } finally {
            machineLock.unlock();
        }
    }
    
    /**
     * Mencari mesin berdasarkan ID
     */
    public Machine findMachine(String machineId) {
        machineLock.lock();
        try {
            return machines.stream()
                    .filter(m -> m.getMachineId().equals(machineId))
                    .findFirst()
                    .orElse(null);
        } finally {
            machineLock.unlock();
        }
    }
    
    /**
     * Mendapatkan daftar mesin yang tersedia
     */
    public List<Machine> getAvailableMachines() {
        machineLock.lock();
        try {
            List<Machine> available = new ArrayList<>();
            for (Machine m : machines) {
                if (m.isAvailable()) {
                    available.add(m);
                }
            }
            return available;
        } finally {
            machineLock.unlock();
        }
    }
    
    /**
     * Mendapatkan semua mesin
     */
    public List<Machine> getAllMachines() {
        machineLock.lock();
        try {
            return new ArrayList<>(machines);
        } finally {
            machineLock.unlock();
        }
    }
    
    // ==================== Operator Management ====================
    
    /**
     * Menambahkan operator baru
     */
    public void addOperator(Operator operator) {
        operatorLock.lock();
        try {
            operators.add(operator);
            System.out.println("[ProductionManager] Operator added: " + operator.getName());
        } finally {
            operatorLock.unlock();
        }
    }
    
    /**
     * Mencari operator berdasarkan ID
     */
    public Operator findOperator(String operatorId) {
        operatorLock.lock();
        try {
            return operators.stream()
                    .filter(o -> o.getOperatorId().equals(operatorId))
                    .findFirst()
                    .orElse(null);
        } finally {
            operatorLock.unlock();
        }
    }
    
    /**
     * Mendapatkan daftar operator yang tersedia
     */
    public List<Operator> getAvailableOperators() {
        operatorLock.lock();
        try {
            List<Operator> available = new ArrayList<>();
            for (Operator o : operators) {
                if (o.isAvailable()) {
                    available.add(o);
                }
            }
            return available;
        } finally {
            operatorLock.unlock();
        }
    }
    
    /**
     * Mendapatkan semua operator
     */
    public List<Operator> getAllOperators() {
        operatorLock.lock();
        try {
            return new ArrayList<>(operators);
        } finally {
            operatorLock.unlock();
        }
    }
    
    // ==================== Work Order Management ====================
    
    /**
     * Membuat work order baru dengan exception handling
     * Bagian C - Exception Handling
     */
    public WorkOrder createWorkOrder(String productName, int quantity, int priority)
            throws InvalidQuantityException, DuplicateWorkOrderException {

        // Validasi quantity
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity, "Quantity must be greater than 0");
        }
        if (quantity > 10000) {
            throw new InvalidQuantityException(quantity, 10000);
        }

        workOrderLock.lock();
        try {
            // Cek duplikasi
            for (WorkOrder wo : workOrders) {
                if (wo.getProductName().equalsIgnoreCase(productName) &&
                    wo.getStatus() == WorkOrderStatus.PENDING) {
                    throw new DuplicateWorkOrderException(
                        "PENDING", wo.getWorkOrderId(), productName);
                }
            }

            WorkOrder workOrder = new WorkOrder(productName, quantity, priority);
            workOrders.add(workOrder);
            System.out.println("[ProductionManager] Work Order created: " + workOrder.getWorkOrderId());
            return workOrder;

        } finally {
            workOrderLock.unlock();
        }
    }
    
    /**
     * Mencari work order berdasarkan ID
     */
    public WorkOrder findWorkOrder(String workOrderId) {
        workOrderLock.lock();
        try {
            return workOrders.stream()
                    .filter(wo -> wo.getWorkOrderId().equals(workOrderId))
                    .findFirst()
                    .orElse(null);
        } finally {
            workOrderLock.unlock();
        }
    }
    
    /**
     * Menetapkan work order ke mesin dan operator dengan exception handling
     * Bagian C - Exception Handling
     * Bagian E - Sinkronisasi
     */
    public synchronized void assignWorkOrder(String workOrderId, String machineId, String operatorId) 
            throws MachineUnavailableException, IllegalStateException {
        
        WorkOrder workOrder = findWorkOrder(workOrderId);
        if (workOrder == null) {
            throw new IllegalStateException("Work Order not found: " + workOrderId);
        }
        
        Machine machine = findMachine(machineId);
        if (machine == null) {
            throw new IllegalStateException("Machine not found: " + machineId);
        }
        
        Operator operator = findOperator(operatorId);
        if (operator == null) {
            throw new IllegalStateException("Operator not found: " + operatorId);
        }
        
        // Cek ketersediaan dengan synchronized block
        synchronized (machine) {
            if (!machine.isAvailable()) {
                throw new MachineUnavailableException(
                    machineId, machine.getName(), 
                    "Machine is currently " + machine.getStatus());
            }
            
            synchronized (operator) {
                if (!operator.isAvailable()) {
                    throw new IllegalStateException(
                        "Operator " + operator.getName() + " is not available (" + operator.getStatus() + ")");
                }
                
                // Set assignment
                workOrder.setAssignedMachine(machine);
                workOrder.setAssignedOperator(operator);
                
                // Lock resources
                machine.setStatus(MachineStatus.BUSY);
                operator.setStatus(OperatorStatus.BUSY);
                
                System.out.println("[ProductionManager] Work Order " + workOrderId + 
                    " assigned to Machine " + machineId + " and Operator " + operatorId);
            }
        }
    }
    
    /**
     * Memulai produksi work order dengan multithreading
     * Bagian D - Multithreading
     */
    public synchronized void startProduction(String workOrderId) throws IllegalStateException {
        WorkOrder workOrder = findWorkOrder(workOrderId);
        if (workOrder == null) {
            throw new IllegalStateException("Work Order not found: " + workOrderId);
        }
        
        if (workOrder.getAssignedMachine() == null || workOrder.getAssignedOperator() == null) {
            throw new IllegalStateException("Work Order must be assigned before starting production");
        }
        
        if (workOrder.getStatus() != WorkOrderStatus.PENDING) {
            throw new IllegalStateException("Work Order is not in PENDING status");
        }
        
        // Cek apakah sudah ada thread aktif
        if (activeThreads.containsKey(workOrderId)) {
            throw new IllegalStateException("Production already started for work order: " + workOrderId);
        }
        
        // Buat dan jalankan thread produksi
        ProductionThread productionThread = new ProductionThread(workOrder, this);
        activeThreads.put(workOrderId, productionThread);
        productionThread.start();
        
        System.out.println("[ProductionManager] Production started for: " + workOrderId);
    }
    
    /**
     * Menghentikan produksi
     */
    public synchronized void stopProduction(String workOrderId) {
        ProductionThread thread = activeThreads.get(workOrderId);
        if (thread != null) {
            thread.interrupt();
            activeThreads.remove(workOrderId);
            System.out.println("[ProductionManager] Production stopped for: " + workOrderId);
        }
    }
    
    /**
     * Callback saat produksi selesai
     */
    public synchronized void onProductionComplete(String workOrderId) {
        activeThreads.remove(workOrderId);
        System.out.println("[ProductionManager] Production completed: " + workOrderId);
    }
    
    /**
     * Membatalkan work order
     */
    public synchronized void cancelWorkOrder(String workOrderId) {
        WorkOrder workOrder = findWorkOrder(workOrderId);
        if (workOrder != null) {
            // Stop production if running
            stopProduction(workOrderId);
            
            // Release resources
            if (workOrder.getAssignedMachine() != null) {
                workOrder.getAssignedMachine().setStatus(MachineStatus.AVAILABLE);
            }
            if (workOrder.getAssignedOperator() != null) {
                workOrder.getAssignedOperator().setStatus(OperatorStatus.AVAILABLE);
            }
            
            workOrder.setStatus(WorkOrderStatus.CANCELLED);
            System.out.println("[ProductionManager] Work Order cancelled: " + workOrderId);
        }
    }
    
    /**
     * Mendapatkan semua work order
     */
    public List<WorkOrder> getAllWorkOrders() {
        workOrderLock.lock();
        try {
            return new ArrayList<>(workOrders);
        } finally {
            workOrderLock.unlock();
        }
    }
    
    /**
     * Mendapatkan work order berdasarkan status
     */
    public List<WorkOrder> getWorkOrdersByStatus(WorkOrderStatus status) {
        workOrderLock.lock();
        try {
            List<WorkOrder> result = new ArrayList<>();
            for (WorkOrder wo : workOrders) {
                if (wo.getStatus() == status) {
                    result.add(wo);
                }
            }
            return result;
        } finally {
            workOrderLock.unlock();
        }
    }
    
    /**
     * Mendapatkan work order yang sedang aktif
     */
    public List<WorkOrder> getActiveWorkOrders() {
        return getWorkOrdersByStatus(WorkOrderStatus.IN_PROGRESS);
    }
    
    /**
     * Mendapatkan active threads
     */
    public Map<String, ProductionThread> getActiveThreads() {
        return new HashMap<>(activeThreads);
    }
    
    /**
     * Get system status summary
     * Bagian E - Sinkronisasi: acquire all locks before reading
     */
    public void printSystemStatus() {
        workOrderLock.lock();
        try {
            machineLock.lock();
            try {
                operatorLock.lock();
                try {
                    System.out.println("\n========================================");
                    System.out.println("      SYSTEM STATUS REPORT");
                    System.out.println("========================================");

                    System.out.println("\n--- Machines (" + machines.size() + ") ---");
                    for (Machine m : machines) {
                        System.out.println("  " + m);
                    }

                    System.out.println("\n--- Operators (" + operators.size() + ") ---");
                    for (Operator o : operators) {
                        System.out.println("  " + o);
                    }

                    System.out.println("\n--- Work Orders (" + workOrders.size() + ") ---");
                    for (WorkOrder wo : workOrders) {
                        System.out.println("  " + wo.getWorkOrderId() + " | " + wo.getProductName() +
                            " | " + wo.getStatus() + " | " + String.format("%.1f%%", wo.getProgressPercentage()));
                    }

                    System.out.println("\n--- Active Productions (" + activeThreads.size() + ") ---");
                    for (String woId : activeThreads.keySet()) {
                        System.out.println("  " + woId + " [RUNNING]");
                    }

                    System.out.println("========================================\n");
                } finally {
                    operatorLock.unlock();
                }
            } finally {
                machineLock.unlock();
            }
        } finally {
            workOrderLock.unlock();
        }
    }
    
    /**
     * Reset singleton (untuk testing)
     */
    public static void resetInstance() {
        synchronized (lock) {
            instance = null;
        }
    }
}
