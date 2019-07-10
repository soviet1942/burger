package core;

import annotation.middleware.Priority;
import bean.PriorityClass;
import bean.Request;
import bean.Response;
import bean.Spider;
import middleware.DownloaderMiddleware;
import middleware.SpiderMiddleware;
import org.jsoup.Jsoup;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 10:10
 * @Description:
 */
public class Engine {

    Logger logger = LoggerFactory.getLogger(Engine.class);
    private static List<DownloaderMiddleware> downloaderMWs = new ArrayList<>();
    private static List<SpiderMiddleware> spiderMWs = new ArrayList<>();

    private Engine() {}

    public void init() {
        //按照priority注解的值对class排序
        Reflections reflections = new Reflections("");
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


    public static void main(String[] args) {
        new Engine().init();
        Spider spider = new Spider();

        downloaderMWs.forEach(downloadMW -> {
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
        });

    }
}
