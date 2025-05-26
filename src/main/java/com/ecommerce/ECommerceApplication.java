package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//DataSourceAutoConfiguration.class,
@EnableJpaAuditing
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.class})
public class ECommerceApplication {

    public static void main(String[] args) {

        SpringApplication.run(ECommerceApplication.class, args);

        System.out.println("Hello World project is working fine");
    }

}