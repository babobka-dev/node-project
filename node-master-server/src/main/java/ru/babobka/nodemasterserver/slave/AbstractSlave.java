package ru.babobka.nodemasterserver.slave;

import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodeserials.NodeData;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.enumerations.ResponseStatus;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.func.Applyer;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.network.NodeConnection;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 123 on 20.08.2017.
 */
public abstract class AbstractSlave extends Thread {

    private final UUID slaveId;
    private final MasterServerConfig masterServerConfig = Container.getInstance().get(MasterServerConfig.class);
    private final SimpleLogger logger = Container.getInstance().get(SimpleLogger.class);
    private final Map<UUID, NodeRequest> tasks = new HashMap<>();
    private final NodeConnection connection;

    AbstractSlave(NodeConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null");
        } else if (connection.isClosed()) {
            throw new IllegalArgumentException("Connection is closed");
        }
        this.connection = connection;
        this.slaveId = UUID.randomUUID();
        logger.info("New connection " + connection + " slaveId: " + slaveId);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                connection.setReadTimeOut(masterServerConfig.getRequestTimeOutMillis());
                NodeResponse response = connection.receive();
                if (response.getStatus() != ResponseStatus.HEART_BEAT) {
                    onReceive(response);
                }
            }
        } catch (IOException | RuntimeException e) {
            if (!isInterrupted() && !connection.isClosed()) {
                logger.error(e);
            }
        } finally {
            logger.info("Removing connection " + connection);
            synchronized (AbstractSlave.class) {
                onExit();
            }
            logger.info("Slave " + slaveId + " was disconnected");
        }
    }

    void applyToTasks(Applyer<NodeRequest> applyer) {
        synchronized (tasks) {
            for (Map.Entry<UUID, NodeRequest> requestEntry : tasks.entrySet()) {
                applyer.apply(requestEntry.getValue());
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        connection.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractSlave that = (AbstractSlave) o;

        return slaveId.equals(that.slaveId);
    }

    public boolean isNoTasks() {
        synchronized (tasks) {
            return tasks.isEmpty();
        }
    }

    public Map<UUID, NodeRequest> getTasks() {
        synchronized (tasks) {
            return new HashMap<>(tasks);
        }
    }

    void clearTasks() {
        synchronized (tasks) {
            tasks.clear();
        }
    }

    public void removeTask(NodeData nodeData) {
        if (nodeData == null) {
            throw new IllegalArgumentException("nodeData is null");
        }
        removeTask(nodeData.getId());
    }

    public void removeTask(UUID requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId is null");
        }
        synchronized (tasks) {
            tasks.remove(requestId);
        }
    }

    void addTask(NodeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }
        synchronized (tasks) {
            tasks.put(request.getId(), request);
        }
    }

    void addTasks(List<NodeRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("requests is null");
        }
        synchronized (tasks) {
            for (NodeRequest request : requests) {
                addTask(request);
            }
        }
    }

    boolean hasTask(UUID taskId) {
        synchronized (tasks) {
            for (Map.Entry<UUID, NodeRequest> requestEntry : tasks.entrySet()) {
                if (requestEntry.getValue().getTaskId().equals(taskId)) {
                    return true;
                }
            }
            return false;
        }
    }

    boolean hasRequest(NodeRequest request) {
        synchronized (tasks) {
            return tasks.containsKey(request.getId());
        }
    }


    @Override
    public int hashCode() {
        return slaveId.hashCode();
    }

    @Override
    public String toString() {
        return slaveId.toString();
    }

    public UUID getSlaveId() {
        return slaveId;
    }

    NodeConnection getConnection() {
        return connection;
    }

    protected abstract void onReceive(NodeResponse response);

    protected abstract void onExit();

}