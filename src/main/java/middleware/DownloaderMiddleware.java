package middleware;

import bean.Crawler;
import bean.Request;
import bean.Response;
import bean.Spider;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:11
 * @Description:
 */
public interface DownloaderMiddleware {

    Logger logger = LoggerFactory.getLogger(DownloaderMiddleware.class);

    default void processRequest(Request request, Spider spider) {

    }

    default void processResponse(Request request, Response response, Spider spider) {

    }

    default void processException(Request request, Exception exception, Spider spider) {

    }

    default void fromCrawler(Crawler crawler) {

    }
}
