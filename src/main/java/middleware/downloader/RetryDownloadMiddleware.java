package middleware.downloader;

import annotation.middleware.Priority;
import bean.Crawler;
import bean.Request;
import bean.Response;
import bean.Spider;
import middleware.DownloaderMiddleware;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
        System.out.println("收到回复 ");
    }

    @Override
    public void processException(Request request, Exception exception, Spider spider) {
        String exceptionMsg = ExceptionUtils.getStackTrace(exception.fillInStackTrace());
        String url = request.getConnection().request().url().toString();
        if (exception.getClass() == SocketTimeoutException.class) {
            int retryCount = request.getRetryCount();
            if (retryCount > maxRetryNum) {
                logger.warn("多次下载超时，重试次数大于{}次，下载失败，url: {}", maxRetryNum, url, exceptionMsg);
            } else {
                request.setRetryCount(++ maxRetryNum);
                logger.info("下载超时 重试，重试次数{}, url: {}", maxRetryNum, url, exceptionMsg);
            }
        } /*else {
            logger.error("下载失败，url：{}, msg：{}", url, exceptionMsg);
        }*/
    }

    @Override
    public void fromCrawler(Crawler crawler) {
        this.maxRetryNum = 3;
    }


}
