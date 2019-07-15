package controller;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Server {

    private static final Vertx vertx = Vertx.vertx();

    public static Vertx getVertx() {
        return vertx;
    }

    @Test
    public void test() throws IOException {
        /*String res = Jsoup.connect("https://www.toutiao.com/api/pc/hot_gallery/?widen=1").ignoreContentType(true).get().text();
        System.out.println(res);*/
        for (int i=0; i<15; i++) {
            int code = Jsoup.connect("https://www.motorcycle.com/").execute().statusCode();
            System.out.println(code);
        }
    }

    @Test
    public void test1() throws URISyntaxException {
        URI uri = new URI("http://httpbin.org/headers");
        WebClient client = WebClient.create(Server.getVertx(), new WebClientOptions() {{
            setConnectTimeout(30000);
            setMaxPoolSize(1);
            setDefaultHost(uri.getHost());
        }});
        HttpRequest<Buffer> httpRequest = client.get(uri.getRawPath());
        /*httpRequest.as(BodyCodec.string()).send(ar -> {
            if (ar.succeeded()) {
                HttpResponse resp = ar.result();
                System.out.println(resp.statusCode() + "==========================>" + Thread.currentThread().getName());
            } else {
                ar.cause().printStackTrace();
            }
        });*/
        while (true);
    }

    public static void main(String[] args) {

        WebClientOptions webClientOptions = new WebClientOptions()
                .setTrustAll(true)
                .setConnectTimeout(30000)
                .setIdleTimeout(10)
                .setMaxPoolSize(1)
                .setFollowRedirects(true)
                .setDefaultHost(".*");
                /*.setProxyOptions(new ProxyOptions() {{
                    setHost("");
                    setPort(1);
                }});*/

        WebClient client = WebClient.create(getVertx(), webClientOptions);

        for (int i=210000; i<210001; i++) {

            client.get("https://www.transfermarkt.com/georges-griffiths/profil/spieler/" + i)
                    .putHeader("Accept", "*/*")
                    .putHeader("Cache-Control", "no-cache")
                    .putHeader("accept-encoding", "gzip, deflate")
                    .putHeader("Connection", "keep-alive")
                    .putHeader("User-Agent", "fockdfsfsdffsdfssdsd" + System.currentTimeMillis())
                    .as(BodyCodec.string())
                    .send(ar -> {
                        if (ar.succeeded()) {
                            HttpResponse<String> response = ar.result();
                            System.out.println(response.statusCode() + "==========================>" + Thread.currentThread().getName());
                        } else {
                            ar.cause().printStackTrace();
                        }
                    });
        }

        while (true) {

        }
    }


}


