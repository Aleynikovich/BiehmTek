package communication;

/**
 * Simple logger interface for communication components.
 * Allows decoupling from KUKA API logger.
 * 
 * Java 1.7 compatible
 */
public interface ILogger {
    void info(String message);
    void warn(String message);
    void error(String message);
}
