package bean;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Feedback {

    URL url;
    private Map<String, Object> meta;

    public Feedback(URL url) {
        this.url = url;
    }

    public Feedback(URL url, Map<String, Object> meta) {
        this.url = url;
        this.meta = meta;
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
