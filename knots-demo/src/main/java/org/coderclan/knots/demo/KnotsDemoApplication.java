package org.coderclan.knots.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class KnotsDemoApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext c = SpringApplication.run(KnotsDemoApplication.class, args);
        Test test = c.getBean(Test.class);
        test.test();
    }
}
