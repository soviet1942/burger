package core;

import bean.Request;
import bean.Spider;
import downloader.HttpDownloader;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spider.SpiderFactory;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
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
        for (Spider spider : SpiderFactory.instance().getSpiders()) {
            List<Request> requests = spider.getStartUrls().stream().map(HttpDownloader::getDefaultRequest).collect(Collectors.toList());
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
        }

    }

    public static void main(String[] args) throws SchedulerException {

    }

    static class Task implements Job {


        public void execute(JobExecutionContext context) {
            String spiderName = context.getTrigger().getJobKey().getName();

        }

    }


}
