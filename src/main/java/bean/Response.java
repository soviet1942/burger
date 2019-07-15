package bean;

import io.vertx.ext.web.client.HttpResponse;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:14
 * @Description:
 */
public class Response {

    private HttpResponse httpResponse;
    private URL[] outlinks;

    public Response(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public Response() {}

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public URL[] getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(URL... outlinks) {
        this.outlinks = outlinks;
    }
}
