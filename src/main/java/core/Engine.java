package core;

import annotation.mysql.Column;
import annotation.mysql.Table;
import annotation.spider.Parser;
import bean.*;
import middleware.MiddlewareFactory;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipeline.PipelineFactory;
import spider.SpiderFactory;
import utils.SqlUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 10:10
 * @Description:
 */
public class Engine {

    private static Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private static Engine INSTANCE;
    /** middleware (downloader + spider) */
    private static MiddlewareFactory MIDDLEWARE_FACTORY;
    /** spider (handle download result) */
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


    public static void main(String[] args) {

        Engine engine = Engine.instance();

        for (Spider spider : SPIDER_FACTORY.getSpiders()) {

            String startUrl = spider.getStartUrls().get(0);

            Request request = new Request(Jsoup.connect(startUrl));
            Response response = new Response();
            List<Object> res = new ArrayList<>();

            try {
                MIDDLEWARE_FACTORY.exeProcessRequest(request, spider);
                response.setResponse(request.getConnection().ignoreContentType(true).execute());
                MIDDLEWARE_FACTORY.exeProcessResponse(request, response, spider);
            } catch (Exception e) {
                e.printStackTrace();
                MIDDLEWARE_FACTORY.exeProcessException(request, e, spider);
            }

            try {
                MIDDLEWARE_FACTORY.exeProcessSpiderInput(response, spider);
                MIDDLEWARE_FACTORY.exeProcessSpiderOutput(response, res, spider);
            } catch (Exception e) {
                e.printStackTrace();
                MIDDLEWARE_FACTORY.exeProcessSpiderException(response, e, spider);
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
