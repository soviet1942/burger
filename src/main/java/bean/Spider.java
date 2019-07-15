package bean;


import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:13
 * @Description:
 */
public class Spider {

    private Request request;
    private Response response;
    private Exception exception;

    private String name;
    private String cron;
    private List<String> allowedDomains;
    private List<String> startUrls;

    private Object instance;
    private Map<Method, Class<?>> methods;

    public Spider() {}

    public Spider(String name, List<String> allowedDomains, List<String> startUrls) {
        this.name = name;
        this.allowedDomains = allowedDomains;
        this.startUrls = startUrls;
    }

    public Object getInstance() {
        return instance;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Map<Method, Class<?>> getMethods() {
        return methods;
    }

    public void setMethods(Map<Method, Class<?>> methods) {
        this.methods = methods;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(List<String> allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public List<String> getStartUrls() {
        return startUrls;
    }

    public void setStartUrls(List<String> startUrls) {
        this.startUrls = startUrls;
    }
}
