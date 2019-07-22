package bean;

import io.vertx.ext.web.client.HttpResponse;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import spider.SpiderFactory;

import java.io.IOException;
import java.lang.reflect.Method;
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

    public String text() {
        return this.getHttpResponse().body().toString();
    }

    public Document html() {
        return Jsoup.parse(this.text());
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void addFeedback(String url, String callbackMethod) {
        Feedback feedback = new Feedback();
        feedback.setCallback(callbackMethod);
        try {
            feedback.setUrl(new URL(url));
            feedbacks.add(feedback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public List<Feedback> getFeedbacks() {
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
