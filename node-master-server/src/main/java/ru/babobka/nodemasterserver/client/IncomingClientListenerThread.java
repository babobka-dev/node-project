package ru.babobka.nodemasterserver.client;

import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.network.NodeConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

/**
 * Created by 123 on 28.10.2017.
 */
public class IncomingClientListenerThread extends Thread {

    private final SimpleLogger logger = Container.getInstance().get(SimpleLogger.class);
    private final ExecutorService executorService = Container.getInstance().get("clientsThreadPool");
    private final ServerSocket serverSocket;

    public IncomingClientListenerThread(ServerSocket serverSocket) {
        if (serverSocket == null) {
            throw new IllegalArgumentException("serverSocket is null");
        } else if (serverSocket.isClosed()) {
            throw new IllegalArgumentException("serverSocket is closed");
        }
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            while (!isDone(serverSocket)) {
                processConnection(serverSocket);
            }
        } finally {
            onExit();
            logger.debug("IncomingClientListenerThread is done");
        }
    }

    void onExit() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error(e);
        }
        executorService.shutdownNow();
    }

    @Override
    public void interrupt() {
        onExit();
        super.interrupt();
    }

    void processConnection(ServerSocket serverSocket) {
        if (serverSocket == null) {
            throw new IllegalArgumentException("serverSocket is null");
        } else if (serverSocket.isClosed()) {
            throw new IllegalStateException("serverSocket is closed");
        }
        try {
            NodeConnection nodeConnection = createNodeConnection(serverSocket);
            NodeRequest request = nodeConnection.receive();
            handleRequest(nodeConnection, request);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    void handleRequest(NodeConnection nodeConnection, NodeRequest request) {
        switch (request.getRequestStatus()) {
            case NORMAL:
            case RACE: {
                try {
                    executorService.submit(createClientExecutor(nodeConnection, request));
                } catch (RuntimeException e) {
                    logger.error("Error while handling request " + request, e);
                }
                return;
            }
            default: {
                logger.warning("Can not handle request " + request);
            }
        }
    }

    Client createClientExecutor(NodeConnection connection, NodeRequest request) {
        return new Client(connection, request);
    }

    boolean isDone(ServerSocket serverSocket) {
        if (serverSocket == null) {
            throw new IllegalArgumentException("serverSocket is null");
        }
        return serverSocket.isClosed() || Thread.currentThread().isInterrupted();
    }

    NodeConnection createNodeConnection(ServerSocket serverSocket) throws IOException {
        return new NodeConnection(serverSocket.accept());
    }
}