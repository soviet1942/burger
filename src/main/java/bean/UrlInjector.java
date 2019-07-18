package bean;

import controller.Server;
import core.Scheduler;
import downloader.HttpDownloader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spider.SpiderFactory;

import java.util.List;

/**
 * spider定时任务
 */
public class UrlInjector implements Job {

    private static Logger LOGGER = LoggerFactory.getLogger(UrlInjector.class);

    @Override
    public void execute(JobExecutionContext context) {
        String spiderName = context.getTrigger().getJobKey().getName();
        Spider spider = SpiderFactory.instance().getSpiders().stream().filter(e -> spiderName.equals(e.getName())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("cannot find spider named [" + spiderName + "]"));
        List<String> startUrls = spider.getStartUrls();
        if (startUrls.size() == 0) {
            Scheduler.addRequest(new Request() {{
                setSpiderName(spiderName);
            }});
        }
        else {
            startUrls.forEach(url -> {
                Request request = HttpDownloader.instance().getDefaultRequest(url, spiderName);
                Scheduler.addRequest(request);
            });
        }

    }

    @Test
    public void test() {
        for (int i=0; i<1000; i++) {
            Server.getVertx().executeBlocking(future -> {
                future.complete();
            }, res -> {
                if (res.succeeded()) {
                    LOGGER.info("success start: ");
                } else {
                    LOGGER.error(ExceptionUtils.getStackTrace(res.cause()));
                }
            });
        }
        while (true);
    }
}
