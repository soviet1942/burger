package bean;


import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:13
 * @Description:
 */
public class Request {

    private HttpRequest<Buffer> httpRequest;
    private int retryCount = 0;
    private URI uri;
    private Map<String, Object> meta;

    public Request() {

    }

    public Request(HttpRequest<Buffer> httpRequest) {
        this.httpRequest = httpRequest;
    }

    public Request(HttpRequest<Buffer> httpRequest, URI uri) {
        this.httpRequest = httpRequest;
        this.uri = uri;
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

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
