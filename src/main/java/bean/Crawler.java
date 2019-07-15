package bean;

import com.alibaba.fastjson.JSONObject;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileReader;

import java.net.URL;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 13:34
 * @Description:
 */
public class Crawler {

    private Logger LOGGER = LoggerFactory.getLogger(Crawler.class);
    private JSONObject CONF;
    private static Crawler INSTANCE;
    private static Reflections REFLECTION;

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

    public static Reflections getReflection() {
        if (REFLECTION == null) {
            synchronized (Crawler.class) {
                if (REFLECTION == null) {
                    REFLECTION = new Reflections("");
                }
            }
        }
        return REFLECTION;
    }

    public void init() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("config/burger.yaml");
        CONF = FileReader.transYaml2Json(url.getPath());
    }
}
