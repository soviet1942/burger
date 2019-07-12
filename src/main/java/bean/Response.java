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
    public Response() {}

    public Connection.Response getResponse() {
        return response;
    }

    public void setResponse(Connection.Response response) {
        this.response = response;
    }

    public Document getDocument() {
        Document document = null;
        try {
            document = this.response.parse();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return document;
        }
    }
}
