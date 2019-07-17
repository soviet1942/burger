package bean;

import controller.Server;
import core.Scheduler;
import downloader.HttpDownloader;
import io.vertx.core.Future;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spider.SpiderFactory;

import java.util.List;

/**
 * spider定时任务
 */
public class InjectTask implements Job {

    private static Logger LOGGER = LoggerFactory.getLogger(InjectTask.class);

    @Override
    public void execute(JobExecutionContext context) {
        String spiderName = context.getTrigger().getJobKey().getName();
        Server.getVertx().executeBlocking(future -> {
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
            future.complete();
        }, res -> {
            if (res.succeeded()) {
                LOGGER.info("success start: " + spiderName);
            } else {
                LOGGER.error(res.cause().getMessage());
            }
        });
    }


}
