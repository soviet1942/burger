package middleware.downloader;

import bean.Request;
import bean.Spider;
import middleware.downloader.interfaces.DownloaderMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 9:26
 * @Description:
 */
public class DefaultDownloaderMiddleware implements DownloaderMiddleware {

    Logger logger = LoggerFactory.getLogger(DefaultDownloaderMiddleware.class);

    @Override
    public void processRequest(Request request, Spider spider) {

    }
}
