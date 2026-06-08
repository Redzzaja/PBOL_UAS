import exception.DuplicateWorkOrderException;
import exception.InvalidQuantityException;
import exception.MachineUnavailableException;
import manager.ProductionManager;
import model.Machine;
import model.Operator;
import model.WorkOrder;
import model.WorkOrderStatus;

import java.util.List;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       SMART MANUFACTURING WORK ORDER MANAGEMENT SYSTEM       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Get singleton instance
        ProductionManager manager = ProductionManager.getInstance();
        
        try {
            // Setup Sistem
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("  PHASE 1: SYSTEM INITIALIZATION");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
            // Buat mesin
            System.out.println("[SETUP] Creating Machines...");
            Machine machine1 = new Machine("MCH-001", "CNC Machine Alpha", "CNC", 120.0, "Line A");
            Machine machine2 = new Machine("MCH-002", "Assembly Robot Beta", "Robot", 200.0, "Line B");
            Machine machine3 = new Machine("MCH-003", "3D Printer Gamma", "3D Printer", 50.0, "Line C");
            
            manager.addMachine(machine1);
            manager.addMachine(machine2);
            manager.addMachine(machine3);
            
            // Buat operator
            System.out.println("\n[SETUP] Creating Operators...");
            Operator operator1 = new Operator("OPR-001", "Budi Santoso", "Senior", "Morning");
            Operator operator2 = new Operator("OPR-002", "Ahmad Wijaya", "Junior", "Morning");
            Operator operator3 = new Operator("OPR-003", "Siti Rahayu", "Senior", "Afternoon");
            
            // Add certifications
            operator1.addCertification("CNC Operation");
            operator1.addCertification("Quality Control");
            operator2.addCertification("Basic Assembly");
            operator3.addCertification("3D Printing");
            operator3.addCertification("Advanced Assembly");
            
            manager.addOperator(operator1);
            manager.addOperator(operator2);
            manager.addOperator(operator3);
            
            // Tampilkan status awal
            manager.printSystemStatus();
            
            // ==========================================
            // Demo Exception Handling
            // ==========================================
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("  PHASE 2: EXCEPTION HANDLING DEMONSTRATION");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
            // Demo InvalidQuantityException
            System.out.println("[DEMO] Testing InvalidQuantityException...");
            try {
                manager.createWorkOrder("Test Product", -10, 1);
            } catch (InvalidQuantityException e) {
                System.out.println("  ✓ Caught InvalidQuantityException: " + e.getMessage());
            }
            
            try {
                manager.createWorkOrder("Test Product", 15000, 1);
            } catch (InvalidQuantityException e) {
                System.out.println("  ✓ Caught InvalidQuantityException: " + e.getMessage());
            }
            
            // Demo DuplicateWorkOrderException
            System.out.println("\n[DEMO] Testing DuplicateWorkOrderException...");
            WorkOrder woTemp = manager.createWorkOrder("Duplicate Test", 100, 1);
            System.out.println("  Created: " + woTemp.getWorkOrderId());
            try {
                manager.createWorkOrder("Duplicate Test", 50, 2);
            } catch (DuplicateWorkOrderException e) {
                System.out.println("  ✓ Caught DuplicateWorkOrderException: " + e.getMessage());
            }
            
            // Demo MachineUnavailableException
            System.out.println("\n[DEMO] Testing MachineUnavailableException...");
            // Assign machine1 dan buat sibuk
            WorkOrder woTest = manager.createWorkOrder("Test Assignment", 50, 1);
            manager.assignWorkOrder(woTest.getWorkOrderId(), machine1.getMachineId(), operator1.getOperatorId());
            
            // Coba assign mesin yang sudah sibuk
            WorkOrder woTest2 = manager.createWorkOrder("Test Assignment 2", 50, 2);
            try {
                manager.assignWorkOrder(woTest2.getWorkOrderId(), machine1.getMachineId(), operator2.getOperatorId());
            } catch (MachineUnavailableException e) {
                System.out.println("  ✓ Caught MachineUnavailableException: " + e.getMessage());
            }
            
            // Cancel test orders
            manager.cancelWorkOrder(woTemp.getWorkOrderId());
            manager.cancelWorkOrder(woTest.getWorkOrderId());
            manager.cancelWorkOrder(woTest2.getWorkOrderId());
            
            // ==========================================
            // Demo Multithreading & Sinkronisasi
            // ==========================================
            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("  PHASE 3: MULTITHREADING & SYNCHRONIZATION DEMO");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
            // Buat beberapa work order
            System.out.println("[PRODUCTION] Creating Work Orders...");
            WorkOrder wo1 = manager.createWorkOrder("Widget Type A", 500, 1);
            WorkOrder wo2 = manager.createWorkOrder("Widget Type B", 800, 2);
            WorkOrder wo3 = manager.createWorkOrder("Widget Type C", 300, 3);
            
            System.out.println("  ✓ Created: " + wo1.getWorkOrderId() + " - " + wo1.getProductName());
            System.out.println("  ✓ Created: " + wo2.getWorkOrderId() + " - " + wo2.getProductName());
            System.out.println("  ✓ Created: " + wo3.getWorkOrderId() + " - " + wo3.getProductName());
            
            // Assign work orders
            System.out.println("\n[ASSIGNMENT] Assigning Work Orders...");
            manager.assignWorkOrder(wo1.getWorkOrderId(), machine1.getMachineId(), operator1.getOperatorId());
            manager.assignWorkOrder(wo2.getWorkOrderId(), machine2.getMachineId(), operator2.getOperatorId());
            manager.assignWorkOrder(wo3.getWorkOrderId(), machine3.getMachineId(), operator3.getOperatorId());
            
            // Start production (multithreading)
            System.out.println("\n[PRODUCTION] Starting Parallel Production...");
            System.out.println("  Each Work Order runs in a separate thread!\n");
            
            manager.startProduction(wo1.getWorkOrderId());
            manager.startProduction(wo2.getWorkOrderId());
            manager.startProduction(wo3.getWorkOrderId());
            
            // Monitor production
            System.out.println("\n[MONITOR] Watching production progress...");
            
            int checkCount = 0;
            while (manager.getActiveThreads().size() > 0) {
                Thread.sleep(10000);
                checkCount++;
                
                System.out.println("\n--- Status Check " + checkCount + " ---");
                List<WorkOrder> activeOrders = manager.getActiveWorkOrders();
                if (activeOrders.isEmpty()) {
                    System.out.println("  No active production");
                } else {
                    for (WorkOrder wo : activeOrders) {
                        System.out.printf("  %s: %s - %.1f%% complete%n",
                            wo.getWorkOrderId(),
                            wo.getProductName(),
                            wo.getProgressPercentage());
                    }
                }
            }
            
            // ==========================================
            // Final Status
            // ==========================================
            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("  PHASE 4: FINAL STATUS");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
            manager.printSystemStatus();
            
            // Print completed work orders details
            System.out.println("\n--- COMPLETED WORK ORDERS DETAILS ---");
            for (WorkOrder wo : manager.getWorkOrdersByStatus(WorkOrderStatus.COMPLETED)) {
                System.out.println("\n" + wo.getWorkOrderId() + ":");
                System.out.println("  Product: " + wo.getProductName());
                System.out.println("  Quantity: " + wo.getCompletedQuantity() + "/" + wo.getQuantity());
                System.out.println("  Duration: " + wo.getProductionDuration());
                System.out.println("  Machine: " + (wo.getAssignedMachine() != null ? wo.getAssignedMachine().getName() : "N/A"));
                System.out.println("  Operator: " + (wo.getAssignedOperator() != null ? wo.getAssignedOperator().getName() : "N/A"));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
