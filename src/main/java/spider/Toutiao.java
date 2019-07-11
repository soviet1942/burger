package spider;

import annotation.spider.Parser;
import annotation.spider.Spider;
import bean.Response;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/11 14:25
 * @Description:
 */

@Spider(name = "toutiao", cron = "0 0/5 * * * ? *", startUrls = "https://www.toutiao.com/api/pc/realtime_news/",
        allowedDomains = {"toutiao.com", "365yg.com", "ixigua.com"})
public class Toutiao {

    @Parser
    public void parse(Response response) {
        String jsonStr = response.getDocument().text();
        JSON.parseObject(jsonStr).getJSONArray("data").forEach(e -> {
            JSONObject data = JSONObject.parseObject(e.toString());
            String url = data.getString("open_url");
            String imgUrl = data.getString("image_url");
            String title = data.getString("title");
            System.out.println(url);
            System.out.println(imgUrl);
            System.out.println(title);
        });

    }

}
