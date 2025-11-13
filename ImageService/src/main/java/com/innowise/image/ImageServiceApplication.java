package com.innowise.image;

import com.innowise.image.config.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties(S3Properties.class)
@ComponentScan(basePackages = {
        "com.innowise.image",
        "com.innowise.image.config",
        "com.innowise.image.security",
        "com.innowise.common"
})
public class ImageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageServiceApplication.class, args);
    }
}
