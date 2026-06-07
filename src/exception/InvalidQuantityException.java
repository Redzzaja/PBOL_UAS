package exception;

/**
 * Exception untuk menangani kasus quantity tidak valid
 * Bagian C - Exception Handling
 */
public class InvalidQuantityException extends Exception {
    
    public InvalidQuantityException(String message) {
        super(message);
    }
    
    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidQuantityException(int quantity, String reason) {
        super(String.format("Invalid quantity '%d': %s", quantity, reason));
    }
    
    public InvalidQuantityException(int quantity, int maxAllowed) {
        super(String.format("Invalid quantity '%d': exceeds maximum allowed (%d)", quantity, maxAllowed));
    }
}
