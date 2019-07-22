package bean;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Feedback {

    private URL url;
    private String callback; //回掉方法相对路径 例: com.website.pro1.package1.class1.method2
    private Map<String, Object> meta;

    public Feedback() {
    }

    public Feedback(URL url) {
        this.url = url;
    }

    public Feedback(URL url, Map<String, Object> meta) {
        this.url = url;
        this.meta = meta;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public <M> M getMeta(String name) {
        return (M) meta.get(name);
    }

    public Map<String, Object> getAllMeta() {
        return meta;
    }

    public void addMeta(String name, Object value) {
        if (meta == null) {
            meta = new HashMap<>();
        }
        meta.put(name, value);
    }

    public void addMeta(Map<String, Object> meta) {
        if (meta == null) {
            meta = new HashMap<>();
        }
        this.meta = meta;
    }
}
