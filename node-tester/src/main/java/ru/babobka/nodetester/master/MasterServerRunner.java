package ru.babobka.nodetester.master;

import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodeutils.container.Container;

/**
 * Created by 123 on 06.11.2017.
 */
public class MasterServerRunner {

    public static TesterMasterServerApplicationContainer init() {
        TesterMasterServerApplicationContainer container = new TesterMasterServerApplicationContainer();
        Container.getInstance().put(container);
        return container;
    }

    public static MasterServer runMasterServer() {
        MasterServer masterServer = new MasterServer();
        masterServer.start();
        return masterServer;
    }
}
