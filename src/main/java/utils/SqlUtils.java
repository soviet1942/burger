package utils;

import annotation.mysql.Column;
import annotation.mysql.Table;
import bean.Crawler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import controller.Server;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/12 15:14
 * @Description: mysql工具类
 */
public class SqlUtils {

    private static JDBCClient CLIENT;
    private static Logger LOGGER = LoggerFactory.getLogger(SqlUtils.class);
    private static String PASSWORD;
    private static String URL;
    private static String USERNAME;

    static {
        JSONObject conf = Crawler.instance().configs();
        if ((conf = conf.getJSONObject("mysql")) != null) {
            CLIENT = init(conf);
        }
    }

    /**
     * 获取连接池
     *
     * @param conf {"url": "", "username": "", "password": ""}
     * @return
     */
    public static JDBCClient init(JSONObject conf) {
        JDBCClient jdbcClient = null;
        try {
            URL = Optional.ofNullable(conf.getString("url")).orElseThrow(() -> new IllegalAccessException("数据库url参数为空"));
            USERNAME = Optional.ofNullable(conf.getString("username")).orElseThrow(() -> new IllegalArgumentException("数据库user参数为空"));
            PASSWORD = Optional.ofNullable(conf.getString("password")).orElse("");

            JsonObject dbConfig = new JsonObject();
            dbConfig.put("url", URL);
            dbConfig.put("driver_class", "com.mysql.jdbc.Driver");
            dbConfig.put("user", USERNAME);
            dbConfig.put("password", PASSWORD);
            jdbcClient = JDBCClient.createShared(Server.getVertx(), dbConfig);
            return jdbcClient;
        } catch (IllegalAccessException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return jdbcClient;
    }

    public static JDBCClient getClient() {
        return CLIENT;
    }

    /**
     * 验证表字段映射关系
     *
     * @param tableName
     * @param columnNameAndType 数据库表中 字段名称及字段类型名称（java对象类型）
     * @return
     */
    public static boolean verifyColumn(String tableName, Map<String, String> columnNameAndType) {
        Map<String, String> checkMap = getTableStructInfo(tableName);
        columnNameAndType.entrySet().forEach(entry -> {
            String columnName = entry.getKey();
            String fieldType = entry.getValue();
            String columnType;
            if ((columnType = checkMap.get(columnName)) == null) {
                throw new IllegalArgumentException("cannot find column `" + columnName + "` in table [" + tableName + "], please check @Column if property exists");
            }

            if (!sqlType2JavaType(columnType).equalsIgnoreCase(fieldType)) {
                throw new IllegalArgumentException("type mismatch field type `" + fieldType + "` in column `" + columnName + "` from table [" + tableName + "], " + " the actual should be [" + columnType + "]");
            }

        });
        return true;
    }

    /**
     * 获取表字段名和字段类型
     *
     * @param tableName
     * @return
     */
    public static Map<String, String> getTableStructInfo(String tableName) {
        String regex = "jdbc:mysql://.+?:\\d+/([a-zA-Z0-9_-]+?)\\?.*";
        String databaseName = Optional.ofNullable(URL).map(Pattern.compile(regex)::matcher).filter(Matcher::matches)
                .map(e -> e.group(1)).orElseThrow(() -> new IllegalArgumentException("failed to extract database name by regex: " + regex + ", url: " + URL));
        String sql = "SELECT `COLUMN_NAME` ,`DATA_TYPE` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s'";
        sql = String.format(sql, databaseName, tableName);
        Map<String, String> columnNameTypeMap = new HashMap<>();
        getClient().query(sql, ar -> {
            if (ar.succeeded()) {
                ResultSet resultSet = ar.result();
                JsonArray jsonArray = resultSet.getOutput();
                columnNameTypeMap.put(jsonArray.getString(0), jsonArray.getString(1));
            }
        });
        if (columnNameTypeMap.size() == 0) {
            throw new IllegalArgumentException("table [" + tableName + "] not exists");
        }
        return columnNameTypeMap;
    }

    /**
     * 数据库字段类型转java对象名称
     *
     * @param sqlType
     * @return
     */
    public static String sqlType2JavaType(String sqlType) {

        switch (sqlType.toLowerCase()) {
            case "bit":
                return "Boolean";
            case "tinyint":
                return "Byte";
            case "smallint":
                return "Short";
            case "int":
                return "Integer";
            case "bigint":
                return "Long";
            case "float":
                return "Float";
            case "decimal":
            case "numeric":
            case "real":
            case "money":
            case "smallmoney":
                return "Double";
            case "datetime":
            case "date":
                return "Date"; //DATE '1972-01-01'
            case "image":
                return "Blob";
            case "timestamp": //'1972-01-01 00:00:01'
                return "Timestamp";
            default:
                return "String";
        }
    }


    /**
     * 验证持久类和sql表及字段之间的映射
     *
     * @return
     */
    public static boolean verifyAllTables() {
        AtomicBoolean res = new AtomicBoolean(false);
        Reflections reflections = Crawler.getReflection();
        Set<Class<?>> tableClasses = reflections.getTypesAnnotatedWith(Table.class);
        tableClasses.forEach(clazz -> {
            Table table = clazz.getAnnotation(Table.class);
            if (StringUtils.isNotEmpty(table.value())) {
                Map<String, String> columnNameTypeMap = new HashMap<>();
                Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && column.insertable() == true) {
                        String columnType = column.columnDefinition() != null ? sqlType2JavaType(column.columnDefinition()) : field.getType().getSimpleName();
                        String columnName = column.name();
                        columnNameTypeMap.put(columnName, columnType);
                    }
                });
                res.set(verifyColumn(table.value(), columnNameTypeMap));
            }
        });
        return res.get();
    }

}
