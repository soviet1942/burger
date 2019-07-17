package bean;

import controller.Server;
import core.Scheduler;
import downloader.HttpDownloader;
import io.vertx.core.Future;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import spider.SpiderFactory;

import java.util.List;

public class InjectTask implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        String spiderName = context.getTrigger().getJobKey().getName();
        executeInjectUrl(spiderName);
    }

    /**
     * inject spider's startUrl
     * @param spiderName
     */
    private void executeInjectUrl(String spiderName) {
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
                    Request request = HttpDownloader.getDefaultRequest(url, spiderName);
                    Scheduler.addRequest(request);
                });
            }
            future.complete();
        }, res -> {
            if (res.succeeded()) {
                System.out.println("success start: " + spiderName);
            } else {
                System.out.println("oh no: " + res.cause().getMessage());
            }
        });
    }

}
