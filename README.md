# Smart Manufacturing Work Order Management System

## PBOL - Ujian Akhir Semester

Sistem manajemen work order untuk perusahaan manufaktur dengan fitur multithreading dan sinkronisasi.

## Bagian A - Object Oriented Analysis and Design (OOAD)

Diagram tersedia di folder `src/diagram/`:

### 1. Use Case Diagram (`UseCaseDiagram.puml`)
Menampilkan aktor dan use case dalam sistem:
- **Aktor**: Production Manager, Operator
- **Use Cases**: Create Work Order, Assign Work Order, Execute Work Order, Monitor Production, dll.

### 2. Class Diagram (`ClassDiagram.puml`)
Menampilkan struktur class dan hubungan antar class:
- **Model Classes**: Machine, Operator, WorkOrder
- **Status Enums**: MachineStatus, OperatorStatus, WorkOrderStatus
- **Manager**: ProductionManager (Singleton)
- **Thread**: ProductionThread
- **Exceptions**: MachineUnavailableException, InvalidQuantityException, DuplicateWorkOrderException

### 3. Sequence Diagram (`SequenceDiagram.puml`)
Menampilkan alur eksekusi untuk Create and Execute Work Order

### 4. Activity Diagram (`ActivityDiagram.puml`)
Menampilkan alur aktivitas sistem secara keseluruhan

## Bagian B - Implementasi OOP

### Model Classes

#### 1. Machine (`model.Machine`)
```java
- Attributes: machineId, name, type, status, capacityPerHour, location, lastMaintenance, nextMaintenance
- Methods: getMachineId(), isAvailable(), setStatus(), performMaintenance(), completeMaintenance()
```

#### 2. Operator (`model.Operator`)
```java
- Attributes: operatorId, name, skillLevel, status, shift, certifications, loginTime
- Methods: getOperatorId(), isAvailable(), setStatus(), addCertification(), login(), logout()
```

#### 3. WorkOrder (`model.WorkOrder`)
```java
- Attributes: workOrderId, productName, quantity, completedQuantity, status, assignedMachine, assignedOperator, createdAt, startedAt, completedAt, priority, notes
- Methods: getWorkOrderId(), updateCompletedQuantity(), getProgressPercentage(), setStatus(), getProductionDuration()
```

#### 4. ProductionManager (`manager.ProductionManager`)
Singleton pattern untuk mengelola seluruh sistem:
- Methods: getInstance(), addMachine(), addOperator(), createWorkOrder(), assignWorkOrder(), startProduction(), getAvailableMachines(), getAvailableOperators(), getAllWorkOrders()

#### 5. ProductionThread (`thread.ProductionThread`)
Thread untuk eksekusi produksi paralel:
- Implements: Runnable (extends Thread)
- Methods: run(), simulateProduction(), completeProduction(), pauseProduction(), resumeProduction()

### Status Enums

#### MachineStatus
- AVAILABLE, BUSY, MAINTENANCE, OFFLINE

#### OperatorStatus
- AVAILABLE, BUSY, OFFLINE

#### WorkOrderStatus
- PENDING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD

## Bagian C - Exception Handling

### Custom Exceptions

#### 1. MachineUnavailableException (`exception.MachineUnavailableException`)
```java
// Thrown when trying to assign a work order to an unavailable machine
try {
    manager.assignWorkOrder(woId, busyMachineId, operatorId);
} catch (MachineUnavailableException e) {
    System.out.println("Machine unavailable: " + e.getMessage());
}
```

#### 2. InvalidQuantityException (`exception.InvalidQuantityException`)
```java
// Thrown when creating work order with invalid quantity
try {
    manager.createWorkOrder("Product", -10, 1);  // Negative
    manager.createWorkOrder("Product", 15000, 1); // Exceeds max
} catch (InvalidQuantityException e) {
    System.out.println("Invalid quantity: " + e.getMessage());
}
```

#### 3. DuplicateWorkOrderException (`exception.DuplicateWorkOrderException`)
```java
// Thrown when creating duplicate work order for same product
try {
    manager.createWorkOrder("Widget A", 100, 1);
    manager.createWorkOrder("Widget A", 50, 2); // Duplicate
} catch (DuplicateWorkOrderException e) {
    System.out.println("Duplicate: " + e.getMessage());
}
```

## Bagian D - Multithreading

Setiap Work Order berjalan di thread terpisah untuk simulasi produksi paralel.

### Key Features:
1. **Parallel Production**: Multiple work orders processed simultaneously
2. **Thread Priority**: Higher priority work orders get higher thread priority
3. **Thread Management**: ProductionManager mengelola active threads

### Implementation:
```java
public class ProductionThread extends Thread {
    private final WorkOrder workOrder;
    private final ProductionManager manager;
    
    @Override
    public void run() {
        // Update status
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        
        // Simulate production
        simulateProduction();
        
        // Complete production
        completeProduction();
    }
}
```

## Bagian E - Thread Synchronization

### Synchronization Mechanisms:

#### 1. synchronized Methods
```java
public synchronized void setStatus(MachineStatus status) {
    this.status = status;
}
```

#### 2. synchronized Blocks
```java
synchronized (machine) {
    if (!machine.isAvailable()) {
        throw new MachineUnavailableException(...);
    }
    
    synchronized (operator) {
        // Critical section - resource allocation
        workOrder.setAssignedMachine(machine);
        workOrder.setAssignedOperator(operator);
        machine.setStatus(MachineStatus.BUSY);
        operator.setStatus(OperatorStatus.BUSY);
    }
}
```

#### 3. ReentrantLock
```java
private final ReentrantLock machineLock = new ReentrantLock(true);

public void addMachine(Machine machine) {
    machineLock.lock();
    try {
        machines.add(machine);
    } finally {
        machineLock.unlock();
    }
}
```

### Resource Management:
- **Machine Lock**: Prevents concurrent modification of machine status
- **Operator Lock**: Prevents concurrent modification of operator status
- **Work Order Lock**: Prevents concurrent modification of work order list

## Cara Menjalankan Program

### Compile:
```bash
javac -d out src/model/*.java src/exception/*.java src/manager/*.java src/thread/*.java src/Main.java
```

### Run:
```bash
cd out
java Main
```

### Atau dengan IntelliJ IDEA:
1. Buka project di IntelliJ IDEA
2. Build > Build Project
3. Run Main.java

## Output Program

Program akan menampilkan:
1. **Phase 1**: System Initialization - Setup machines and operators
2. **Phase 2**: Exception Handling Demo - Demonstrate custom exceptions
3. **Phase 3**: Multithreading Demo - Parallel production with progress tracking
4. **Phase 4**: Final Status - Complete system report

## Struktur Project

```
PBOL_UAS/
├── src/
│   ├── Main.java                           # Main class
│   ├── model/
│   │   ├── Machine.java                    # Machine model class
│   │   ├── Operator.java                   # Operator model class
│   │   ├── WorkOrder.java                  # WorkOrder model class
│   │   ├── MachineStatus.java              # Machine status enum
│   │   ├── OperatorStatus.java             # Operator status enum
│   │   └── WorkOrderStatus.java            # WorkOrder status enum
│   ├── exception/
│   │   ├── MachineUnavailableException.java
│   │   ├── InvalidQuantityException.java
│   │   └── DuplicateWorkOrderException.java
│   ├── manager/
│   │   └── ProductionManager.java            # Singleton manager
│   ├── thread/
│   │   └── ProductionThread.java           # Production thread
│   └── diagram/
│       ├── UseCaseDiagram.puml
│       ├── ClassDiagram.puml
│       ├── SequenceDiagram.puml
│       └── ActivityDiagram.puml
├── docs/
│   └── README.md                           # Dokumentasi
└── out/                                    # Compiled classes
```

## Kesimpulan

Sistem ini mengimplementasikan:
- ✓ **OOAD**: Use Case, Class, Sequence, Activity Diagrams
- ✓ **OOP**: Encapsulation, Inheritance, Polymorphism
- ✓ **Exception Handling**: 3 custom exceptions untuk validasi
- ✓ **Multithreading**: Parallel production simulation
- ✓ **Thread Synchronization**: Prevent resource conflicts dengan synchronized dan locks
