package com.paprika.task.executor;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Receives and schedules {@link Callable} tasks for execution
 * <p>
 * Created by akapitonov on 20.10.2016.
 */
public interface SmartTaskExecutor {

    /**
     * Registers task for execution
     *
     * @param dateTime task execution date time, time units lesser that second is ignored
     * @param task     task
     * @param <T>      task return type
     * @return {@link Future} on result of the task execution
     */
    <T> Future<T> submit(LocalDateTime dateTime, Callable<T> task);

    void init();

    void shutdown();
}
