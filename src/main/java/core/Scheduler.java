package core;

import bean.Crawler;
import bean.Request;
import bean.Spider;
import bean.InjectTask;
import controller.Server;
import downloader.HttpDownloader;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spider.SpiderFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 9:39
 * @Description: 调度器
 */
public class Scheduler {

    private static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private static Queue<Request> REQUEST_QUEUE = new PriorityQueue<>(Comparator.comparingInt(Request::getPriority));
    private static Map<String, List<Pattern>> URL_PATTERN_MAP = new HashMap<>();
    private static Long FETCH_INTERVAL;
    private static Scheduler INSTANCE;

    private Scheduler() {}

    public static Scheduler instance() {
        if (INSTANCE == null) {
            synchronized (Scheduler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Scheduler();
                    INSTANCE.init();
                    return INSTANCE;
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        FETCH_INTERVAL = Optional.ofNullable(Crawler.instance().configs()).map(e -> e.getJSONObject("schedule")).map(e -> e.getLong("fetchInterval")).orElse(1000L);
        SpiderFactory.instance().getSpiders().stream().filter(e -> StringUtils.isNotEmpty(e.getName()))
                .filter(e -> e.getStartUrls() != null && e.getStartUrls().size() > 0).forEach(
                        spider -> URL_PATTERN_MAP.put(spider.getName(), spider.getStartUrls().stream().map(Pattern::compile).collect(Collectors.toList())));
    }

    public static void addRequest(Request request) {
        REQUEST_QUEUE.add(request);
    }

    public void start() {
        Server.getVertx().setPeriodic(FETCH_INTERVAL, e -> {
            Request request = REQUEST_QUEUE.poll();
            String spiderName;
            if (request != null && (spiderName = request.getSpiderName()) != null) {
                String url;
                if (request.getUrl() != null && StringUtils.isNotEmpty(url = request.getUrl().toString())) {
                    for (Pattern pattern : URL_PATTERN_MAP.get(spiderName)) {
                        if (pattern.matcher(url).matches()) {
                            Request newRequest = HttpDownloader.instance().getDefaultRequest(url, spiderName);
                            REQUEST_QUEUE.add(newRequest);
                            break;
                        }
                    }
                }
            }
        });
    }


    /**
     * 根据{@link Spider}的@cron注解 ，执行定时任务
     * @throws SchedulerException
     */
    public void schedulerJob() throws SchedulerException{
        Properties quartzConfig = new Properties() {{ setProperty("org.quartz.threadPool.threadCount", "1"); }};
        SchedulerFactory schedulerFactory = new StdSchedulerFactory(quartzConfig);
        for (Spider spider : SpiderFactory.instance().getSpiders()) {
            JobDetail jobDetail = JobBuilder.newJob(InjectTask.class).withIdentity(spider.getName(), "job-detail").build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(spider.getName(), "job-trigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(spider.getCron())).build();
            org.quartz.Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        }
    }

    public static void main(String[] args) throws SchedulerException {
        Scheduler scheduler = Scheduler.instance();
        scheduler.schedulerJob();
        scheduler.start();
        while (true);
    }



}
