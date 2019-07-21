package utils;

import annotation.mysql.Column;
import annotation.mysql.Table;
import bean.Crawler;
import com.alibaba.fastjson.JSONObject;
import core.Engine;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    private static BasicDataSource DS;
    private static boolean INITIALIZED;
    private static Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private static String PASSWORD;
    private static String URL;
    private static String USERNAME;

    static {
        JSONObject conf = Crawler.instance().configs();
        if ((conf = conf.getJSONObject("mysql")) != null) {
            DS = init(conf);
        }
        INITIALIZED = true;
    }

    /**
     * 增删改
     *
     * @param sql
     * @return
     */
    public static boolean execute(String sql) {
        Connection conn = getConn();
        Statement stat = getStat(conn);
        boolean res = false;
        try {
            res = stat.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
            } catch (SQLException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return res;
    }

    public static Connection getConn(BasicDataSource ds) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 获取连接池
     *
     * @param conf {"url": "", "username": "", "password": ""}
     * @return
     */
    public static BasicDataSource init(JSONObject conf) {
        BasicDataSource ds = new BasicDataSource();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            URL = Optional.ofNullable(conf.getString("url")).orElseThrow(() -> new IllegalAccessException("数据库url参数为空"));
            USERNAME = Optional.ofNullable(conf.getString("username")).orElseThrow(() -> new IllegalArgumentException("数据库user参数为空"));
            PASSWORD = Optional.ofNullable(conf.getString("password")).orElse("");
            ds.setUrl(URL);
            ds.setUsername(USERNAME);
            ds.setPassword(PASSWORD);
            ds.setMinIdle(Optional.ofNullable(conf.getString("minIdle")).map(Integer::parseInt).orElse(5));
            ds.setMaxIdle(Optional.ofNullable(conf.getString("maxIdle")).map(Integer::parseInt).orElse(10));
            ds.setMaxOpenPreparedStatements(Optional.ofNullable(conf.getString("maxStatements")).map(Integer::parseInt).orElse(100));
        } catch (ClassNotFoundException | IllegalAccessException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return ds;
    }

    public static boolean isInitialize() {
        return INITIALIZED;
    }

    public static void main(String[] args) {

        boolean res = verifyColumn("article", new HashMap<String, String>() {{
            put("cover", "varchar");
            put("isOriginal", "smallint");
            put("publish_time", "datetime");
            put("id", "int");
            put("title", "varchar");
            put("cont1ent", "fff");
        }});
        System.out.println(res);
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
            String columnJavaType = sqlType2JavaType(columnType);
            if (!columnJavaType.equalsIgnoreCase(fieldType)) {
                throw new IllegalArgumentException("wrong return type [" + fieldType + "] mapping column [" + tableName + "." + columnName + "] " + ",the actual type is [" + columnJavaType + "]");
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
        executeQuery(sql, (resultSet) -> {
            while (resultSet.next()) {
                String columnName = resultSet.getString(1);
                String columnType = resultSet.getString(2);
                columnNameTypeMap.put(columnName, columnType);
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
     * 查询
     *
     * @param sql
     * @param resultSet 拿到查询结果，后续操作
     */
    public static void executeQuery(String sql, ExecuteQuery resultSet) {
        Connection conn = getConn();
        Statement stat = getStat(conn);
        try {
            ResultSet resSet = stat.executeQuery(sql);
            resultSet.exec(resSet);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
            } catch (SQLException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public static Connection getConn() {
        Connection conn = null;
        try {
            conn = DS.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static Statement getStat(Connection conn) {
        Statement stat = null;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return stat;
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
                        String columnName = column.name();
                        String columnType = StringUtils.isNotEmpty(column.columnDefinition())
                                ? sqlType2JavaType(column.columnDefinition()) : field.getType().getSimpleName();
                        columnNameTypeMap.put(columnName, columnType);
                    }
                });
                res.set(verifyColumn(table.value(), columnNameTypeMap));
            }
        });
        return res.get();
    }

    interface Execute {
        void exec(boolean success) throws SQLException;
    }

    interface ExecuteQuery {
        void exec(ResultSet resultSet) throws SQLException;
    }

}
