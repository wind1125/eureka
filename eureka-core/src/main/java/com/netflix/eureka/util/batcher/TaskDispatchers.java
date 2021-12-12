package com.netflix.eureka.util.batcher;

/**
 * See {@link TaskDispatcher} for an overview.
 * <p>
 * TODO:学习思路 该类是专门为TaskDispatcher接口 创建实现类的
 *
 * @author Tomasz Bak
 */
public class TaskDispatchers {

    public static <ID, T> TaskDispatcher<ID, T> createNonBatchingTaskDispatcher(String id,
                                                                                int maxBufferSize,
                                                                                int workerCount,
                                                                                long maxBatchingDelay,
                                                                                long congestionRetryDelayMs,
                                                                                long networkFailureRetryMs,
                                                                                TaskProcessor<T> taskProcessor) {
        final AcceptorExecutor<ID, T> acceptorExecutor = new AcceptorExecutor<>(
                id, maxBufferSize, 1, maxBatchingDelay, congestionRetryDelayMs, networkFailureRetryMs
        );
        final TaskExecutors<ID, T> taskExecutor = TaskExecutors.singleItemExecutors(id, workerCount, taskProcessor, acceptorExecutor);
        return new TaskDispatcher<ID, T>() {
            @Override
            public void process(ID id, T task, long expiryTime) {
                acceptorExecutor.process(id, task, expiryTime);
            }

            @Override
            public void shutdown() {
                acceptorExecutor.shutdown();
                taskExecutor.shutdown();
            }
        };
    }

    /**
     * @param id
     * @param maxBufferSize          同步队列最大任务数量，配置读取默认10000
     * @param workloadSize           同步队列 一个批次最多250个请求
     * @param workerCount            同步数据最大线程数，配置读取默认20
     * @param maxBatchingDelay       批处理延迟时间 10ms
     * @param congestionRetryDelayMs 阻塞重试延迟时间 1ms
     * @param networkFailureRetryMs  网络失败重试时间
     * @param taskProcessor          任务处理器 由任务使用端实现，这里是
     * @param <ID>
     * @param <T>
     * @return
     * @see com.netflix.eureka.cluster.ReplicationTaskProcessor
     */
    public static <ID, T> TaskDispatcher<ID, T> createBatchingTaskDispatcher(String id,
                                                                             int maxBufferSize,
                                                                             int workloadSize,
                                                                             int workerCount,
                                                                             long maxBatchingDelay,
                                                                             long congestionRetryDelayMs,
                                                                             long networkFailureRetryMs,
                                                                             TaskProcessor<T> taskProcessor) {
        //接收执行器
        final AcceptorExecutor<ID, T> acceptorExecutor = new AcceptorExecutor<>(
                id, maxBufferSize, workloadSize, maxBatchingDelay, congestionRetryDelayMs, networkFailureRetryMs
        );
        //初始化任务执行器，会创建20个线程来不断轮训队列来消费任务执行
        final TaskExecutors<ID, T> taskExecutor = TaskExecutors.batchExecutors(id, workerCount, taskProcessor, acceptorExecutor);
        //任务分发接口实现类
        return new TaskDispatcher<ID, T>() {
            @Override
            public void process(ID id, T task, long expiryTime) {
                acceptorExecutor.process(id, task, expiryTime);
            }

            @Override
            public void shutdown() {
                acceptorExecutor.shutdown();
                taskExecutor.shutdown();
            }
        };
    }
}
