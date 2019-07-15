package middleware.downloader;

import annotation.middleware.Priority;
import bean.Crawler;
import bean.Request;
import bean.Response;
import bean.Spider;
import io.netty.channel.ConnectTimeoutException;
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
    public void processException(Request request, Throwable throwable, Spider spider) {
        String exceptionMsg = ExceptionUtils.getStackTrace(throwable);
        String url = request.getUri().toString();
        if (throwable instanceof SocketTimeoutException || throwable instanceof ConnectTimeoutException) {
            int retryCount = request.getRetryCount();
            if (retryCount > maxRetryNum) {
                logger.warn("多次下载超时，重试次数大于{}次，下载失败，url: {}", maxRetryNum, url, exceptionMsg);
            } else {
                request.setRetryCount(++ maxRetryNum);
                logger.info("下载超时 重试，重试次数{}, url: {}", maxRetryNum, url, exceptionMsg);
            }
        }
    }

    @Override
    public void fromCrawler(Crawler crawler) {
        this.maxRetryNum = 3;
    }


}
