package bean;

import io.vertx.ext.web.client.HttpResponse;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import spider.SpiderFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:14
 * @Description:
 */
public class Response {

    private HttpResponse httpResponse;
    private Feedback feedback;

    public Response(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public Response() {}

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public Feedback getFeedback() {
        return feedback;
    }


    public void addFeedback(URL... urls) {
        this.feedback.setOutlinks(Arrays.asList(urls));
    }

    public void addFeedback(String... urls) {
        addFeedback(Arrays.asList(urls));
    }

    public void addFeedback(List<String> urls) {
        List<URL> list = new ArrayList<>();
        for (String url : urls) {
            try {
                list.add(new URL(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        this.feedback.setOutlinks(list);
    }

}
