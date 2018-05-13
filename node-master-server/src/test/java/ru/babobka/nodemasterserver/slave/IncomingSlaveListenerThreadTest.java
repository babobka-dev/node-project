package ru.babobka.nodemasterserver.slave;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.babobka.nodemasterserver.key.MasterServerKey;
import ru.babobka.nodemasterserver.server.config.MasterServerConfig;
import ru.babobka.nodemasterserver.server.config.TimeoutConfig;
import ru.babobka.nodemasterserver.service.MasterAuthService;
import ru.babobka.nodesecurity.auth.AuthResult;
import ru.babobka.nodesecurity.data.SecureDataFactory;
import ru.babobka.nodesecurity.network.SecureNodeConnection;
import ru.babobka.nodesecurity.service.SecurityService;
import ru.babobka.nodetask.TaskPool;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.network.NodeConnection;
import ru.babobka.nodeutils.network.NodeConnectionFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Created by 123 on 19.09.2017.
 */
public class IncomingSlaveListenerThreadTest {

    private IncomingSlaveListenerThread incomingSlaveListenerThread;
    private NodeConnectionFactory nodeConnectionFactory;
    private SlaveFactory slaveFactory;
    private SimpleLogger logger;
    private SlavesStorage slavesStorage;
    private MasterAuthService authService;
    private ServerSocket serverSocket;
    private TaskPool taskPool;
    private MasterServerConfig masterServerConfig;

    @Before
    public void setUp() {
        masterServerConfig = mock(MasterServerConfig.class);
        nodeConnectionFactory = mock(NodeConnectionFactory.class);
        slaveFactory = mock(SlaveFactory.class);
        logger = mock(SimpleLogger.class);
        slavesStorage = mock(SlavesStorage.class);
        authService = mock(MasterAuthService.class);
        serverSocket = mock(ServerSocket.class);
        taskPool = mock(TaskPool.class);
        Container.getInstance().put(container -> {
            container.put(nodeConnectionFactory);
            container.put(slaveFactory);
            container.put(masterServerConfig);
            container.put(logger);
            container.put(slavesStorage);
            container.put(authService);
            container.put(MasterServerKey.MASTER_SERVER_TASK_POOL, taskPool);
            container.put(mock(SecurityService.class));
            container.put(mock(SecureDataFactory.class));
        });

        incomingSlaveListenerThread = new IncomingSlaveListenerThread(serverSocket);
    }

    @After
    public void tearDown() {
        Container.getInstance().clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullServerSocket() {
        new IncomingSlaveListenerThread(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorClosedServerSocket() {
        when(serverSocket.isClosed()).thenReturn(true);
        new IncomingSlaveListenerThread(serverSocket);
    }

    @Test
    public void testOnExit() throws IOException {
        incomingSlaveListenerThread.onExit();
        verify(serverSocket).close();
    }

    @Test
    public void testOnExitException() throws IOException {
        doThrow(new IOException()).when(serverSocket).close();
        incomingSlaveListenerThread.onExit();
        verify(logger).error(any(Exception.class));
    }

    @Test
    public void testOnAwake() throws IOException {
        Socket socket = mock(Socket.class);
        TimeoutConfig timeoutConfig = new TimeoutConfig();
        when(masterServerConfig.getTimeouts()).thenReturn(timeoutConfig);
        when(serverSocket.accept()).thenReturn(socket);
        NodeConnection connection = mock(NodeConnection.class);
        when(nodeConnectionFactory.create(socket)).thenReturn(connection);
        when(authService.auth(connection)).thenReturn(AuthResult.success(new byte[]{0}));
        Slave slave = mock(Slave.class);
        Set<String> availableTasks = new HashSet<>();
        when(connection.receive()).thenReturn(availableTasks);
        when(slaveFactory.create(eq(availableTasks), any(SecureNodeConnection.class))).thenReturn(slave);
        when(taskPool.containsAnyOfTask(any(Set.class))).thenReturn(true);
        incomingSlaveListenerThread.onCycle();
        verify(slavesStorage).add(slave);
        verify(slave).start();
    }

    @Test
    public void testOnAwakeAuthFail() throws IOException {
        Socket socket = mock(Socket.class);
        TimeoutConfig timeoutConfig = new TimeoutConfig();
        when(masterServerConfig.getTimeouts()).thenReturn(timeoutConfig);
        when(serverSocket.accept()).thenReturn(socket);
        NodeConnection connection = mock(NodeConnection.class);
        when(nodeConnectionFactory.create(socket)).thenReturn(connection);
        when(authService.auth(connection)).thenReturn(AuthResult.fail());
        incomingSlaveListenerThread.onCycle();
        verify(connection).close();
    }

    @Test
    public void testOnAwakeIOException() throws IOException {
        when(serverSocket.isClosed()).thenReturn(true);
        when(serverSocket.accept()).thenThrow(new IOException());
        incomingSlaveListenerThread.onCycle();
        verify(logger).error(any(IOException.class));
    }

    @Test
    public void testInterrupt() throws IOException {
        incomingSlaveListenerThread.interrupt();
        verify(serverSocket).close();
    }

}
