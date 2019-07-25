import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class Download {
    private static Logger LOGGER = LoggerFactory.getLogger(Download.class);
    private static String URL = "https://s3.pstatp.com/toutiao/xigua_video_web_pc/static/js/index.517ce9e3.chunk.js";
    private static Integer TOTAL = 500;
    private static Integer ACTUAL = (int) Math.round(TOTAL * 0.95);
    //https://s3.pstatp.com/toutiao/xigua_video_web_pc/static/js/index.517ce9e3.chunk.js 1.4M
    //https://s3.pstatp.com/toutiao/xigua_video_web_pc/static/js/vendors~byted-player-xgpc.2555ab1c.chunk.js 127k
    //https://www.ixigua.com/i6696739609893667335/ 200k
    //https://mp.weixin.qq.com/s?timestamp=1563959776&src=3&ver=1&signature=a3Y*0gZyUrLefMzRWIuAeS3KjsjnqbwTCP4uHBra9xWzERF0fxN4HPxQ43kfaFLlKrIwt-gmV9vAkbKpL5eITjfSYqOn04tHeGqSqaFpZGBKM2W3lFqwXLBZ9flMdG7tVcYEpwZkKnfr7qWBRZaimhANIz6uziTTXj5*uIJE9ok= 350k
    //http://quotes.toscrape.com/page/2/ 15k
    //http://httpbin.org/headers  504b

    @Test
    public void testHttpClient() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        AtomicInteger total = new AtomicInteger(1);
        Long start = System.currentTimeMillis();
        for (int i = 1; i <= TOTAL; i++) {
            int finalI = i;
            threadPoolExecutor.execute(() -> {
                try {
                    int status = Jsoup.connect(URL + "?i=" + System.currentTimeMillis()).ignoreContentType(true)
                            .proxy("123.163.21.12", 3128)
                            .execute().statusCode();
                    LOGGER.info(status + "---" + finalI + "---");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (total.getAndIncrement() == ACTUAL) {
                        LOGGER.info("=========================" + (System.currentTimeMillis() - start));
                    }
                }
            });
        }
        while (true) ;
    }


    @Test
    public void testVertx() throws Exception {
        Vertx vertx = Vertx.vertx(new VertxOptions() {{
            /*setWorkerPoolSize(1);
            setEventLoopPoolSize(1);
            setQuorumSize(1);
            setInternalBlockingPoolSize(1);*/
            //setEventLoopPoolSize(15);
        }});
        WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(30000)
                .setMaxPoolSize(2000)
                .setIdleTimeout(60)
                .setHttp2MaxPoolSize(1000);

        Long start = System.currentTimeMillis();
        AtomicInteger total = new AtomicInteger(1);
        Set<String> set = new HashSet<>();
        WebClient client = WebClient.create(vertx, options);
        for (int i = 1; i <= TOTAL; i++) {
            /*Proxy proxy = nextProxy();
            options.setProxyOptions(new ProxyOptions() {{
                setHost(proxy.getIp());
                setPort(proxy.getPort());
            }});*/
            int finalI = i;
            client.getAbs(URL).send(rq -> {
                if (rq.succeeded()) {
                    int status = rq.result().statusCode();
                    set.add(Thread.currentThread().getName());
                    LOGGER.info(status + "---" + finalI + "---");
                } else {
                    rq.cause().printStackTrace();
                }
                if (total.getAndIncrement() == ACTUAL) {
                    LOGGER.info("====================" + (System.currentTimeMillis() - start) + "---" + set.size());
                }
            });
        }
        while (true) ;
    }


    @Test
    public void testAsynch() throws IOException {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        AtomicInteger total = new AtomicInteger(1);
        Long start = System.currentTimeMillis();
        for (int i=1; i<=TOTAL; i++) {
            int finalI = i;
            asyncHttpClient
                    .prepareGet(URL)
                    .execute()
                    .toCompletableFuture()
                    .thenApply(response -> {
                        int status = response.getStatusCode();
                        LOGGER.info(status + "---" + finalI + "---");
                        if (total.getAndIncrement() == ACTUAL) {
                            LOGGER.info("=========================" + (System.currentTimeMillis() - start));
                        }
                        return response;
                    });
        }
        while (true);
    }

    @Test
    public void testOkHttp() throws IOException {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        AtomicInteger total = new AtomicInteger(1);
        Long start = System.currentTimeMillis();
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(URL)
                .build();
        for (int i = 1; i <= TOTAL; i++) {
            int finalI = i;
            threadPoolExecutor.execute(() -> {
                try {
                    int status = httpClient.newCall(request).execute().code();
                    LOGGER.info(status + "---" + finalI + "---");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (total.getAndIncrement() == ACTUAL) {
                        LOGGER.info("=========================" + (System.currentTimeMillis() - start));
                    }
                }
            });
        }
        while (true) ;
    }


    /**
     * 获取当前cpu使用率
     *
     * @return
     */
    public static double getProcessCpuLoad() {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        double res = 0f;
        try {
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});
            if (list.isEmpty()) return Double.NaN;

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            if (value == -1.0) return Double.NaN;
            res = ((int) (value * 1000) / 10.0);
        } catch (MalformedObjectNameException | InstanceNotFoundException | ReflectionException e) {
            e.printStackTrace();
        }
        return res;
    }


    Queue<Proxy> PROXYLIST = new LinkedBlockingQueue<>();

    public Proxy nextProxy() throws IOException {
        if (PROXYLIST.size() == 0) {
            getJddProxy().forEach(PROXYLIST::add);
        }
        return PROXYLIST.poll();
    }

    public List<Proxy> getJddProxy() throws IOException {
        String jsonStr = Jsoup.connect("https://data2c.jdddata.com/dac/proxy/findProxy").ignoreContentType(true)
                .header("Content-Type", "application/json")
                .requestBody("{\"size\":10}")
                .method(Connection.Method.POST)
                .execute().body();
        List<Proxy> list = new ArrayList<>();
        JSONArray jsonArray = JSON.parseObject(jsonStr).getJSONArray("data");
        for (Object jsonObject : jsonArray) {
            JSONObject obj = (JSONObject) jsonObject;
            String ip = obj.getString("ip");
            String port = obj.getString("port");
            list.add(new Proxy(ip, Integer.parseInt(port)));
        }
        return list;
    }

    class Proxy {
        String ip;
        Integer port;

        public Proxy(String ip, Integer port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }

    @Test
    public void prox() throws IOException {
        String result = Jsoup.connect("http://httpbin.org/ip")
                .ignoreContentType(true)
                .proxy("123.55.3.47", 3128)
                .get().text();
        System.out.println(result);
    }

}
