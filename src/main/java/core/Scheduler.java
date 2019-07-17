package core;

import bean.Request;
import bean.Spider;
import bean.InjectTask;
import controller.Server;
import downloader.HttpDownloader;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spider.SpiderFactory;

import java.util.*;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 9:39
 * @Description: 调度器
 */
public class Scheduler {

    private static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private static Queue<Request> QUEUE = new PriorityQueue<>(Comparator.comparingInt(Request::getPriority));

    public void start() {
        Server.getVertx().setPeriodic(10, e -> {
            Request request = QUEUE.poll();
            if (request != null) {
                System.out.println(request.getUrl());
            }
        });
    }

    public static void addRequest(Request request) {
        QUEUE.add(request);
    }


    /**
     * 根据{@link Spider}的@cron注解 ，执行定时任务
     * @throws SchedulerException
     */
    public static void schedulerJob() throws SchedulerException{
        Properties quartzConfig = new Properties();
        quartzConfig.setProperty("org.quartz.threadPool.threadCount", "1");
        SchedulerFactory schedulerFactory = new StdSchedulerFactory(quartzConfig);
        for (Spider spider : SpiderFactory.instance().getSpiders()) {
            JobDetail jobDetail = JobBuilder.newJob(InjectTask.class).withIdentity(spider.getName(), "group1").build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(spider.getName(), "group2")
                    .withSchedule(CronScheduleBuilder.cronSchedule(spider.getCron()))
                    .build();
            org.quartz.Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        }
    }

    public static void main(String[] args) throws SchedulerException {
        schedulerJob();
        while (true);
    }



}
