package com.tgy.rtls.docking.config;

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
public class ThreadPoolConfigD {
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
}
