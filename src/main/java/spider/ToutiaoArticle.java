package spider;

import annotation.spider.Parser;
import annotation.spider.Spider;
import bean.Response;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.HttpResponse;
import pipeline.ToutiaoPO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/12 10:05
 * @Description:
 */

@Spider(name = "toutiao", cron = "0/3 * * * * ?", startUrls = {"http://www.toutiao.com/api/pc/realtime_news/"},
        filterUrls = {".*toutiao.com.*"})
public class ToutiaoArticle {

    private String TOUTIAO_HOST = "http://www.toutiao.com/";

    /**
     * 头条实时新闻
     * @param response
     * @return
     */
    @Parser
    public List<ToutiaoPO> realtimeNews(Response response) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String jsonStr = response.text();
        List<ToutiaoPO> res = new ArrayList<>();
        JSONArray jsonArray = JSON.parseObject(jsonStr).getJSONArray("data");
        for (int i=0; i<jsonArray.size(); i++) {
            JSONObject data = jsonArray.getJSONObject(i);
            String url = data.getString("open_url");
            String cover = data.getString("image_url");
            String title = data.getString("title");
            ToutiaoPO po = new ToutiaoPO();
            po.setUrl(url);
            po.setTitle(title);
            po.setPoster(cover);
            res.add(po);
        }
        //response.addOutlink("http://www.baidu.com");
        Method method = this.getClass().getMethod("detail", Response.class);

        return res;
    }
    
    public void detail(Response response) {
        
    }
}
