package core.engine.bean;

import org.jsoup.Connection;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:13
 * @Description:
 */
public class Request {

    private Connection connection;

    private int retryCount = 0;

    public Request(Connection connection) {
        this.connection = connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
