package ru.babobka.nodemasterserver.slave;

import org.junit.Before;
import org.junit.Test;
import ru.babobka.nodeserials.NodeData;
import ru.babobka.nodeutils.container.ApplicationContainer;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.network.NodeConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by 123 on 15.09.2017.
 */
public class SlavesStorageTest {

    private SlavesStorage slavesStorage;

    @Before
    public void setUp() {
        new ApplicationContainer() {
            @Override
            public void contain(Container container) {
                container.put(mock(SimpleLogger.class));
            }
        }.contain(Container.getInstance());
        slavesStorage = new SlavesStorage();
    }

    @Test
    public void testIsEmpty() {
        assertTrue(slavesStorage.isEmpty());
    }

    @Test
    public void testIsEmptyAfterAdd() {
        slavesStorage.add(mock(Slave.class));
        assertFalse(slavesStorage.isEmpty());
    }

    @Test
    public void testRemove() {
        Slave slave = mock(Slave.class);
        slavesStorage.add(slave);
        slavesStorage.remove(slave);
        assertTrue(slavesStorage.isEmpty());
    }

    @Test
    public void testRemoveUnexisting() {
        Slave slave = mock(Slave.class);
        slavesStorage.add(slave);
        slavesStorage.remove(mock(Slave.class));
        assertFalse(slavesStorage.isEmpty());
    }

    @Test
    public void testClear() {
        Slave slave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        slavesStorage.clear();
        assertTrue(slavesStorage.isEmpty());
        verify(slave, times(size)).interrupt();
    }

    @Test
    public void testGetClusterSize() {
        Slave slave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        assertEquals(slavesStorage.getClusterSize(), size);
    }

    @Test
    public void testInterruptAll() {
        Slave slave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        slavesStorage.interruptAll();
        assertFalse(slavesStorage.isEmpty());
        verify(slave, times(size)).interrupt();
    }

    @Test
    public void testGetFullList() {
        Slave slave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        assertEquals(slavesStorage.getFullList().size(), size);
    }

    @Test
    public void testGetCurrentClusterUserList() {
        Slave slave = mock(Slave.class);
        NodeConnection connection = mock(NodeConnection.class);
        when(slave.getConnection()).thenReturn(connection);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        assertEquals(slavesStorage.getCurrentClusterUserList().size(), size);
    }

    @Test
    public void testGetClusterSizeByTaskName() {
        String taskName = "task name 1";
        Slave slave1 = mock(Slave.class);
        when(slave1.taskIsAvailable(taskName)).thenReturn(true);
        Slave slave2 = mock(Slave.class);
        when(slave2.taskIsAvailable(taskName)).thenReturn(false);
        slavesStorage.add(slave1);
        slavesStorage.add(slave2);
        assertEquals(slavesStorage.getClusterSize(taskName), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetListByTaskIdNullTaskId() {
        slavesStorage.getListByTaskId((UUID) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetListByTaskIdNullNodeData() {
        slavesStorage.getListByTaskId((NodeData) null);
    }

    @Test
    public void testGetListByTaskId() {
        UUID taskId = UUID.randomUUID();
        Slave slaveWithTask = mock(Slave.class);
        when(slaveWithTask.hasTask(taskId)).thenReturn(true);
        Slave slaveNoTask = mock(Slave.class);
        List<Slave> slaveList = Arrays.asList(slaveWithTask, slaveWithTask, slaveWithTask);
        for (Slave slave : slaveList) {
            slavesStorage.add(slave);
        }
        slavesStorage.add(slaveNoTask);
        assertEquals(slavesStorage.getListByTaskId(taskId).size(), slaveList.size());
    }

    @Test
    public void testGetListByTaskNameBadMaxSize() {
        assertTrue(slavesStorage.getList("test", 0).isEmpty());
    }

    @Test
    public void testGetListByTaskNameLessMaxSize() {
        String taskName = "abc";
        Slave slave = mock(Slave.class);
        when(slave.taskIsAvailable(taskName)).thenReturn(true);
        Slave badSlave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        slavesStorage.add(badSlave);
        int maxSize = 2;
        assertEquals(slavesStorage.getList(taskName, maxSize).size(), maxSize);
    }

    @Test
    public void testGetListByTaskNameMoreMaxSize() {
        String taskName = "abc";
        Slave slave = mock(Slave.class);
        when(slave.taskIsAvailable(taskName)).thenReturn(true);
        Slave badSlave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        slavesStorage.add(badSlave);
        int maxSize = 5;
        assertEquals(slavesStorage.getList(taskName, maxSize).size(), size);
    }

    @Test
    public void testGetListByTaskNameNoSlaves() {
        String taskName = "abc";
        Slave slave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        int maxSize = 2;
        assertEquals(slavesStorage.getList(taskName, maxSize).size(), 0);
    }

    @Test
    public void testHeartBeatAllSlaves() throws IOException {
        Slave slave = mock(Slave.class);
        int size = 3;
        for (int i = 0; i < size; i++) {
            slavesStorage.add(slave);
        }
        slavesStorage.heartBeatAllSlaves();
        verify(slave, times(size)).sendHeartBeating();
    }

    @Test
    public void testHeartBeatAllSlavesOneException() throws IOException {
        Slave slave1 = mock(Slave.class);
        Slave exceptionSlave = mock(Slave.class);
        doThrow(new IOException()).when(exceptionSlave).sendHeartBeating();
        Slave slave2 = mock(Slave.class);
        slavesStorage.add(slave1);
        slavesStorage.add(exceptionSlave);
        slavesStorage.add(slave2);
        slavesStorage.heartBeatAllSlaves();
        verify(slave1).sendHeartBeating();
        verify(slave2).sendHeartBeating();
    }

}
