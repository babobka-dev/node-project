package ru.babobka.nodemasterserver.thread;

import ru.babobka.nodemasterserver.client.ClientStorage;
import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodemasterserver.slave.SlavesStorage;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.thread.CyclicThread;

public class HeartBeatingThread extends CyclicThread {

    public HeartBeatingThread() {
        setDaemon(true);
    }

    private final MasterServerConfig masterServerConfig = Container.getInstance().get(MasterServerConfig.class);
    private final SlavesStorage slavesStorage = Container.getInstance().get(SlavesStorage.class);
    private final ClientStorage clientStorage = Container.getInstance().get(ClientStorage.class);
    private final SimpleLogger logger = Container.getInstance().get(SimpleLogger.class);

    @Override
    public int sleepMillis() {
        return masterServerConfig.getHeartBeatTimeOutMillis();
    }

    @Override
    public void onCycle() {
        logger.debug("heart beating time");
        clientStorage.heartBeatAllClients();
        slavesStorage.heartBeatAllSlaves();
    }

}
