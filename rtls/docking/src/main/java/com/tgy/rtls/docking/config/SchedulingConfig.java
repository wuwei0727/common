package com.tgy.rtls.docking.config;

import com.tgy.rtls.docking.controller.park.PlaceVideoScheduledTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import javax.annotation.PostConstruct;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private PlaceVideoScheduledTasks scheduledTasks;

    @PostConstruct
    public void scheduleRunnableWithCronTrigger() {
        taskScheduler.schedule(() -> scheduledTasks.executeTask(null),
                new CronTrigger("0 * * * * ?"));
    }
}