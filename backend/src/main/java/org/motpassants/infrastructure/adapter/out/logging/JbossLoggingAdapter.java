package org.motpassants.infrastructure.adapter.out.logging;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.motpassants.domain.port.out.LoggingPort;

/**
 * Infrastructure adapter using JBoss Logging as the concrete implementation.
 */
@ApplicationScoped
public class JbossLoggingAdapter implements LoggingPort {

    private final Logger logger = Logger.getLogger("org.motpassants");

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void infof(String format, Object... args) {
        logger.infof(format, args);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message, Throwable t) {
        logger.error(message, t);
    }
}
