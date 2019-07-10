package core.engine.processor;

import core.engine.annotation.middleware.Priority;
import core.engine.bean.PriorityClass;
import core.engine.bean.Request;
import core.engine.bean.Response;
import core.engine.bean.Spider;
import core.middleware.DownloaderMiddleware;
import core.middleware.SpiderMiddleware;
import org.jsoup.Jsoup;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 10:10
 * @Description:
 */
public class Reflection {

    public static void main(String[] args) {
        List<DownloaderMiddleware> downloaderMWs = new ArrayList<>();
        List<SpiderMiddleware> spiderMWs = new ArrayList<>();

        //按照priority注解的值对class排序
        Reflections reflections = new Reflections("burger");
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


        downloaderMWs.forEach(downloadMW -> {
            try {
                Request request = new Request(Jsoup.connect("http://httpbin.org/ip"));
                Spider spider = new Spider();
                //before download
                downloadMW.processRequest(request, spider);


                Response response = new Response(request.getConnection().ignoreContentType(true).get());
                //after download
                downloadMW.processResponse(request, response, spider);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
