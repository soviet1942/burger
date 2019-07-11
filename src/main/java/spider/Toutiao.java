package spider;

import annotation.spider.Parser;
import annotation.spider.Spider;
import bean.Response;
import org.jsoup.nodes.Document;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/11 14:25
 * @Description:
 */

@Spider(name = "spider", startUrls = "https://www.toutiao.com/", allowedDomains = {"toutiao", "365yg", "ixigua"})
public class Toutiao {


    @Parser
    public void parse(Response response) {
        Document document = response.getDocument();
        String title = "";
        String content = "";
        String author = "";
        String url = "";
        boolean isOriginal = true;
        System.out.println("hhh");
    }

}
