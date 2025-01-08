package com.tgy.rtls.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.config
 * @Author: wuwei
 * @CreateTime: 2023-05-10 13:47
 * @Description: TODO 监控 线程池。往线程池提交任务前，在日志中打印线程池情况
 * @Version: 1.0
 */
@EnableAsync
@Configuration
public class ThreadPoolConfig {
    @Bean("VipCarBitTimeoutOrOccupyTaskExecutor")
    public Executor mailNumPoolSingleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置为1，任务顺序执行
        executor.setCorePoolSize(10);//核心线程数：线程池创建时候初始化的线程数
        executor.setMaxPoolSize(100);//最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setQueueCapacity(20);//缓冲队列200：缓冲执行任务的队列
        executor.setKeepAliveSeconds(60);//允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setThreadNamePrefix("VipCarBitTimeoutOrOccupyTaskExecutor-");//线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());// 线程池对拒绝任务的处理策略
        return executor;
    }

    @Bean("otherSingleTaskExecutor")
    public Executor otherSingleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置为1，任务顺序执行
        executor.setCorePoolSize(1);//核心线程数：线程池创建时候初始化的线程数
        executor.setMaxPoolSize(1);//最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setQueueCapacity(20);//缓冲队列200：缓冲执行任务的队列
        executor.setKeepAliveSeconds(60);//允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setThreadNamePrefix("otherSingleTaskExecutor-");//线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());// 线程池对拒绝任务的处理策略
        return executor;
    }

    @Bean("SubAsyncExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor ();
        // 核心线程数：线程池创建的时候初始化的线程数
        executor.setCorePoolSize(50);
        // 最大线程数：线程池最大的线程数，只有缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(200);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(1000);
        // 线程池关闭：等待所有任务都完成再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间：等待5秒后强制停止
        executor.setAwaitTerminationSeconds(5);
        // 允许空闲时间：超过核心线程之外的线程到达60秒后会被销毁
        executor.setKeepAliveSeconds(60);
        // 线程名称前缀
        executor.setThreadNamePrefix("learn-Async-");
        // 初始化线程
        executor.initialize();
        return executor;
    }

    @Bean("cameraAsyncExecutor")
    public Executor cameraAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor ();
        // 核心线程数：线程池创建的时候初始化的线程数
        executor.setCorePoolSize(10);
        // 最大线程数：线程池最大的线程数，只有缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(20);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(200);
        // 线程池关闭：等待所有任务都完成再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间：等待5秒后强制停止
        executor.setAwaitTerminationSeconds(5);
        // 线程名称前缀
        executor.setThreadNamePrefix("camera-Async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());// 线程池对拒绝任务的处理策略

        // 初始化线程
        executor.initialize();
        return executor;
    }
}
