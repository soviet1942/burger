package core;

import bean.Request;
import bean.Spider;
import bean.InjectTask;
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

        Properties quartzConfig = new Properties();
        quartzConfig.setProperty("org.quartz.threadPool.threadCount", "1");
        SchedulerFactory schedulerFactory = new StdSchedulerFactory(quartzConfig);
        for (int i=1; i<=10; i++) {
            JobDetail jobDetail = JobBuilder.newJob(InjectTask.class).withIdentity("fuck" + i, "group1").build();
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
