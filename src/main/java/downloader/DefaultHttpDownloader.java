package downloader;

import bean.Crawler;
import bean.Request;
import com.alibaba.fastjson.JSONObject;
import controller.Server;
import core.Engine;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/15 18:19
 * @Description:
 */
public class DefaultHttpDownloader {

    private static Integer maxTimeout;
    private static Integer maxPoolSize;
    private static Integer idelTimeout;

    static {
        init();
    }

    private void init() {
        JSONObject jsonObject = Crawler.instance().configs().getJSONObject("download");
        maxTimeout = jsonObject.getString("maxTimeout") == null ? 30000 : Integer.parseInt(jsonObject.getString("maxTimeout"));
        maxPoolSize = jsonObject.getString("maxPoolSize") == null ? 1 : Integer.parseInt(jsonObject.getString("maxTimeout"));
        idelTimeout = jsonObject.getString("idelTimeout") == null ? 10 : Integer.parseInt(jsonObject.getString("maxTimeout"));
    }

    private static Request getDefaultDownloader(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setConnectTimeout();
        webClientOptions.setIdleTimeout(10);
        webClientOptions.setMaxPoolSize(1);
        WebClient client = WebClient.create(Server.getVertx(), webClientOptions);
        HttpRequest<Buffer> httpRequest = client.get(uri.getRawPath());
        Request request = new Request(httpRequest, uri);
        return request;
    }

    private static void defaultDownload(String url, HttpHandler handler) {
        HttpRequest httpRequest = getDefaultDownloader(url).getHttpRequest();
        handler.handle(httpRequest);
    }

    interface HttpHandler {
        void handle(HttpRequest httpRequest);
    }
}
