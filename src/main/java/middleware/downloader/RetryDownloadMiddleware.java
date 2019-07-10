package middleware.downloader;

import annotation.middleware.Priority;
import bean.Crawler;
import bean.Request;
import bean.Response;
import bean.Spider;
import middleware.DownloaderMiddleware;
import org.jsoup.Connection;

import java.net.SocketTimeoutException;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:24
 * @Description:
 */

@Priority(10)
public class RetryDownloadMiddleware implements DownloaderMiddleware {

    private Integer maxRetryNum;

    @Override
    public void processRequest(Request request, Spider spider) {
        Connection connection = request.getConnection();
        System.out.println("请求网页地址 " + connection.request().url().toString());
    }

    @Override
    public void processResponse(Request request, Response response, Spider spider) {
        String text = response.getDocument().text();
        System.out.println("收到回复 " + text);
    }

    @Override
    public void processException(Request request, Exception exception, Spider spider) {
        if (exception.getClass() == SocketTimeoutException.class) {
            int retryCount = request.getRetryCount();
            if (retryCount > maxRetryNum) {
                logger.warn("下载失败,重试次数大于{}次, url: {}", maxRetryNum, request.getConnection().request().url().toString());
            } else {
                request.setRetryCount(++ maxRetryNum);
                logger.info("下载重试，重试次数{}, url: {}", maxRetryNum, request.getConnection().request().url().toString());
            }
        } else {
            logger.error("下载失败，url");
        }
    }

    @Override
    public void fromCrawler(Crawler crawler) {
        this.maxRetryNum = 3;
    }


}
