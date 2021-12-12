package com.netflix.eureka.util.batcher;

/**
 * 任务的封装 ，增加了一些过滤属性，如过期时间、提交时间等，可用于过滤任务
 *
 * @author Tomasz Bak
 */
class TaskHolder<ID, T> {

    private final ID id;
    private final T task;
    private final long expiryTime;
    private final long submitTimestamp;

    TaskHolder(ID id, T task, long expiryTime) {
        this.id = id;
        this.expiryTime = expiryTime;
        this.task = task;
        this.submitTimestamp = System.currentTimeMillis();
    }

    public ID getId() {
        return id;
    }

    public T getTask() {
        return task;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public long getSubmitTimestamp() {
        return submitTimestamp;
    }
}
