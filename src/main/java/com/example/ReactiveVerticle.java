package com.example;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.Session;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.CorsHandler;
import io.vertx.rxjava3.ext.web.handler.SessionHandler;
import io.vertx.rxjava3.ext.web.sstore.ClusteredSessionStore;
import io.vertx.rxjava3.ext.web.sstore.SessionStore;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class ReactiveVerticle extends AbstractVerticle {
    private static final int HTTP_PORT = 4000;
//    private static final Logger logger = LoggerFactory.getLogger(TestVerticle.class);

    WebClient webClient;


    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        Router router = Router.router(vertx);

        SessionStore store = ClusteredSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("Authorization");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.PUT);

        router.route().handler(CorsHandler
                .create()
                .allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods));

        router.route().handler(sessionHandler);

        BodyHandler bodyHandler = BodyHandler.create();
        router.post().handler(bodyHandler);
        router.put().handler(bodyHandler);

        router.get("/test").handler(this::test);

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(HTTP_PORT)
                .ignoreElement();
    }

    private void test(RoutingContext ctx) {
        Session session = ctx.session();
        String email = session.get("email");

        webClient
                .get("kwabremanu.free.beeceptor.com", "/")
                .rxSend()
                .subscribe(
                        response -> {
//                            System.out.println("Email is: " + email);
                            ctx.response().end("Email is: " + email);
                        },
                        error -> System.out.println("Something went wrong " + error.getMessage())
                );
    }

    public static void main(String... args) throws UnknownHostException {
        String ipv4 = InetAddress.getLocalHost().getHostAddress();
        VertxOptions options = new VertxOptions()
                .setEventBusOptions(new EventBusOptions()
                        .setHost(ipv4)
                        .setClusterPublicHost(ipv4));

//        Vertx vertx = Vertx.vertx();
//
//        vertx.rxDeployVerticle(new ReactiveVerticle())
//                .subscribe(
//                        ok -> System.out.println("HTTP server started on port " + HTTP_PORT),
//                        err -> System.out.println("Eiish " + err)
//                );

        Vertx.clusteredVertx(options)
                .subscribe(
                        vertx -> vertx.rxDeployVerticle(new ReactiveVerticle()),
                        err -> System.out.println("Cluster error" + err)
                );

    }
}
