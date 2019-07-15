package spider;

import annotation.spider.Parser;
import annotation.spider.Spider;
import bean.Response;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import pipeline.ToutiaoPO;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/12 10:05
 * @Description:
 */

@Spider(name = "toutiao", cron = "0 0/5 * * * ? *", startUrls = "https://www.toutiao.com/api/pc/realtime_news/",
        allowedDomains = {"toutiao.com", "365yg.com", "ixigua.com"})
public class ToutiaoArticle {

    @Parser
    public List<ToutiaoPO> parse(Response response) {
        String jsonStr = response.getDocument().text();
        List<ToutiaoPO> res = new ArrayList<>();
        JSONArray jsonArray = JSON.parseObject(jsonStr).getJSONArray("data");
        for (int i=0; i<jsonArray.size(); i++) {
            JSONObject data = jsonArray.getJSONObject(i);
            String url = data.getString("open_url");
            String imgUrl = data.getString("image_url");
            String title = data.getString("title");
            ToutiaoPO po = new ToutiaoPO();
            po.setUrl(url);
            po.setTitle(title);
            po.setPoster(imgUrl);
        }
        return res;
    }
}
