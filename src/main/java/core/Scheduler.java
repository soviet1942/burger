package core;

import bean.Request;
import bean.Spider;
import bean.TimeTask;
import downloader.HttpDownloader;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spider.SpiderFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 9:39
 * @Description: 调度器
 */
public class Scheduler {

    private static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private static Queue<Request> QUEUE = new PriorityQueue<>(20, Comparator.comparingInt(Request::getPriority));

    public void inject() {
        for (Spider spider : SpiderFactory.instance().getSpiders()) {
            spider.getStartUrls().forEach(url -> {
                Request request = HttpDownloader.getDefaultRequest(url);
                QUEUE.add(request);
            });

        }
    }

    public static void addRequest(Request request) {
        QUEUE.add(request);
    }



    public static void schedulerJob() throws SchedulerException{
        /*for (Spider spider : SpiderFactory.instance().getSpiders()) {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            JobDetail jobDetail = JobBuilder.newJob(Task.class).withIdentity(spider.getName(), "group1").build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(spider.getName(), "group2")
                    .withSchedule(CronScheduleBuilder.cronSchedule(spider.getCron()))
                    .build();
            org.quartz.Scheduler scheduler = schedulerFactory.getScheduler();
            //将任务及其触发器放入调度器
            scheduler.scheduleJob(jobDetail, trigger);
            //调度器开始调度任务
            scheduler.start();
        }*/
        Properties quartzConfig = new Properties();
        quartzConfig.setProperty("org.quartz.threadPool.threadCount", "1");
        SchedulerFactory schedulerFactory = new StdSchedulerFactory(quartzConfig);
        for (int i=0; i<10; i++) {
            JobDetail jobDetail = JobBuilder.newJob(TimeTask.class).withIdentity("fuck" + i, "group1").build();
            Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity("bitch" + i, "group2")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?"))
                    .build();
            org.quartz.Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger1);
            scheduler.start();
        }
    }

    public static void main(String[] args) throws SchedulerException {
        schedulerJob();
        while (true);
    }



}
