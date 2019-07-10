package bean;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:14
 * @Description:
 */
public class Response {

    private Connection.Response response;

    public Response(Connection.Response response) {
        this.response = response;
    }

    public Connection.Response getResponse() {
        return response;
    }

    public void setResponse(Connection.Response response) {
        this.response = response;
    }

    public Document getDocument() {
        Document document = null;
        try {
            document = getResponse().parse();
        } catch (IOException e) {
        } finally {
            return document;
        }
    }
}
