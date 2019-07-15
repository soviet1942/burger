package middleware;

import annotation.middleware.Priority;
import bean.*;
import core.Engine;
import middleware.downloader.DownloaderMiddleware;
import middleware.spider.SpiderMiddleware;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MiddlewareFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(MiddlewareFactory.class);
    private static MiddlewareFactory INSTANCE;
    /** download middleware */
    private List<DownloaderMiddleware> downloaderMWs = new ArrayList<>();
    /** spider middleware */
    private List<SpiderMiddleware> spiderMWs = new ArrayList<>();

    private MiddlewareFactory() {}

    public static MiddlewareFactory instance() {
        if (INSTANCE == null) {
            synchronized (Engine.class) {
                if (INSTANCE == null) {
                    MiddlewareFactory middlewareFactory = new MiddlewareFactory();
                    middlewareFactory.init();
                    INSTANCE = middlewareFactory;
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        loadMiddlewares(Crawler.getReflection());
    }

    /**
     * load (downloader + spider) middlewares
     *
     * @param reflections
     */
    private void loadMiddlewares(Reflections reflections) {
        List<PriorityClass> classPriorityList = reflections.getTypesAnnotatedWith(Priority.class).stream().map(e -> new PriorityClass() {{
            setPriorty(e.getAnnotation(Priority.class).value());
            setInterfaces(Arrays.stream(e.getInterfaces()).collect(Collectors.toSet()));
            setClazz(e);
        }}).collect(Collectors.toList());
        Collections.sort(classPriorityList, Comparator.comparing(PriorityClass::getPriorty));
        classPriorityList.forEach(e -> {
            try {
                if (e.getInterfaces().contains(DownloaderMiddleware.class)) {
                    downloaderMWs.add((DownloaderMiddleware) e.getClazz().newInstance());
                } else if (e.getInterfaces().contains(SpiderMiddleware.class)) {
                    spiderMWs.add((SpiderMiddleware) e.getClazz().newInstance());
                }
            } catch (InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        });
    }


    private List<DownloaderMiddleware> getDownloaderMWs() {
        return downloaderMWs;
    }

    private List<SpiderMiddleware> getSpiderMWs() {
        return spiderMWs;
    }

    /**
     * execute downloader MiddleWare
     */
    public void exeProcessRequest(Request request, Spider spider) {
        for (DownloaderMiddleware downloadMW : downloaderMWs) {
            downloadMW.processRequest(request, spider);
        }
    }
    public void exeProcessResponse(Request request, Response response, Spider spider) {
        for (DownloaderMiddleware downloadMW : downloaderMWs) {
            downloadMW.processResponse(request, response, spider);
        }
    }
    public void exeProcessException(Request request, Exception exception, Spider spider) {
        for (DownloaderMiddleware downloadMW : downloaderMWs) {
            downloadMW.processException(request, exception, spider);
        }
    }

    /**
     * execute spider MiddleWare
     */
    public void exeProcessSpiderInput(Response response, Spider spider) {
        for (SpiderMiddleware spiderMW : spiderMWs) {
            spiderMW.processSpiderInput(response, spider);
        }
    }
    public void exeProcessSpiderOutput(Response response, List<Object> result, Spider spider) {
        for (SpiderMiddleware spiderMW : spiderMWs) {
            spiderMW.processSpiderOutput(response, result, spider);
        }
    }
    public void exeProcessSpiderException(Response response, Exception exception, Spider spider) {
        for (SpiderMiddleware spiderMW : spiderMWs) {
            spiderMW.processSpiderException(response, exception, spider);
        }
    }

    public void initMiddleware(Engine engine) {
        Crawler crawler = Crawler.instance();
        getDownloaderMWs().forEach(e -> e.fromCrawler(crawler));
        getSpiderMWs().forEach(e -> e.fromCrawler(crawler));
    }
}
