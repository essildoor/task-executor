package com.paprika.task.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by akapitonov on 20.10.2016.
 */
public class SmartTaskExecutorImpl implements SmartTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(SmartTaskExecutorImpl.class);
    private static final AtomicLong taskIdCounter = new AtomicLong(0L);
    private static final AtomicLong workerIdCounter = new AtomicLong(0L);

    private static final int TASK_EXECUTOR_INITIAL_POOL_SIZE = 4;
    private static final int TASK_EXECUTOR_MAX_POOL_SIZE = 16;
    private static final long KEEP_ALIVE = 10; //sec

    private static final ThreadFactory NAMED_THREAD_FACTORY = r -> {
        Thread thread = new Thread(r);
        thread.setName("WorkerThread-" + workerIdCounter.getAndIncrement());
        return thread;
    };

    private ExecutorService taskExecutor;
    private BlockingQueue<TaskWrapper> initialQueue;
    private Thread scannerThread;

    public void init() {
        initialQueue = new DelayQueue<>();
        taskExecutor = new ThreadPoolExecutor(TASK_EXECUTOR_INITIAL_POOL_SIZE, TASK_EXECUTOR_MAX_POOL_SIZE,
                KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), NAMED_THREAD_FACTORY);

        scannerThread = new Thread(() -> {
            boolean isInterrupted = false;
            while (!isInterrupted) {
                try {
                    TaskWrapper tw = initialQueue.take();
                    taskExecutor.execute(tw.getTask());
                } catch (InterruptedException e) {
                    log.info("interrupted");
                    isInterrupted = true;
                }
            }
        });

        scannerThread.setName("Scanner");
        scannerThread.start();
    }

    public void shutdown() {
        scannerThread.interrupt();
        List<Runnable> rejected = taskExecutor.shutdownNow();
        log.info("shutting down, rejected tasks count: {}", rejected.size());
    }

    @Override
    public <T> Future<T> submit(LocalDateTime dateTime, Callable<T> task) {
        Objects.requireNonNull(dateTime, "dateTime");
        Objects.requireNonNull(task, "task");

        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("schedule dateTime is before current time");
        }

        FutureTask<T> futureTask = new FutureTask<>(task);
        try {
            initialQueue.put(new TaskWrapper(taskIdCounter.getAndIncrement(), dateTime, futureTask));
        } catch (InterruptedException e) {
            log.info("interrupted");
        }
        return futureTask;
    }
}
