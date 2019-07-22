package core;

import annotation.mysql.Column;
import annotation.mysql.Table;
import annotation.spider.Parser;
import bean.*;
import com.alibaba.fastjson.JSONObject;
import controller.Server;
import downloader.HttpDownloader;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import middleware.MiddlewareFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipeline.PipelineFactory;
import spider.SpiderFactory;
import sun.net.www.http.HttpClient;
import utils.SqlUtils;

import java.lang.reflect.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 10:10
 * @Description: 爬虫引擎
 */
public class Engine {

    private static Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private static Engine INSTANCE;
    private static Map<String, Object> CLASSPATH_INSTANCE_MAP = new HashMap<>();
    /** scheduler */
    private static Scheduler SCHEDULER;
    /** middleware (downloader + spider) */
    private static MiddlewareFactory MIDDLEWARE_FACTORY;
    /** spider (parse text/html) */
    private static SpiderFactory SPIDER_FACTORY;
    /** pipeline (persist db) */
    private static PipelineFactory PIPELINE_FACTORY;

    private Engine() {}

    public static Engine instance() {
        if (INSTANCE == null) {
            synchronized (Engine.class) {
                if (INSTANCE == null) {
                    Engine engine = new Engine();
                    engine.loadFactory();
                    INSTANCE = engine;
                }
            }
        }
        return INSTANCE;
    }

    private void loadFactory() {
        SCHEDULER = Scheduler.instance();
        MIDDLEWARE_FACTORY = MiddlewareFactory.instance();
        PIPELINE_FACTORY = PipelineFactory.instance();
        SPIDER_FACTORY = SpiderFactory.instance();
    }

    public void download(Request request) {
        String spiderName = Optional.ofNullable(request).map(Request::getSpiderName).filter(StringUtils::isNotEmpty)
                .orElseThrow(() -> new IllegalArgumentException("request or request's spiderName should not be empty !"));
        Spider spider = Optional.ofNullable(SPIDER_FACTORY.getSpiderByName(spiderName))
                .orElseThrow(() -> new IllegalArgumentException("cannot find spider named " + spiderName));
        download(request, spider);
    }

    /**
     * 下载
     * @param request
     * @param spider
     */
    public void download(Request request, Spider spider) {
        //before download
        MIDDLEWARE_FACTORY.exeProcessRequest(request, spider);
        //downloading
        request.getHttpRequest().as(BodyCodec.string()).send(ar -> {
            //download success
            if (ar.succeeded()) {
                Response response = new Response(ar.result());
                //after download
                MIDDLEWARE_FACTORY.exeProcessResponse(request, response, spider);
                boolean containsFeedback = StringUtils.isNotEmpty(request.getCallback());
                try {
                    //start parse
                    if (containsFeedback) {
                        feedback(response, spider);
                    } else {
                        parse(response, spider);
                    }
                } catch (Exception e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                    MIDDLEWARE_FACTORY.exeProcessSpiderException(response, e.getCause(), spider);
                }
            } else {
                //download failed
                LOGGER.error(ExceptionUtils.getStackTrace(ar.cause()));
                MIDDLEWARE_FACTORY.exeProcessException(request, ar.cause(), spider);
            }
        });

    }

    /**
     * 解析
     * @param response
     * @param spider
     */
    public void parse(Response response, Spider spider) {
        //before parse
        MIDDLEWARE_FACTORY.exeProcessSpiderInput(response, spider);
        for (Map.Entry<Method, Class<?>> mEntry : spider.getMethods().entrySet()) {
            Method method = mEntry.getKey();
            Class<?> returnType = mEntry.getValue();
            if (method.getAnnotation(Parser.class) != null) {
                try {
                    //parsing
                    Object returnValue = method.invoke(spider.getInstance(), response);
                    //after parse
                    MIDDLEWARE_FACTORY.exeProcessSpiderOutput(response, spider);
                    //add feedback
                    addFeedback(response, spider);
                    //begin to persist
                    persist(returnType, returnValue);
                } catch (Exception e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                    MIDDLEWARE_FACTORY.exeProcessSpiderException(response, e.getCause(),spider);
                }
            }
        }
    }

    /**
     * 回掉
     *
     * @param response
     */
    public void feedback(Response response, Spider spider) {
        //before callback
        MIDDLEWARE_FACTORY.exeProcessSpiderInput(response, spider);
        for (Feedback feedback : response.getFeedbacks()) {
            String callbackPath = feedback.getCallback();
            if (StringUtils.isNotEmpty(callbackPath)) {
                Integer splitIndex = callbackPath.lastIndexOf(".");
                String classPath = callbackPath.substring(0, splitIndex);
                String methodName = callbackPath.substring(splitIndex + 1);
                try {
                    Object instance = CLASSPATH_INSTANCE_MAP.get(classPath);
                    if (instance == null) {
                        instance = Class.forName(classPath).newInstance();
                        CLASSPATH_INSTANCE_MAP.put(classPath, instance);
                    }
                    //call back
                    Method method = instance.getClass().getDeclaredMethod(methodName, Response.class);
                    Object returnValue = method.invoke(instance, response);
                    Class<?> returnType = method.getReturnType();
                    //after callback
                    MIDDLEWARE_FACTORY.exeProcessSpiderOutput(response, spider);
                    //begin to persist
                    persist(returnType, returnValue);
                } catch (Exception e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                    MIDDLEWARE_FACTORY.exeProcessSpiderException(response, e.getCause(), spider);
                }
            }
        }
    }


    /**
     * 添加回掉
     * @param response
     * @param spider
     */
    public void addFeedback(Response response, Spider spider) {
        for (Feedback feedback : response.getFeedbacks()) {
            String callbackPath = feedback.getCallback();
            String url = feedback.getUrl().toString();
            Request request = HttpDownloader.instance().getDefaultRequest(url);
            request.setCallback(callbackPath);
            request.setSpiderName(spider.getName());
            request.addMeta(feedback.getAllMeta());
            Scheduler.addRequest(request);
        }
    }

    /**
     * 持久化
     * @param returnType
     * @param data
     */
    public void persist(Class<?> returnType, Object data) {
        List<Object> dataList;
        if (returnType != null) {
            if (returnType == List.class) {
                dataList = (List) data;
            } else {
                dataList = new ArrayList<Object>() {{ add(data); }};
            }
            List<String> sqlList = PIPELINE_FACTORY.generateSql(returnType, dataList);

        }
    }

}
