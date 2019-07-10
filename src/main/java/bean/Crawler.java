package bean;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import utils.FileReader;

import java.net.URL;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 13:34
 * @Description:
 */
public class Crawler {

    public void loadConfig() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("config/burger.yaml");
        JSONObject jsonObject = FileReader.transYaml2Json(url.getPath());
        System.out.println(jsonObject.toJSONString());
        System.out.println(jsonObject.getJSONObject("download").getString("maxRetry"));
    }

    public static void main(String[] args) {
        new Crawler().loadConfig();
    }
}
