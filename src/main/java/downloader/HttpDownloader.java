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

import java.util.Optional;

public class HttpDownloader {

    private static Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);

    private static HttpDownloader INSTANCE;
    private static WebClientOptions CLIENT_OPTIONS;

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
        Integer connectTimeout = Optional.ofNullable(jsonObject).map(e -> e.getString("maxTimeout")).map(Integer::parseInt).orElse(30000);
        Integer maxPoolSize = Optional.ofNullable(jsonObject).map(e -> e.getString("maxPoolSize")).map(Integer::parseInt).orElse(1);
        Integer idelTimeout = Optional.ofNullable(jsonObject).map(e -> e.getString("idelTimeout")).map(Integer::parseInt).orElse(10);
        CLIENT_OPTIONS = new WebClientOptions() {{
            setConnectTimeout(connectTimeout);
            setMaxPoolSize(maxPoolSize);
            setIdleTimeout(idelTimeout);
        }};
    }

    public Request getDefaultRequest(String url) {
        return getDefaultRequest(url, null);
    }

    public Request getDefaultRequest(String url, String spiderName) {
        WebClient client = WebClient.create(Server.getVertx(), CLIENT_OPTIONS);
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
