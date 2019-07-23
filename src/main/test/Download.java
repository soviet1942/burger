import controller.Server;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class Download {
    private static Logger LOGGER = LoggerFactory.getLogger(Download.class);
    private static String URL = "https://www.baidu.com/";

    @Test
    public void test() throws MalformedURLException, InterruptedException {
        Vertx vertx = Vertx.vertx(new VertxOptions() {{
            /*setWorkerPoolSize(1);
            setEventLoopPoolSize(1);
            setQuorumSize(1);
            setInternalBlockingPoolSize(1);*/
        }});
        WebClientOptions options = new WebClientOptions();
        options.setConnectTimeout(30000);
        options.setMaxPoolSize(2000);
        options.setIdleTimeout(60);
        options.setHttp2MaxPoolSize(12);
        vertx.createHttpClient(options);

        Long start = System.currentTimeMillis();
        AtomicInteger total = new AtomicInteger(1);
        for (int i=1; i<=500; i++) {
            WebClient client = WebClient.create(vertx, options);
            int finalI = i;
            client.getAbs(URL).send(rq -> {
                if (rq.succeeded()) {
                    if (total.getAndIncrement() == 485) {
                        System.out.println("====================" + (System.currentTimeMillis() - start));
                    }
                    LOGGER.info(rq.result().statusCode() + "-" + finalI);
                } else {
                    rq.cause().printStackTrace();
                }
            });
        }
        while (true) ;
    }

    @Test
    public void tset2() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        AtomicInteger total = new AtomicInteger(1);
        Long start = System.currentTimeMillis();
        for (int i=1; i<=500; i++) {
            int finalI = i;
            threadPoolExecutor.execute(() -> {
                try {
                    int status = Jsoup.connect(URL).ignoreContentType(true).execute().statusCode();
                    System.out.println(status + "---" + finalI);
                    if (total.getAndIncrement() == 485) {
                        System.out.println("=========================" + (System.currentTimeMillis() - start));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        while (true);
    }

}
