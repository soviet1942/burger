package bean;

import com.alibaba.fastjson.JSONObject;
import middleware.DownloaderMiddleware;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import utils.FileReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 13:34
 * @Description:
 */
public class Crawler {

    private Logger logger = LoggerFactory.getLogger(Crawler.class);
    private JSONObject configs;

    {
        URL url = Thread.currentThread().getContextClassLoader().getResource("config/burger.yaml");
        configs = FileReader.transYaml2Json(url.getPath());
    }

    public JSONObject configs() {
        return configs;
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        String userAgentType = crawler.configs().getJSONObject("download").getString("userAgentType");
        System.out.println(userAgentType);
    }
}
