package exception;

/**
 * Exception untuk menangani kasus mesin tidak tersedia
 * Bagian C - Exception Handling
 */
public class MachineUnavailableException extends Exception {
    
    public MachineUnavailableException(String message) {
        super(message);
    }
    
    public MachineUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MachineUnavailableException(String machineId, String machineName, String reason) {
        super(String.format("Machine [%s] '%s' is unavailable: %s", machineId, machineName, reason));
    }
}
