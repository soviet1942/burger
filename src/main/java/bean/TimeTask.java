package bean;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class TimeTask implements Job {

    public void execute(JobExecutionContext context) {
        String spiderName = context.getTrigger().getJobKey().getName();
        System.out.println(spiderName);
    }

}
