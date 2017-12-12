package ru.babobka.nodetask.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dolgopolov.a on 29.09.15.
 */
public class ExecutionResult {

    private final boolean stopped;

    private final Map<String, Serializable> resultMap = new HashMap<>();

    public ExecutionResult(boolean stopped, Map<String, Serializable> resultMap) {
        this.stopped = stopped;
        if (resultMap != null)
            this.resultMap.putAll(resultMap);
    }

    public static ExecutionResult ok(Map<String, Serializable> resultMap) {
        return new ExecutionResult(false, resultMap);
    }

    public static ExecutionResult stopped() {
        return new ExecutionResult(true, null);
    }

    public boolean isStopped() {
        return stopped;
    }

    public Map<String, Serializable> getResultMap() {
        return resultMap;
    }

    @Override
    public String toString() {
        return "ExecutionResult [stopped=" + stopped + ", resultMap=" + resultMap + "]";
    }

}