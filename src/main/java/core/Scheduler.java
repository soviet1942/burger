package core;

import bean.*;
import controller.Server;
import downloader.HttpDownloader;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spider.SpiderFactory;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 9:39
 * @Description: 调度器
 */
public class Scheduler {

    private static Long FETCH_INTERVAL;
    private static Scheduler INSTANCE;
    private static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private static Map<Pattern, String> PATTERN_SPIDER_MAP = new HashMap<>();
    private static Queue<Request> REQUEST_QUEUE = new PriorityQueue<>(Comparator.comparingInt(Request::getPriority));

    private Scheduler() {
    }

    public static void addRequest(Request request) {
        REQUEST_QUEUE.add(request);
    }

    public static void main(String[] args) throws SchedulerException {
        Scheduler scheduler = Scheduler.instance();
        scheduler.schedulerJob();
        scheduler.start();
        while (true) ;
    }

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

    /**
     * 根据{@link Spider}的@cron注解 ，执行定时任务
     *
     * @throws SchedulerException
     */
    public void schedulerJob() throws SchedulerException {
        Properties quartzConfig = new Properties() {{
            setProperty("org.quartz.threadPool.threadCount", "1");
        }};
        SchedulerFactory schedulerFactory = new StdSchedulerFactory(quartzConfig);
        for (Spider spider : SpiderFactory.instance().getSpiders()) {
            JobDetail jobDetail = JobBuilder.newJob(UrlInjector.class).withIdentity(spider.getName(), "job-detail").build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(spider.getName(), "job-trigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(spider.getCron())).build();
            org.quartz.Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        }
    }

    public void start() {
        Server.getVertx().setPeriodic(FETCH_INTERVAL, e -> {
            Request request = REQUEST_QUEUE.poll();
            Engine.instance().download(request);
        });
    }

    private void init() {
        FETCH_INTERVAL = Optional.ofNullable(Crawler.instance().configs()).map(e -> e.getJSONObject("schedule"))
                .map(e -> e.getLong("fetchInterval")).orElse(1000L);
        SpiderFactory.instance().getSpiderMap().forEach((spiderName, spider) -> {
            List<String> filterUrls;
            if ((filterUrls = spider.getFilterUrls()) != null) {
                filterUrls.forEach(url -> {
                    if (StringUtils.isNotEmpty(url)) {
                        PATTERN_SPIDER_MAP.put(Pattern.compile(url), spider.getName());
                    }
                });
            }
        });
        SpiderFactory.instance().getSpiders().stream()
                .filter(e -> e.getFilterUrls() != null).forEach(
                spider -> spider.getFilterUrls().forEach(url ->
                        PATTERN_SPIDER_MAP.put(Pattern.compile(url), spider.getName())));
    }

    /**
     * 爬虫运行完毕，进行回调
     * @param response
     */
    public void feedback(Response response) {
        Feedback feedback;
        if ((feedback = response.getFeedback()) != null) {
            List<URL> urlList;
            if ((urlList = feedback.getOutlinks()) != null) {
                for (URL url : urlList) {
                    for (Map.Entry<Pattern, String> entry : PATTERN_SPIDER_MAP.entrySet()) {
                        Pattern pattern = entry.getKey();
                        if (pattern.matcher(url.toString()).matches()) {
                            String spiderName = entry.getValue();
                            Request request = HttpDownloader.instance().getDefaultRequest(url.toString(), spiderName);
                            REQUEST_QUEUE.add(request);
                            break;
                        }
                    }
                }
            } else if (StringUtils.isNotEmpty(feedback.getSpiderName())) {
                REQUEST_QUEUE.add(new Request() {{
                    setSpiderName(feedback.getSpiderName());
                }});
            }
        }
    }


}
