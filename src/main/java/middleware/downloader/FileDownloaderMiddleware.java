package middleware.downloader;

import bean.Request;
import bean.Response;
import bean.Spider;
import controller.Server;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
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

    @Test
    public void fff() {
        Request request = new Request();
        request.addMeta("aa", new Object());
        System.out.println(request.getAllMeta());
        String a = request.getMeta("aa");
        System.out.println(a);
    }

    @Override
    public void processResponse(Request request, Response response, Spider spider) {
        HttpResponse httpResponse = response.getHttpResponse();
        Buffer buffer = null;
        if (httpResponse == null || (buffer = httpResponse.bodyAsBuffer()) == null) {
            URI uri = null;
            try {
                uri = new URI("http://httpbin.org/headers");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
        downloadFile(request, buffer);
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
}
