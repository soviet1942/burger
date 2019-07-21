package middleware.downloader;

import bean.Request;
import bean.Spider;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import middleware.downloader.interfaces.DownloaderMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 9:26
 * @Description:
 */
public class DefaultHttpDownloaderMiddleware implements DownloaderMiddleware {

    Logger logger = LoggerFactory.getLogger(DefaultHttpDownloaderMiddleware.class);

    @Override
    public void processRequest(Request request, Spider spider) {
        HttpRequest<Buffer> httpRequest = request.getHttpRequest();
        httpRequest.host(request.getUrl().getHost());
    }
}
