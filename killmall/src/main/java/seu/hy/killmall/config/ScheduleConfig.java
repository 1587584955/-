package seu.hy.killmall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//配置定时任务由多线程执行
@Configuration
public class ScheduleConfig implements SchedulingConfigurer{

    @Override
    public void configureTasks(ScheduledTaskRegistrar staskRegistrar) {
        //配置定时任务的线程池
        staskRegistrar.setScheduler(Executors.newScheduledThreadPool(5));
    }
}
