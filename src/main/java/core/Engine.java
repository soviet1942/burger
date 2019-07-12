package core;

import annotation.middleware.Priority;
import annotation.mysql.Column;
import annotation.mysql.Table;
import annotation.spider.Parser;
import bean.*;
import middleware.DownloaderMiddleware;
import middleware.SpiderMiddleware;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 10:10
 * @Description:
 */
public class Engine {

    private static Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private List<DownloaderMiddleware> downloaderMWs = new ArrayList<>();
    private List<SpiderMiddleware> spiderMWs = new ArrayList<>();
    private List<Spider> spiders = new ArrayList<>();
    private static Engine INSTANCE;

    private Engine() {
    }

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
            try {
                spider.setInstance(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error(ExceptionUtils.getMessage(e.getCause()));
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
        engine.getDownloaderMWs().forEach(e -> e.fromCrawler(crawler));
        engine.getSpiderMWs().forEach(e -> e.fromCrawler(crawler));
    }

    public static void main(String[] args) {
        Engine engine = Engine.instance();
        engine.initMiddleware(engine);
        for (Spider spider : engine.getSpiders()) {

            String startUrl = spider.getStartUrls().get(0);

            Request request = new Request(Jsoup.connect(startUrl));
            Response response = new Response();
            List<Object> res = new ArrayList<>();

            try {
                engine.exeProcessRequest(request, spider);
                response.setResponse(request.getConnection().ignoreContentType(true).execute());
                engine.exeProcessResponse(request, response, spider);
            } catch (Exception e) {
                e.printStackTrace();
                engine.exeProcessException(request, e, spider);
            }

            try {
                engine.exeProcessSpiderInput(response, spider);
                engine.exeProcessSpiderOutput(response, res, spider);
            } catch (Exception e) {
                e.printStackTrace();
                engine.exeProcessSpiderException(response, e, spider);
            }

            for (Map.Entry<Method, Class<?>> mEntry : spider.getMethods().entrySet()) {
                Method method = mEntry.getKey();
                Class<?> returnType = mEntry.getValue();
                if (method.getAnnotation(Parser.class) != null) {
                    try {
                        Object result = method.invoke(spider.getInstance(), response);
                        List<Object> data;
                        if (returnType != null) {
                            if (method.getReturnType() == List.class) {
                                data = (List) result;
                            } else {
                                data = new ArrayList<Object>() {{ add(result); }};
                            }
                            engine.persist(data, returnType);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public void persist(List<Object> data, Class<?> clazz) {
        //sql前缀
        Table table = clazz.getAnnotation(Table.class);
        if (table == null && StringUtils.isNotEmpty(table.value())) {
            return;
        }
        StringBuilder sql = new StringBuilder("INSERT IGNORE INTO `").append(table.value()).append("` (");
        List<Column> columns = Arrays.stream(clazz.getAnnotationsByType(Column.class)).filter(Column::insertable)
                .filter(c -> c.name() != null).collect(Collectors.toList());
        for (Column column : columns) {
            sql.append("`").append(column.name()).append("`,");
        }
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append(") VLAUES (");

        //判断字段类型
        Arrays.stream(clazz.getFields()).forEach(field -> {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                String fieldName = field.getName();
                String fieldValue = null;

            }
        });

        //读取bean，sql赋值
        data.forEach(e -> {
            Arrays.stream(e.getClass().getFields()).forEach(field -> {
                String fieldName = field.getName();
                Class fieldType = field.getType();
                if (fieldType == int.class || fieldType == Integer.class) {

                } else if (fieldType == char.class || fieldType == String.class) {

                } else if (fieldType == double.class || fieldType == Double.class) {

                }
            });
        });
        System.out.println(data.toArray());
        System.out.println(clazz.getName());
    }


}
