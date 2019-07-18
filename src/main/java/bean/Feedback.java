package bean;

import java.net.URL;
import java.util.List;

public class Feedback {
    private List<URL> outlinks;
    private String spiderName;

    public List<URL> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<URL> outlinks) {
        this.outlinks = outlinks;
    }

    public String getSpiderName() {
        return spiderName;
    }

    public void setSpiderName(String spiderName) {
        this.spiderName = spiderName;
    }
}
