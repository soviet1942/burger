package bean;

import io.vertx.ext.web.client.HttpResponse;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import spider.SpiderFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:14
 * @Description:
 */
public class Response {

    private HttpResponse httpResponse;
    private Map<String, Object> meta;
    private List<Feedback> feedbacks;

    public Response(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public Response() {}

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }



    public void addOutlink(URL... urls) {
        for (URL url : urls) {
            feedbacks.add(new Feedback(url, meta));
        }
    }

    public void addOutlink(String... urls) {
        addOutlink(Arrays.asList(urls));
    }

    public void addOutlink(List<String> urls) {
        for (String url : urls) {
            try {
                feedbacks.add(new Feedback(new URL(url), meta));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Feedback> getOutlinks() {
        return feedbacks;
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
}
