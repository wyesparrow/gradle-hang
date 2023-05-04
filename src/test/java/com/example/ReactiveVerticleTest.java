package com.example;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Session;
import mockit.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;

@ExtendWith(VertxExtension.class)
public class ReactiveVerticleTest {
    private static RequestSpecification requestSpecification;

    @Capturing
    Session session;

    @BeforeAll
    static void prepareSpec(Vertx vertx, VertxTestContext testContext) {
        requestSpecification = new RequestSpecBuilder()
                .addFilters(asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))
                .setBaseUri("http://localhost:4000/")
//                .setBasePath("/api/v1")
                .build();


        vertx.rxDeployVerticle(new ReactiveVerticle())
                .ignoreElement()
                .subscribe(testContext::completeNow, testContext::failNow);
    }

    @AfterAll
    static void end(VertxTestContext testContext) {
        testContext.completeNow();
    }

    @Test
    void testTest(Vertx vertx, VertxTestContext testContext) {

        new Expectations() {{
            session.get("email"); result = "nyarkofranklin@ymail.com";
//            session.get("firstname"); result = registrations.get("Foo").getString("firstName");
        }};

        given(requestSpecification)
//                .contentType(ContentType.JSON)
//                .body(registration.encode())
                .get("/test")
                .then()
                .assertThat()
                .statusCode(200);

        new Verifications() {{
            session.get("email"); minTimes = 1;
//            session.get("firstName"); minTimes = 1;
        }};

        testContext.completeNow();
    }
}
