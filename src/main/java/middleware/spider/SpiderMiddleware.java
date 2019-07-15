package middleware.spider;

import bean.Crawler;
import bean.Response;
import bean.Spider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:11
 * @Description:
 */
public interface SpiderMiddleware {

    Logger logger = LoggerFactory.getLogger(SpiderMiddleware.class);

    default void processSpiderInput(Response response, Spider spider) {

    }

    default void processSpiderOutput(Response response, List<Object> result, Spider spider) {

    }

    default void processSpiderException(Response response, Throwable throwable, Spider spider) {

    }

    default void fromCrawler(Crawler crawler) {

    }
}
