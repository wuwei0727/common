package com.tgy.rtls.docking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@EnableAsync
@Configuration
public class ThreadConfig implements AsyncConfigurer {
    /**
     * 默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，
     * 当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中；
     * 当队列满了，就继续创建线程，当线程数量大于等于maxPoolSize后，开始使用拒绝策略拒绝
     */

    // 核心线程池大小
    @Value("${async.executor.thread.core-pool-size:5}")
    private int corePoolSize;

    // 最大可创建的线程数
    @Value("${async.executor.thread.max-pool-size:12}")
    private int maxPoolSize;

    // 队列最大长度
    @Value("${async.executor.thread.queue-capacity:1024}")
    private int queueCapacity;

    // 线程池维护线程所允许的空闲时间
    @Value("${async.executor.thread.keep-alive-seconds:300}")
    private int keepAliveSeconds;

    // 线程池名前缀
    @Value("${async.executor.thread.threadNamePrefix:vehicleCount-}")
    private String threadNamePrefix;


    @Bean(name = "vehicleCountThreadPool")
    public ThreadPoolTaskExecutor vehicleCountThreadPool()
    {
        // 阿里巴巴推荐使用 ThreadPoolTaskExecutor 而非 Executors 自定义线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);
        // 线程池对拒绝任务(无线程可用)的处理策略
        // CallerRunsPolicy：由调用线程（提交任务的线程）处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }
}