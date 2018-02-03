package ru.babobka.nodeift;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodeslaveserver.exception.SlaveAuthFailException;
import ru.babobka.nodetask.TaskPool;
import ru.babobka.nodetester.master.MasterServerRunner;
import ru.babobka.nodetester.slave.SlaveServerRunner;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by 123 on 07.11.2017.
 */
public class AuthCommonTasksITCase {
    private static MasterServer masterServer;
    private TaskPool taskPool;

    @BeforeClass
    public static void setUp() {
        try {
            Container.getInstance().put(SimpleLogger.debugLogger("AuthCommonTasksITCase", System.getenv("NODE_LOGS"), "AuthCommonTasksITCase"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        MasterServerRunner.init();
        SlaveServerRunner.init();
        masterServer = MasterServerRunner.runMasterServer();
    }

    @AfterClass
    public static void tearDown() {
        masterServer.interrupt();
    }

    @Before
    public void setUpMocks() {
        taskPool = mock(TaskPool.class);
        Container.getInstance().put("slaveServerTaskPool", taskPool);

    }

    @Test(expected = SlaveAuthFailException.class)
    public void testNoTasks() throws IOException {
        when(taskPool.getTaskNames()).thenReturn(new HashSet<>());
        SlaveServerRunner.runSlaveServer("test_user", "test_password");
    }

    @Test(expected = SlaveAuthFailException.class)
    public void testNoCommonTasks() throws IOException {
        Set<String> availableTasks = new HashSet<>();
        availableTasks.add("abc");
        availableTasks.add("xyz");
        when(taskPool.getTaskNames()).thenReturn(availableTasks);
        SlaveServerRunner.runSlaveServer("test_user", "test_password");
    }

    @Test
    public void testOneCommonTask() throws IOException {
        Set<String> availableTasks = new HashSet<>();
        TaskPool masterSlaveTaskPool = Container.getInstance().get("masterServerTaskPool");
        availableTasks.add(masterSlaveTaskPool.getTaskNames().iterator().next());
        when(taskPool.getTaskNames()).thenReturn(availableTasks);
        SlaveServerRunner.runSlaveServer("test_user", "test_password");
    }

}
