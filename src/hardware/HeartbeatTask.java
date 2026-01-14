package hardware;

import java.util.concurrent.atomic.AtomicBoolean;

import com.kuka.roboticsAPI.applicationModel.tasks.ITaskLogger;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;

/**
 * Background task that toggles a Profinet heartbeat signal every 100ms.
 * Used to indicate robot is alive to the PLC (cell master).
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 * Non-blocking - runs in background thread
 */
public class HeartbeatTask implements Runnable {
    
    private ITaskLogger logger;
    private AbstractIOGroup ioGroup;
    private String signalName;
    private int intervalMs;
    
    private AtomicBoolean running;
    private AtomicBoolean currentState;
    private Thread backgroundThread;
    
    /**
     * Create a heartbeat task
     * @param logger Task logger for debug output
     * @param ioGroup I/O group containing the heartbeat signal
     * @param signalName Name of the boolean output signal to toggle
     * @param intervalMs Toggle interval in milliseconds (default: 100ms)
     */
    public HeartbeatTask(ITaskLogger logger, AbstractIOGroup ioGroup, String signalName, int intervalMs) {
        this.logger = logger;
        this.ioGroup = ioGroup;
        this.signalName = signalName;
        this.intervalMs = intervalMs;
        
        this.running = new AtomicBoolean(false);
        this.currentState = new AtomicBoolean(false);
    }
    
    /**
     * Start the heartbeat task in background
     */
    public void start() {
        if (running.get()) {
            logger.warn("HeartbeatTask already running");
            return;
        }
        
        running.set(true);
        
        backgroundThread = new Thread(this);
        backgroundThread.setDaemon(true);
        backgroundThread.setName("HeartbeatTask-" + signalName);
        backgroundThread.start();
        
        logger.info("HeartbeatTask started for signal: " + signalName + " (interval: " + intervalMs + "ms)");
    }
    
    /**
     * Stop the heartbeat task
     */
    public void stop() {
        running.set(false);
        
        if (backgroundThread != null) {
            try {
                backgroundThread.interrupt();
                backgroundThread.join(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        logger.info("HeartbeatTask stopped");
    }
    
    /**
     * Background thread that toggles the heartbeat signal
     */
    public void run() {
        while (running.get()) {
            try {
                // Toggle the state
                boolean newState = !currentState.get();
                currentState.set(newState);
                
                // Write to I/O signal
                ioGroup.setDigitalOutput(signalName, Boolean.valueOf(newState));
                
                // Sleep for the interval
                Thread.sleep(intervalMs);
                
            } catch (InterruptedException e) {
                if (running.get()) {
                    logger.warn("HeartbeatTask interrupted");
                }
                break;
            } catch (Exception e) {
                logger.error("HeartbeatTask error: " + e.getMessage());
                // Continue running even on error
            }
        }
        
        // Set to false on exit
        try {
            ioGroup.setDigitalOutput(signalName, Boolean.valueOf(false));
        } catch (Exception e) {
            logger.error("Failed to reset heartbeat signal: " + e.getMessage());
        }
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public boolean getCurrentState() {
        return currentState.get();
    }
}
