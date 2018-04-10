package ru.babobka.nodeutils.logger;

import ru.babobka.nodeutils.util.TextUtil;

import java.io.IOException;
import java.util.logging.Logger;

public class SimpleLogger {

    private final Logger logger;
    private final boolean debugMode;

    SimpleLogger(Logger logger) {
        this.logger = logger;
        this.debugMode = false;
    }

    public SimpleLogger(String loggerName, String runningFolder, boolean debugMode, boolean writeConsole) throws IOException {
        if (writeConsole) {
            logger = LogBuilder.buildRegular(loggerName, runningFolder, debugMode);
        } else {
            logger = LogBuilder.buildNoConsole(loggerName, runningFolder, debugMode);
        }
        this.debugMode = debugMode;
    }

    public static SimpleLogger defaultLogger(String loggerName, String runningFolder) throws IOException {
        return new SimpleLogger(loggerName, runningFolder, false, true);
    }

    public static SimpleLogger debugLogger(String loggerName, String runningFolder) throws IOException {
        return new SimpleLogger(loggerName, runningFolder, true, true);
    }

    public static SimpleLogger silentLogger(String loggerName, String runningFolder) throws IOException {
        return new SimpleLogger(loggerName, runningFolder, false, false);
    }

    public void info(Object o) {
        info(o.toString());
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warning(String message) {
        logger.warning(message);
    }

    public void warning(Exception e) {
        logger.warning(TextUtil.getStringFromException(e));
    }

    public void warning(String message, Exception e) {
        logger.warning(message + "\t" + TextUtil.getStringFromException(e));
    }

    public void debug(String message) {
        if (debugMode) {
            info(message);
        }
    }

    public void error(String message) {
        logger.severe(message);
    }

    public void error(Exception e) {
        logger.severe(TextUtil.getStringFromException(e));
    }

    public void error(String message, Exception e) {
        logger.severe(message + "\t" + TextUtil.getStringFromException(e));
    }

}
