package ru.babobka.nodemasterserver.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.babobka.nodebusiness.dao.CacheDAO;
import ru.babobka.nodebusiness.service.NodeUsersService;
import ru.babobka.nodemasterserver.client.IncomingClientListenerThread;
import ru.babobka.nodemasterserver.server.config.MasterServerConfig;
import ru.babobka.nodemasterserver.server.config.ModeConfig;
import ru.babobka.nodemasterserver.slave.IncomingSlaveListenerThread;
import ru.babobka.nodemasterserver.slave.SlavesStorage;
import ru.babobka.nodemasterserver.thread.HeartBeatingThread;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.vsjws.webserver.WebServer;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by 123 on 19.09.2017.
 */
public class MasterServerTest {

    private MasterServer masterServer;
    private Thread incomingClientsThread;
    private Thread heartBeatingThread;
    private Thread slaveListenerThread;
    private WebServer webServer;
    private SlavesStorage slavesStorage;
    private MasterServerConfig masterServerConfig;
    private NodeUsersService nodeUsersService;
    private SimpleLogger logger;
    private CacheDAO cacheDAO;

    @Before
    public void setUp() {
        nodeUsersService = mock(NodeUsersService.class);
        masterServerConfig = mock(MasterServerConfig.class);
        heartBeatingThread = mock(HeartBeatingThread.class);
        slaveListenerThread = mock(IncomingSlaveListenerThread.class);
        cacheDAO = mock(CacheDAO.class);
        webServer = mock(WebServer.class);
        incomingClientsThread = mock(IncomingClientListenerThread.class);
        slavesStorage = mock(SlavesStorage.class);
        logger = mock(SimpleLogger.class);
        Container.getInstance().put(container -> {
            container.put(nodeUsersService);
            container.put(heartBeatingThread);
            container.put(incomingClientsThread);
            container.put(slaveListenerThread);
            container.put(masterServerConfig);
            container.put(webServer);
            container.put(logger);
            container.put(slavesStorage);
            container.put(cacheDAO);
        });
        masterServer = spy(new MasterServer());
    }

    @After
    public void tearDown() {
        Container.getInstance().clear();
    }

    @Test
    public void testRun() {
        ModeConfig modeConfig = new ModeConfig();
        when(masterServerConfig.getModes()).thenReturn(modeConfig);
        masterServer.run();
        verify(slaveListenerThread).start();
        verify(heartBeatingThread).start();
        verify(webServer).start();
    }

    @Test
    public void testRunException() throws IOException {
        doThrow(new RuntimeException()).when(slaveListenerThread).start();
        masterServer.run();
        verify(logger).error(any(RuntimeException.class));
        verify(masterServer).clear();
    }


    @Test
    public void testClear() throws IOException {
        masterServer.clear();
        verify(cacheDAO).close();
    }

    @Test
    public void testInterrupt() {
        masterServer.interrupt();
        verify(masterServer).clear();
    }

}
