package ru.babobka.nodeslaveserver.server;

import ru.babobka.nodesecurity.auth.AuthResult;
import ru.babobka.nodesecurity.network.SecureNodeConnection;
import ru.babobka.nodeslaveserver.controller.SocketController;
import ru.babobka.nodeslaveserver.exception.SlaveAuthFailException;
import ru.babobka.nodeslaveserver.key.SlaveServerKey;
import ru.babobka.nodeslaveserver.service.SlaveAuthService;
import ru.babobka.nodetask.TaskPool;
import ru.babobka.nodetask.TasksStorage;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.network.NodeConnection;
import ru.babobka.nodeutils.network.NodeConnectionFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SlaveServer extends Thread {

    private final SlaveAuthService authService = Container.getInstance().get(SlaveAuthService.class);
    private final SimpleLogger logger = Container.getInstance().get(SimpleLogger.class);
    private final TaskPool taskPool = Container.getInstance().get(SlaveServerKey.SLAVE_SERVER_TASK_POOL);
    private final NodeConnectionFactory nodeConnectionFactory = Container.getInstance().get(NodeConnectionFactory.class);
    private final NodeConnection connection;
    private final TasksStorage tasksStorage;

    public SlaveServer(Socket socket, String login, String password) throws IOException {
        NodeConnection connection = nodeConnectionFactory.create(socket);
        AuthResult authResult = authService.auth(connection, login, password);
        if (!authResult.isSuccess()) {
            logger.error("auth fail");
            throw new SlaveAuthFailException();
        }
        logger.info("auth success");
        connection.send(taskPool.getTaskNames());
        boolean haveCommonTasks = connection.receive();
        if (!haveCommonTasks) {
            logger.error("no common tasks with master server");
            throw new SlaveAuthFailException();
        }
        tasksStorage = new TasksStorage();
        this.connection = new SecureNodeConnection(connection, authResult.getSecretKey());
    }

    @Override
    public void run() {
        try (SocketController controller = new SocketController(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
                tasksStorage)) {
            while (!isInterrupted() && !connection.isClosed()) {
                controller.control(connection);
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
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
