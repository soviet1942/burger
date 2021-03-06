package bean;


import downloader.HttpDownloader;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:13
 * @Description:
 */
public class Request {

    private HttpRequest<Buffer> httpRequest;
    private URL url;
    private short retryCount;
    private short priority;
    private String spiderName;
    private String callback;
    private Map<String, Object> meta;

    public Request() {

    }

    public Request(HttpRequest<Buffer> httpRequest) {
        this.httpRequest = httpRequest;
    }

    public Request(HttpRequest<Buffer> httpRequest, URL url) {
        this.httpRequest = httpRequest;
        this.url = url;
        httpRequest.host(this.url.getHost());
    }

    public Request(HttpRequest<Buffer> httpRequest, String url) {
        this.httpRequest = httpRequest;
        try {
            this.url = new URL(url);
            httpRequest.host(this.url.getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public <M> M getMeta(String name) {
        return (M) meta.get(name);
    }

    public Map<String, Object> getAllMeta() {
        return meta;
    }

    public void addMeta(String name, Object value) {
        if (meta == null) {
            meta = new HashMap<>();
        }
        meta.put(name, value);
    }

    public void addMeta(Map<String, Object> meta) {
        if (meta == null) {
            meta = new HashMap<>();
        }
        this.meta = meta;
    }

    public HttpRequest<Buffer> getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest<Buffer> httpRequest) {
        this.httpRequest = httpRequest;
    }

    public short getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(short retryCount) {
        this.retryCount = retryCount;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }

    public String getSpiderName() {
        return spiderName;
    }

    public void setSpiderName(String spiderName) {
        this.spiderName = spiderName;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}
