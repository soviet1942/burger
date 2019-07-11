package core;

import annotation.middleware.Priority;
import annotation.spider.Parser;
import bean.*;
import middleware.DownloaderMiddleware;
import middleware.SpiderMiddleware;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 10:10
 * @Description:
 */
public class Engine {

    private Logger logger = LoggerFactory.getLogger(Engine.class);
    private List<DownloaderMiddleware> downloaderMWs = new ArrayList<>();
    private List<SpiderMiddleware> spiderMWs = new ArrayList<>();
    private List<Spider> spiders = new ArrayList<>();
    private static Engine INSTANCE;
    private Engine() {}

    public static Engine instance() {
        if (INSTANCE == null) {
            synchronized (Engine.class) {
                if (INSTANCE == null) {
                    Engine engine = new Engine();
                    engine.init(engine);
                    INSTANCE = engine;
                }
            }
        }
        return INSTANCE;
    }

    private void init(Engine engine) {
        Reflections reflections = new Reflections("");
        engine.loadMiddlewares(reflections);
        engine.loadSpiders(reflections);
    }

    private void loadMiddlewares(Reflections reflections) {
        //load (downloader + spider) middlewares
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

     private void loadSpiders(Reflections reflections) {
        //load spider
        reflections.getTypesAnnotatedWith(annotation.spider.Spider.class).forEach(clazz -> {
            Spider spider = new Spider();
            annotation.spider.Spider spiderAnt = clazz.getAnnotation(annotation.spider.Spider.class);
            String spiderName = spiderAnt.name();
            String[] allowedDomains = spiderAnt.allowedDomains();
            String[] startUrls = spiderAnt.startUrls();
            //get all parse() methods
            List<Method> methods = Arrays.stream(clazz.getDeclaredMethods()).filter(s -> s.getAnnotation(Parser.class) != null).collect(Collectors.toList());
            spider.setName(spiderName);
            spider.setAllowedDomains(Arrays.asList(allowedDomains));
            spider.setStartUrls(Arrays.asList(startUrls));
            spider.setMethods(methods);
            try {
                spider.setInstance(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn(ExceptionUtils.getMessage(e.getCause()));
            }
            spiders.add(spider);
        });
    }

    public List<DownloaderMiddleware> getDownloaderMWs() {
        return downloaderMWs;
    }

    public List<SpiderMiddleware> getSpiderMWs() {
        return spiderMWs;
    }

    public List<Spider> getSpiders() {
        return spiders;
    }

    /** execute downloader MiddleWare */
    public void exeFromCrawler(Crawler crawler) {
        for (DownloaderMiddleware downloadMW : downloaderMWs) {
            downloadMW.fromCrawler(crawler);
        }
    }
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
    /** execute spider MiddleWare */
    public void exeProcessStartRequests(List<String> startUrls, Spider spider) {
        for (SpiderMiddleware spiderMW : spiderMWs) {
            spiderMW.processStartRequests(startUrls, spider);
        }
    }
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

    public void handle() {

    }

    public static void main(String[] args) {
        Engine engine = Engine.instance();
        Request request = new Request(Jsoup.connect("http://httpbin.org/ip").ignoreContentType(true));

        for (Spider spider : engine.getSpiders()) {
            //download

            Response res = new Response();
            Response response = new Response();
            for (SpiderMiddleware spiderMW : engine.getSpiderMWs()) {

            }
            for (Method method : spider.getMethods()) {
                try {
                    method.invoke(spider.getInstance(), response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        engine.getSpiders().forEach(spider -> spider.getMethods().forEach(method -> {
        }));



        /*downloaderMWs.forEach(downloadMW -> {
            try {
                Request request = new Request(Jsoup.connect("http://httpbin.org/ip"));
                //before download
                downloadMW.processRequest(request, spider);


                Response response = new Response(request.getConnection().ignoreContentType(true).execute());
                //after download
                downloadMW.processResponse(request, response, spider);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        spiderMWs.forEach(spiderMWs -> {
            spiderMWs.processStartRequests(spider.getStartUrls(), spider);
            spiderMWs.processSpiderInput(spider.getResponse(), spider);


            spiderMWs.processSpiderOutput(spider.getResponse(), new ArrayList<>(), spider);
        });*/

    }
}
