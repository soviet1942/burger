package middleware.downloader;

import annotation.middleware.Priority;
import bean.Crawler;
import bean.Request;
import bean.Spider;
import com.alibaba.fastjson.JSON;
import middleware.downloader.interfaces.DownloaderMiddleware;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/11 10:06
 * @Description: 随机user-agent 包含（pc、Android、ios）
 */
@Priority(20)
public class UserAgentMiddleware implements DownloaderMiddleware {

    Logger logger = LoggerFactory.getLogger(UserAgentMiddleware.class);
    Random random = new Random();
    String userAgentType;
    List<String> userAgents;
    Integer agentSize;

    @Override
    public void fromCrawler(Crawler crawler) {
        String userAgentType = crawler.configs().getJSONObject("download").getString("userAgentType");
        this.userAgentType = userAgentType == null ? "pc" : userAgentType;
        List<String> userAgents = loadUserAgents(userAgentType);
        switch (userAgentType) {
            case "pc": this.userAgents = userAgents != null ? userAgents : new ArrayList<String>() {{ add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");}};
            case "android": this.userAgents = userAgents != null ? userAgents : new ArrayList<String>() {{add("Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");}};
            case "ios": this.userAgents = userAgents != null ? userAgents : new ArrayList<String>() {{add("Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");}};
        }
        this.agentSize = userAgents.size();
    }

    @Override
    public void processRequest(Request request, Spider spider) {
        String userAgent = userAgents.get(random.nextInt(agentSize));
        request.getHttpRequest().putHeader("user-agent", userAgent);
    }

    public List<String> loadUserAgents(String userAgentType) {
        List<String> res = null;
        String filePath = Thread.currentThread().getContextClassLoader().getResource("config/user-agent").getPath();
        try {
            String jsonStr = FileUtils.readFileToString(new File(filePath), "utf-8");
            res = JSON.parseObject(jsonStr).getJSONArray(userAgentType).toJavaList(String.class)
                    .stream().peek(e -> e.replaceAll("\\n", "")).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return res;
    }

}
