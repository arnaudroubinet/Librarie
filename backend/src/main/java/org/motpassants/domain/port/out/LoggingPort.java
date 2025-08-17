package org.motpassants.domain.port.out;

/**
 * Outbound logging port to keep application/domain layers independent
 * from concrete logging frameworks. Implemented in infrastructure.
 */
public interface LoggingPort {
    void debug(String message);
    void info(String message);
    void infof(String format, Object... args);
    void warn(String message);
    void error(String message, Throwable t);
}
