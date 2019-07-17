package spider;


import annotation.spider.Parser;
import bean.Crawler;
import bean.Spider;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpiderFactory {

    private static Logger LOGGER  = LoggerFactory.getLogger(SpiderFactory.class);
    private List<Spider> spiders = new ArrayList<>();
    private static SpiderFactory INSTANCE;

    private SpiderFactory() {}

    public static SpiderFactory instance() {
        if (INSTANCE == null) {
            synchronized (SpiderFactory.class) {
                if (INSTANCE == null) {
                    SpiderFactory pipelineFactory = new SpiderFactory();
                    pipelineFactory.init();
                    INSTANCE = pipelineFactory;
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        loadSpiders(Crawler.getReflection());
    }

    /**
     * load spider
     *
     * @param reflections
     */
    private void loadSpiders(Reflections reflections) {
        reflections.getTypesAnnotatedWith(annotation.spider.Spider.class).forEach(clazz -> {
            Spider spider = new Spider();
            annotation.spider.Spider spiderAnt = clazz.getAnnotation(annotation.spider.Spider.class);
            String spiderName = spiderAnt.name();
            String[] allowedDomains = spiderAnt.allowedDomains();
            String[] startUrls = spiderAnt.startUrls();
            String cron = spiderAnt.cron();
            //get all parse() methods
            Map<Method, Class<?>> methods = Arrays.stream(clazz.getDeclaredMethods()).filter(s -> s.getAnnotation(Parser.class) != null)
                    .collect(Collectors.toMap(e -> e, e -> {
                        Type gt = e.getGenericReturnType();
                        if (gt != null) {
                            ParameterizedType pt = (ParameterizedType) gt;
                            if (pt.getActualTypeArguments().length > 0) {
                                return (Class) pt.getActualTypeArguments()[0];
                            }
                            return e.getReturnType();
                        }
                        return null;
                    }));
            spider.setName(spiderName);
            spider.setAllowedDomains(Arrays.asList(allowedDomains));
            spider.setStartUrls(Arrays.asList(startUrls));
            spider.setMethods(methods);
            spider.setCron(cron);
            try {
                spider.setInstance(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            spiders.add(spider);
        });
    }

    public List<Spider> getSpiders() {
        return spiders;
    }



}
