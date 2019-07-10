package core.middleware;

import core.engine.bean.Request;
import core.engine.bean.Response;
import core.engine.bean.Spider;
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
        logger.error(exception.getCause().getMessage());
    }

    default void fromCrawler() {

    }
}
