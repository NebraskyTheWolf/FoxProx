package com.foxprox.logger.api;

public interface ILogger {
    public void severe(String message);
    public void warning(String message);
    public void info(String message);
    public void config(String message);
    public void fine(String message);
    public void finer(String message);
    public void finest(String message);
}
