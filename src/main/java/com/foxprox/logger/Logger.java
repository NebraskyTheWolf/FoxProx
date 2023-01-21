package com.foxprox.logger;

import com.foxprox.logger.api.ILogger;
import com.foxprox.logger.api.Level;

public class Logger implements ILogger {
    private final String prefix;

    public Logger(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void severe(String message) {
        this.log(Level.SEVERE, message);
    }

    @Override
    public void warning(String message) {
        this.log(Level.WARNING, message);
    }

    @Override
    public void info(String message) {
        this.log(Level.INFO, message);
    }

    @Override
    public void config(String message) {
        this.log(Level.CONFIG, message);
    }

    @Override
    public void fine(String message) {
        this.log(Level.FINE, message);
    }

    @Override
    public void finer(String message) {
        this.log(Level.FINER, message);
    }

    @Override
    public void finest(String message) {
        this.log(Level.FINEST, message);
    }

    public void log(Level level, String message) {
        LoggerFormatter.format(this.prefix, level, message);
    }

    public void log(Level level, String message, Throwable exception) {
        LoggerFormatter.format(this.prefix, level, message + "\n\n Caused By: " + exception.getCause());
    }

    public void log(Level level, String message, Exception exception) {
        LoggerFormatter.format(this.prefix, level, message + "\n\n Caused By: " + exception.getCause());
    }

    public void log(Level level, String message, Object exception) {
        LoggerFormatter.format(this.prefix, level, message + "\n\n Caused By: " + exception);
    }

    public void log(Level level, String message, Object[] exception) {
        LoggerFormatter.format(this.prefix, level, message + "\n\n Caused By: " + exception);
    }
}
