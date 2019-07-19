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
                try {
                    //start parse
                    parse(response, spider);
                } catch (Exception e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e.getCause()));
                    MIDDLEWARE_FACTORY.exeProcessSpiderException(response, e.getCause(), spider);
                }
            } else {
                //download failed
                MIDDLEWARE_FACTORY.exeProcessException(request, ar.cause(), spider);
            }
        });

    }


    public void parse(Response response, Spider spider) {
        //before parse
        MIDDLEWARE_FACTORY.exeProcessSpiderInput(response, spider);
        for (Map.Entry<Method, Class<?>> mEntry : spider.getMethods().entrySet()) {
            Method method = mEntry.getKey();
            Class<?> returnType = mEntry.getValue();
            if (method.getAnnotation(Parser.class) != null) {
                try {
                    //parsing
                    Object result = method.invoke(spider.getInstance(), response);
                    //after parse
                    MIDDLEWARE_FACTORY.exeProcessSpiderOutput(response, new ArrayList<>(), spider);
                    List<Object> data;
                    if (returnType != null) {
                        if (method.getReturnType() == List.class) {
                            data = (List) result;
                        } else {
                            data = new ArrayList<Object>() {{ add(result); }};
                        }
                        persist(data, returnType);
                    }
                } catch (Exception e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e.getCause()));
                }
            }
        }
    }


    public void persist(List<Object> data, Class<?> clazz) {
        //校验
        SqlUtils.verifyAllTables();
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
        for (Field field : clazz.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                String fieldName = field.getName();
                try {
                    Object value = field.get(clazz);
                    Class<?> fieldType = field.getType();
                    if (fieldType == String.class) {

                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }

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
