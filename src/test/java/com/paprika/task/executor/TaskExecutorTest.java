package com.paprika.task.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by akapitonov on 20.10.2016.
 */
@Test(singleThreaded = true)
public class TaskExecutorTest {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutorTest.class);

    private SmartTaskExecutor taskExecutor;

    @BeforeMethod
    public void setUp() throws Exception {
        taskExecutor = new SmartTaskExecutorImpl();
        taskExecutor.init();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        taskExecutor.shutdown();
    }

    @Test
    public void testDelayedExecution() throws Exception {
        Future<String> future = taskExecutor.submit(LocalDateTime.now().plusSeconds(1), () -> {
            log.info("executed");
            return "result";
        });

        assertTrue(!future.isDone());

        Thread.sleep(1500);

        assertTrue(future.isDone());
        assertEquals(future.get(), "result");
    }

    @Test
    public void testOrder() throws Exception {
        final AtomicInteger counter = new AtomicInteger(1);

        Future[] ft = new Future[5];

        Thread t1 = new Thread(() -> {
            ft[0] = taskExecutor.submit(LocalDateTime.now().plus(50, ChronoUnit.MILLIS), () -> counter.getAndIncrement());
            ft[1] = taskExecutor.submit(LocalDateTime.now().plus(10, ChronoUnit.MILLIS), () -> counter.getAndIncrement());
            ft[2] = taskExecutor.submit(LocalDateTime.now().plus(40, ChronoUnit.MILLIS), () -> counter.getAndIncrement());
        });

        Thread t2 = new Thread(() -> {
            ft[3] = taskExecutor.submit(LocalDateTime.now().plus(20, ChronoUnit.MILLIS), () -> counter.getAndIncrement());
            ft[4] = taskExecutor.submit(LocalDateTime.now().plus(30, ChronoUnit.MILLIS), () -> counter.getAndIncrement());
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertEquals(ft[0].get(), Integer.valueOf(5));
        assertEquals(ft[1].get(), Integer.valueOf(1));
        assertEquals(ft[2].get(), Integer.valueOf(4));
        assertEquals(ft[3].get(), Integer.valueOf(2));
        assertEquals(ft[4].get(), Integer.valueOf(3));
    }
}
