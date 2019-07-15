package controller;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Server {

    @Test
    public void test() throws IOException {
        /*String res = Jsoup.connect("https://www.toutiao.com/api/pc/hot_gallery/?widen=1").ignoreContentType(true).get().text();
        System.out.println(res);*/
        for (int i=0; i<15; i++) {
            int code = Jsoup.connect("https://www.motorcycle.com/").execute().statusCode();
            System.out.println(code);
        }
    }

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        WebClientOptions webClientOptions = new WebClientOptions()
                .setTrustAll(true)
                .setConnectTimeout(30000)
                .setIdleTimeout(10)
                .setMaxPoolSize(1)
                .setFollowRedirects(true)
                .setDefaultHost("www.transfermarkt.com");
                /*.setProxyOptions(new ProxyOptions() {{
                    setHost("");
                    setPort(1);
                }});*/

        WebClient client = WebClient.create(vertx, webClientOptions);

        for (int i=210000; i<210015; i++) {
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


