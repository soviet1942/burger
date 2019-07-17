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

public class HttpDownloader {

    private static Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);

    private static Integer connectTimeout;
    private static Integer maxPoolSize;
    private static Integer idelTimeout;
    private static HttpDownloader INSTANCE;

    private HttpDownloader() {}

    public static HttpDownloader instance() {
        if (INSTANCE == null) {
            synchronized (HttpDownloader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpDownloader();
                    INSTANCE.init();
                }
            }
        }
        return INSTANCE;
    }


    private void init() {
        JSONObject jsonObject = Crawler.instance().configs().getJSONObject("download");
        if (jsonObject != null) {
            connectTimeout = jsonObject.getString("maxTimeout") == null ? 30000 : Integer.parseInt(jsonObject.getString("maxTimeout"));
            maxPoolSize = jsonObject.getString("maxPoolSize") == null ? 1 : Integer.parseInt(jsonObject.getString("maxTimeout"));
            idelTimeout = jsonObject.getString("idelTimeout") == null ? 10 : Integer.parseInt(jsonObject.getString("maxTimeout"));
        }
    }

    public Request getDefaultRequest(String url) {
        return getDefaultRequest(url, null);
    }

    public Request getDefaultRequest(String url, String spiderName) {
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setConnectTimeout(connectTimeout);
        webClientOptions.setMaxPoolSize(maxPoolSize);
        webClientOptions.setIdleTimeout(idelTimeout);
        WebClient client = WebClient.create(Server.getVertx(), webClientOptions);
        HttpRequest<Buffer> httpRequest = client.get(url);
        Request request = new Request(httpRequest, url);
        if (spiderName != null) {
            request.setSpiderName(spiderName);
        }
        return request;
    }

    public void httpDownload(String url, DownloadInboundHandler handler) {
        HttpRequest<Buffer> httpRequest = getDefaultRequest(url).getHttpRequest();
        handler.handle(httpRequest);
    }
}
