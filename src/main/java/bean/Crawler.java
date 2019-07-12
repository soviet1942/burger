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

    private Logger LOGGER = LoggerFactory.getLogger(Crawler.class);
    private JSONObject CONF;
    private static Crawler INSTANCE;

    public JSONObject configs() {
        return CONF;
    }

    public static Crawler instance() {
        if (INSTANCE == null) {
            synchronized (Crawler.class) {
                if (INSTANCE == null) {
                    Crawler crawler = new Crawler();
                    crawler.init();
                    INSTANCE = crawler;
                }
            }
        }
        return INSTANCE;
    }

    public void init() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("config/burger.yaml");
        CONF = FileReader.transYaml2Json(url.getPath());
    }
}
