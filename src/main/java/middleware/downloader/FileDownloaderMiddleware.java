package middleware.downloader;

import bean.Request;
import bean.Response;
import bean.Spider;
import controller.Server;
import downloader.HttpDownloader;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.codec.BodyCodec;
import middleware.downloader.interfaces.DownloaderMiddleware;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 15:35
 * @Description:
 */
public class FileDownloaderMiddleware implements DownloaderMiddleware {

    private static Logger LOGGER = LoggerFactory.getLogger(FileDownloaderMiddleware.class);

    @Override
    public void processResponse(Request request, Response response, Spider spider) {
        HttpResponse httpResponse = response.getHttpResponse();
        Buffer buffer;
        if (httpResponse != null && (buffer = httpResponse.bodyAsBuffer()) != null) {
            downloadFile(request, buffer);
        }
    }

    public void downloadFile(Request request, Buffer buffer) {
        String filePath = request.getMeta("filePath");
        FileSystem fs = Server.getVertx().fileSystem();
        Future createFuture = Future.future(); //a file is created (createFuture)
        Future writeFuture = Future.future(); //something is written in the file (writeFuture)
        fs.createFile(filePath, createFuture);
        createFuture.compose(v -> {
            fs.writeFile(filePath, buffer, writeFuture);
            return writeFuture;
        });
    }

    @Test
    public void testDownloadPic() {
        HttpDownloader.httpDownload("https://www.csgo.com.cn/web201608/images/cslogo.png", httpRequest -> {
            httpRequest.as(BodyCodec.string()).send(ar -> {
                if (ar.succeeded()) {

                }
            });
        });
    }

    @Test
    public void testDownloadVideo() {
        String counterStrike = "https://gamevideo.wmupd.com/csgomedia/media/LEFT_1_21.webm";
        String terrorist = "https://gamevideo.wmupd.com/csgomedia/media/RIGHT_1_20.webm";
        String background = "https://gamevideo.wmupd.com/csgomedia/media/sirocco_webm.webm";
        String dota2 = "https://gamevideo.wmupd.com/dota2media/media/0510.webm";
        HttpDownloader.httpDownload(counterStrike, httpRequest -> {
            httpRequest.as(BodyCodec.string()).send(ar -> {
                if (ar.succeeded()) {

                }
            });
        });
    }
}
