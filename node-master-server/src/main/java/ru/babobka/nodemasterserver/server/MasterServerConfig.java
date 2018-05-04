package ru.babobka.nodemasterserver.server;

import ru.babobka.nodeutils.math.SafePrime;
import ru.babobka.nodeutils.util.TextUtil;

import java.io.Serializable;

public class MasterServerConfig implements Serializable {

    private static final long serialVersionUID = 156081573106293600L;
    private int authTimeOutMillis;
    private int slaveListenerPort;
    private int clientListenerPort;
    private int requestTimeOutMillis;
    private int heartBeatTimeOutMillis;
    private int webListenerPort;
    private String loggerFolder;
    private String loggerFolderEnv;
    private String tasksFolder;
    private String tasksFolderEnv;
    private boolean debugMode;
    private boolean localOnly;
    private boolean enableCache;
    private SafePrime bigSafePrime;

    public int getAuthTimeOutMillis() {
        return authTimeOutMillis;
    }

    public void setAuthTimeOutMillis(int authTimeOutMillis) {
        this.authTimeOutMillis = authTimeOutMillis;
    }

    public int getSlaveListenerPort() {
        return slaveListenerPort;
    }

    public void setSlaveListenerPort(int slaveListenerPort) {
        this.slaveListenerPort = slaveListenerPort;
    }

    public int getRequestTimeOutMillis() {
        return requestTimeOutMillis;
    }

    public void setRequestTimeOutMillis(int requestTimeOutMillis) {
        this.requestTimeOutMillis = requestTimeOutMillis;
    }

    public int getHeartBeatTimeOutMillis() {
        return heartBeatTimeOutMillis;
    }

    public void setHeartBeatTimeOutMillis(int heartBeatTimeOutMillis) {
        this.heartBeatTimeOutMillis = heartBeatTimeOutMillis;
    }

    public int getWebListenerPort() {
        return webListenerPort;
    }

    public void setWebListenerPort(int webListenerPort) {
        this.webListenerPort = webListenerPort;
    }

    public String getLoggerFolder() {
        return TextUtil.getFirstNonNull(loggerFolder, TextUtil.getEnv(loggerFolderEnv));
    }

    public void setLoggerFolder(String loggerFolder) {
        this.loggerFolder = loggerFolder;
    }

    public String getTasksFolder() {
        return TextUtil.getFirstNonNull(tasksFolder, TextUtil.getEnv(tasksFolderEnv));
    }

    public void setTasksFolder(String tasksFolder) {
        this.tasksFolder = tasksFolder;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getClientListenerPort() {
        return clientListenerPort;
    }

    public void setClientListenerPort(int clientListenerPort) {
        this.clientListenerPort = clientListenerPort;
    }

    public String getLoggerFolderEnv() {
        return loggerFolderEnv;
    }

    public void setLoggerFolderEnv(String loggerFolderEnv) {
        this.loggerFolderEnv = loggerFolderEnv;
    }

    public String getTasksFolderEnv() {
        return tasksFolderEnv;
    }

    public void setTasksFolderEnv(String tasksFolderEnv) {
        this.tasksFolderEnv = tasksFolderEnv;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public SafePrime getBigSafePrime() {
        return bigSafePrime;
    }

    public void setBigSafePrime(SafePrime bigSafePrime) {
        this.bigSafePrime = bigSafePrime;
    }

    @Override
    public String toString() {
        return "MasterServerConfig{" +
                "authTimeOutMillis=" + authTimeOutMillis +
                ", slaveListenerPort=" + slaveListenerPort +
                ", clientListenerPort=" + clientListenerPort +
                ", requestTimeOutMillis=" + requestTimeOutMillis +
                ", heartBeatTimeOutMillis=" + heartBeatTimeOutMillis +
                ", webListenerPort=" + webListenerPort +
                ", loggerFolder='" + loggerFolder + '\'' +
                ", loggerFolderEnv='" + loggerFolderEnv + '\'' +
                ", tasksFolder='" + tasksFolder + '\'' +
                ", tasksFolderEnv='" + tasksFolderEnv + '\'' +
                ", debugMode=" + debugMode +
                ", localOnly=" + localOnly +
                ", enableCache=" + enableCache +
                ", bigSafePrime=" + bigSafePrime +
                '}';
    }
}
