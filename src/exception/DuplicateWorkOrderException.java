package exception;

/**
 * Exception untuk menangani kasus duplikasi work order
 * Bagian C - Exception Handling
 */
public class DuplicateWorkOrderException extends Exception {
    
    public DuplicateWorkOrderException(String message) {
        super(message);
    }
    
    public DuplicateWorkOrderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DuplicateWorkOrderException(String workOrderId, String productName) {
        super(String.format("Duplicate work order detected: ID=[%s], Product=[%s]", workOrderId, productName));
    }
    
    public DuplicateWorkOrderException(String workOrderId, String existingId, String productName) {
        super(String.format("Work order for product '%s' already exists: ID=[%s], Existing ID=[%s]", 
                productName, workOrderId, existingId));
    }
}
