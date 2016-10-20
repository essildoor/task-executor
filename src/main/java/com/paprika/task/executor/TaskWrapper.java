package com.paprika.task.executor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Encapsulates task and auxiliary info
 * <p>
 * Created by akapitonov on 20.10.2016.
 */
final class TaskWrapper implements Delayed {
    private final long id;
    private final LocalDateTime sheduledDt;
    private final FutureTask task;

    TaskWrapper(long id, LocalDateTime sheduledDt, FutureTask task) {
        this.id = id;
        this.sheduledDt = Objects.requireNonNull(sheduledDt, "sheduledDt");
        this.task = Objects.requireNonNull(task, "task");
    }

    public FutureTask getTask() {
        return task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskWrapper that = (TaskWrapper) o;

        if (id != that.id) return false;
        return sheduledDt != null ? sheduledDt.equals(that.sheduledDt) : that.sheduledDt == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (sheduledDt != null ? sheduledDt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaskWrapper{" +
                "id=" + id +
                ", sheduledDt=" + sheduledDt +
                '}';
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TaskWrapper) {
            TaskWrapper that = (TaskWrapper) o;
            if (this.sheduledDt.compareTo(that.sheduledDt) == 0) {
                return (int) (this.id - that.id);
            }
            return this.sheduledDt.compareTo(that.sheduledDt);
        }
        throw new IllegalStateException();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(LocalDateTime.now().until(sheduledDt, ChronoUnit.NANOS), TimeUnit.NANOSECONDS);
    }
}
