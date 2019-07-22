package spider;

import annotation.spider.Feedback;
import annotation.spider.Injector;
import annotation.spider.Parser;
import annotation.spider.Spider;
import bean.Response;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.HttpResponse;
import org.junit.jupiter.api.Test;
import pipeline.ToutiaoPO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/12 10:05
 * @Description:
 */

@Spider(name = "toutiao", cron = "0/3 * * * * ?", filterUrls = {".*toutiao.com.*"})
public class ToutiaoArticle {

    @Injector
    public List<URL> startRequests() throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        String tempUrl = "https://www.toutiao.com/api/pc/realtime_news/";
        for (int i=0; i < 1; i++) {
            urls.add(new URL(tempUrl));
        }
        return urls;
    }

    @Parser
    public void realtimeNews(Response response) throws NoSuchMethodException {
        String jsonStr = response.text();
        System.out.println(jsonStr);
        List<ToutiaoPO> res = new ArrayList<>();
        JSONArray jsonArray = JSON.parseObject(jsonStr).getJSONArray("data");
        for (int i=0; i<jsonArray.size(); i++) {
            JSONObject data = jsonArray.getJSONObject(i);
            String openUrl = data.getString("open_url");
            String url = "https://www.toutiao.com" + openUrl.substring(1);
            String cover = data.getString("image_url");
            String title = data.getString("title");
            ToutiaoPO po = new ToutiaoPO();
            po.setUrl(url);
            po.setTitle(title);
            po.setPoster(cover);
            res.add(po);
            response.addFeedback(url, "spider.ToutiaoArticle.detail");
        }

    }

    @Feedback
    public void detail(Response response) {
        System.out.println("hehe");
    }

    @Test
    public void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        /*Map<String, Object> classPathInstanceMap = new HashMap<>();
        String callbackPath = "spider.ToutiaoArticle.detail";
        Integer splitIndex = callbackPath.lastIndexOf(".");
        String classPath = callbackPath.substring(0, splitIndex);
        String methodName = callbackPath.substring(splitIndex + 1);
        Object instance = classPathInstanceMap.get(classPath);
        if (instance == null) {
            instance = Class.forName(classPath).newInstance();
            classPathInstanceMap.put(classPath, instance);
        }
        Method method = Class.forName(classPath).getDeclaredMethod(methodName, Response.class);
        method.invoke(instance, new Response());*/
    }
}
