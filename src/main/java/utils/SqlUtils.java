package utils;

import bean.Crawler;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.Optional;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/12 15:14
 * @Description:
 */
public class SqlUtils {

    private static Connection CONNECTION;
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            JSONObject conf = Crawler.instance().configs().getJSONObject("mysql");
            URL = Optional.ofNullable(conf.getString("url")).orElseThrow(() -> new IllegalAccessException("数据库url参数为空"));
            USER = Optional.ofNullable(conf.getString("user")).orElseThrow(() -> new IllegalArgumentException("数据库user参数为空"));
            PASSWORD = Optional.ofNullable(conf.getString("password")).orElse("");
        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Connection getSingleConn() {
        if (CONNECTION == null) {
            synchronized (SqlUtils.class) {
                try {
                    CONNECTION = DriverManager.getConnection(URL, USER, PASSWORD);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return CONNECTION;
    }

    public static Statement getStat(Connection conn) {
        Statement stat = null;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stat;
    }

    public static void executeQuery(String sql, ExecuteQuery resultSet) {
        Connection conn = getSingleConn();
        Statement stat = getStat(conn);
        try {
            ResultSet result = stat.executeQuery(sql);
            resultSet.exec(result);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void execute(String sql, Execute execution) {
        Connection conn = getSingleConn();
        Statement stat = getStat(conn);
        try {
            boolean success = stat.execute(sql);
            execution.exec(success);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean execute(String sql) {
        Connection conn = getSingleConn();
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
                e.printStackTrace();
            }
        }
        return res;
    }

    public static void main(String[] args) {
        String sql = "SELECT `COLUMN_NAME` ,`DATA_TYPE` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='motor' AND `TABLE_NAME`='toutiao_article'";
        executeQuery(sql, (resultSet) -> {
            while (resultSet.next()) {
                String columnName = resultSet.getString(1);
                String columnType = resultSet.getString(2);
                System.out.println(columnName + " " + columnType);
            }
        });
    }

    public static String sqlType2JavaType(String sqlType) {
        switch (sqlType.toLowerCase()){
            case "bit": return "boolean";
            case "tinyint": return "byte";
            case "smallint": return "short";
            case "int": return "int";
            case "bigint": return "long";
            case "float": return "float";
            case "decimal":
            case "numeric":
            case "real":
            case "money":
            case "smallmoney": return "double";
            case "datetime":
            case "date": return "Date";
            case "image": return "Blob";
            case "timestamp": return "Timestamp";
            default: return "String";
        }
    }


    interface ExecuteQuery {
        void exec(ResultSet resultSet) throws SQLException;
    }

    interface Execute {
        void exec(boolean success) throws SQLException;
    }

}
