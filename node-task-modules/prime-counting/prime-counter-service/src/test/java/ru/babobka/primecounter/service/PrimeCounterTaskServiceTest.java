package ru.babobka.primecounter.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.key.UtilKey;
import ru.babobka.nodeutils.thread.ThreadPoolService;
import ru.babobka.primecounter.model.Range;
import ru.babobka.primecounter.tester.DummyPrimeTester;

import static org.junit.Assert.assertEquals;

/**
 * Created by 123 on 21.10.2017.
 */
public class PrimeCounterTaskServiceTest {

    private PrimeCounterTaskService primeCounterTaskService;

    @Before
    public void setUp() {
        Container.getInstance().put(container -> {
            container.put(
                    UtilKey.SERVICE_THREAD_POOL,
                    ThreadPoolService.createDaemonPool("test",
                            Runtime.getRuntime().availableProcessors()));
            container.put(new DummyPrimeTester());
        });

        primeCounterTaskService = new PrimeCounterTaskService(Runtime.getRuntime().availableProcessors());
    }

    @After
    public void tearDown() {
        Container.getInstance().clear();
        primeCounterTaskService.stop();
    }

    @Test
    public void testExecuteLittleRange() {
        Range range = new Range(0, 1000);
        assertEquals(primeCounterTaskService.execute(range), new Integer(168));
    }

    @Test
    public void testExecuteMediumRange() {
        Range range = new Range(0, 100_000);
        assertEquals(primeCounterTaskService.execute(range), new Integer(9592));
    }

    @Test
    public void testExecuteBigRange() {
        Range range = new Range(0, 1000000);
        assertEquals(primeCounterTaskService.execute(range), new Integer(78498));
    }
}
