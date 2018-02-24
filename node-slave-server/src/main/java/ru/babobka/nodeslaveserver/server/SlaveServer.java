package ru.babobka.nodeslaveserver.server;

import ru.babobka.nodeslaveserver.controller.SocketController;
import ru.babobka.nodeslaveserver.exception.SlaveAuthFailException;
import ru.babobka.nodeslaveserver.service.AuthService;
import ru.babobka.nodetask.TaskPool;
import ru.babobka.nodetask.TasksStorage;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.network.NodeConnection;
import ru.babobka.nodeutils.util.HashUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SlaveServer extends Thread {

    private final AuthService authService = Container.getInstance().get(AuthService.class);
    private final SimpleLogger logger = Container.getInstance().get(SimpleLogger.class);
    private final TaskPool taskPool = Container.getInstance().get("slaveServerTaskPool");
    private final NodeConnection connection;
    private final TasksStorage tasksStorage;

    public SlaveServer(NodeConnection connection, String login, String password) throws IOException {
        this.connection = connection;
        if (!authService.auth(connection, login, HashUtil.hexSha2(password))) {
            logger.error("auth fail");
            throw new SlaveAuthFailException();
        } else {
            logger.info("auth success");
            connection.send(taskPool.getTaskNames());
            boolean haveCommonTasks = connection.receive();
            if (!haveCommonTasks) {
                logger.error("no common tasks with master server");
                throw new SlaveAuthFailException();
            }
        }
        tasksStorage = new TasksStorage();
    }

    public SlaveServer(String host, int port, String login, String password) throws IOException {
        this(new NodeConnection(new Socket(host, port)), login, password);
    }

    @Override
    public void run() {
        try (SocketController controller = new SocketController(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), tasksStorage)) {
            while (!isInterrupted()) {
                controller.control(connection);
            }
        } catch (IOException e) {
            if (!isInterrupted()) {
                logger.error(e);
            }
            logger.info("exiting slave server");
        } finally {
            clear();
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        clear();
    }


    void clear() {
        tasksStorage.stopAllTheTasks();
        if (connection != null) {
            connection.close();
        }
    }

}
