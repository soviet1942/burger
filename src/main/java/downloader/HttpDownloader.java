package downloader;

import bean.Crawler;
import bean.Request;
import com.alibaba.fastjson.JSONObject;
import controller.Server;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import middleware.downloader.interfaces.DownloadInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpDownloader {

    private static Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);

    private static Integer connectTimeout;
    private static Integer maxPoolSize;
    private static Integer idelTimeout;

    static {
        init();
    }

    private static void init() {
        JSONObject jsonObject = Crawler.instance().configs().getJSONObject("download");
        if (jsonObject != null) {
            connectTimeout = jsonObject.getString("maxTimeout") == null ? 30000 : Integer.parseInt(jsonObject.getString("maxTimeout"));
            maxPoolSize = jsonObject.getString("maxPoolSize") == null ? 1 : Integer.parseInt(jsonObject.getString("maxTimeout"));
            idelTimeout = jsonObject.getString("idelTimeout") == null ? 10 : Integer.parseInt(jsonObject.getString("maxTimeout"));
        }
    }

    public static Request getDefaultRequest(String url) {
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setConnectTimeout(connectTimeout);
        webClientOptions.setMaxPoolSize(maxPoolSize);
        webClientOptions.setIdleTimeout(idelTimeout);
        WebClient client = WebClient.create(Server.getVertx(), webClientOptions);
        HttpRequest<Buffer> httpRequest = client.get(url);
        Request request = new Request(httpRequest, url);
        return request;
    }

    public static void httpDownload(String url, DownloadInboundHandler handler) {
        HttpRequest<Buffer> httpRequest = getDefaultRequest(url).getHttpRequest();
        handler.handle(httpRequest);
    }
}
