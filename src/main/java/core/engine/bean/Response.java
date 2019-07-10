package core.engine.bean;

import org.jsoup.nodes.Document;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:14
 * @Description:
 */
public class Response {

    private Document document;

    public Response(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
