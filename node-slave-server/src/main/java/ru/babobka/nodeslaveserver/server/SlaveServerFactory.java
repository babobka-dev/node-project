package ru.babobka.nodeslaveserver.server;

import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeslaveserver.controller.MasterBackedSocketController;
import ru.babobka.nodeslaveserver.controller.SlaveBackedSocketController;
import ru.babobka.nodeutils.react.PubSub;
import ru.babobka.nodeutils.thread.PrettyNamedThreadPoolFactory;

import java.io.IOException;
import java.net.Socket;

public interface SlaveServerFactory {

    static SlaveServer slaveBacked(Socket socket,
                                   String login,
                                   String password) throws IOException {
        return new SlaveServer(socket,
                login,
                password,
                (connection, tasksStorage) -> new SlaveBackedSocketController(connection,
                        tasksStorage,
                        PrettyNamedThreadPoolFactory.fixedDaemonThreadPool("sb-socket_controller")));
    }

    static SlaveServer masterBacked(Socket socket,
                                    String login,
                                    String password) throws IOException {
        return new SlaveServer(socket,
                login,
                password,
                (connection, tasksStorage) ->
                        new MasterBackedSocketController(connection,
                                PrettyNamedThreadPoolFactory.fixedDaemonThreadPool("mb-socket_controller"),
                                new PubSub<NodeRequest>()));
    }
}
