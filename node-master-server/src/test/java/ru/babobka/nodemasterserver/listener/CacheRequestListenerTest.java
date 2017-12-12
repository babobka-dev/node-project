package ru.babobka.nodemasterserver.listener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.babobka.nodebusiness.service.CacheService;
import ru.babobka.nodebusiness.service.CacheServiceImpl;
import ru.babobka.nodemasterserver.model.CacheEntry;
import ru.babobka.nodemasterserver.task.TaskExecutionResult;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeutils.container.ApplicationContainer;
import ru.babobka.nodeutils.container.Container;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Created by 123 on 22.11.2017.
 */
public class CacheRequestListenerTest {

    private CacheRequestListener cacheRequestListener;

    private CacheService<Integer> cacheService;

    @Before
    public void setUp() {
        cacheService = mock(CacheServiceImpl.class);
        new ApplicationContainer() {
            @Override
            public void contain(Container container) {
                container.put(cacheService);
            }
        }.contain(Container.getInstance());
        cacheRequestListener = new CacheRequestListener();
    }

    @After
    public void tearDown() {
        Container.getInstance().clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnRequestNullRequest() {
        cacheRequestListener.onRequest(null);
    }

    @Test
    public void testOnRequestNoCachedData() {
        NodeRequest request = mock(NodeRequest.class);
        when(request.getData()).thenReturn(new HashMap<>());
        when(request.getTaskName()).thenReturn("abc");
        when(cacheService.get(anyInt())).thenReturn(null);
        assertNull(cacheRequestListener.onRequest(request));
    }

    @Test
    public void testOnRequestCollisionCachedData() {
        NodeRequest request = mock(NodeRequest.class);
        when(request.getData()).thenReturn(new HashMap<>());
        when(request.getTaskName()).thenReturn("abc");
        CacheEntry cacheEntry = new CacheEntry("xyz", new HashMap<>(), mock(TaskExecutionResult.class));
        when(cacheService.get(anyInt())).thenReturn(cacheEntry);
        assertNull(cacheRequestListener.onRequest(request));
    }

    @Test
    public void testOnRequestCachedData() {
        String taskName = "abc";
        NodeRequest request = mock(NodeRequest.class);
        when(request.getData()).thenReturn(new HashMap<>());
        when(request.getTaskName()).thenReturn(taskName);
        TaskExecutionResult taskExecutionResult = mock(TaskExecutionResult.class);
        CacheEntry cacheEntry = new CacheEntry(taskName, new HashMap<>(), taskExecutionResult);
        when(cacheService.get(anyInt())).thenReturn(cacheEntry);
        assertEquals(cacheRequestListener.onRequest(request), taskExecutionResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAfterRequestNullRequest() {
        cacheRequestListener.afterRequest(null, mock(TaskExecutionResult.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAfterRequestNullTaskResult() {
        cacheRequestListener.afterRequest(mock(NodeRequest.class), null);
    }

    @Test
    public void testAfterRequestWasStopped() {
        NodeRequest request = mock(NodeRequest.class);
        TaskExecutionResult result = mock(TaskExecutionResult.class);
        when(result.isWasStopped()).thenReturn(true);
        cacheRequestListener.afterRequest(request, result);
        verify(cacheService, never()).put(anyInt(), any(CacheEntry.class));
    }

    @Test
    public void testAfterRequestWasNotStopped() {
        NodeRequest request = mock(NodeRequest.class);
        TaskExecutionResult result = mock(TaskExecutionResult.class);
        when(result.isWasStopped()).thenReturn(false);
        when(request.getTaskName()).thenReturn("abc");
        when(request.getData()).thenReturn(new HashMap<>());
        cacheRequestListener.afterRequest(request, result);
        verify(cacheService).put(anyInt(), any(CacheEntry.class));
    }

}