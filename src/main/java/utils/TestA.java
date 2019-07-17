package utils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.parsetools.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/16 9:31
 * @Description:
 */

public class TestA {

    private Vertx vertx = Vertx.vertx();

    @Test
    public void futures_chaining() throws Exception {

        Future<String> res = firstOperation()
            .compose(outcome -> {
                System.out.println(outcome);
                return secondOperation();
            })
            .compose(outcome -> {
                System.out.println(outcome);
                return Future.future(handle -> handle.complete("hahaha"));
            });
        System.out.println("first");
        while (true);
    }

    private Future<String> firstOperation() {
        Future<String> future = Future.future();

        vertx.setTimer(1000, delay -> future.complete("First Operation Complete"));

        return future;
    }

    private Future<String> secondOperation() {
        Future<String> future = Future.future();

        vertx.setTimer(1000, delay -> future.complete("Second Operation Complete"));

        return future;
    }

    @Test
    public void test2() {
        vertx.executeBlocking(future -> {
            // Call some blocking API that takes a significant amount of time to return
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.complete("hehe");
        }, res -> {
            if (res.succeeded()) {
                System.out.println("The result is: " + res.result());
            } else {
                System.out.println("oh no: " + res.cause().getMessage());
            }
        });
        System.out.println("zaza");
        while (true);
    }

    @Test
    public void test3() {
        EventBus eb = vertx.eventBus();
        eb.consumer("news.uk.sport", message -> {
            System.out.println("I have received a message: " + message.body());
        });
        eb.send("news.uk.sport", "Yay! Someone kicked a ball");
        while (true);
    }

    @Test
    public void test4() {
        JsonParser parser = JsonParser.newParser();

        parser.handler(event -> {
            // Start the object
            switch (event.type()) {
                case START_OBJECT:
                    // Set object value mode to handle each entry, from now on the parser won't emit start object events
                    parser.objectValueMode();
                    break;
                case VALUE:
                    // Handle each object
                    // Get the field in which this object was parsed
                    String id = event.fieldName();
                    System.out.println("User with id " + id + " : " + event.value());
                    break;
                case END_OBJECT:
                    // Set the object event mode so the parser emits start/end object events again
                    parser.objectEventMode();
                    break;
            }
        });

        parser.handle(Buffer.buffer("[0,1,2,3,4]"));
        parser.end();
    }
}
