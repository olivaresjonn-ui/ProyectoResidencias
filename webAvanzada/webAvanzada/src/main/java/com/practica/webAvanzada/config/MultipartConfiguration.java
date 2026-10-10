package com.practica.webAvanzada.config;

import jakarta.servlet.MultipartConfigElement;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class MultipartConfiguration {

    @Bean(name = "multipartResolver")
    @ConditionalOnMissingBean(MultipartResolver.class)
    StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    @ConditionalOnMissingBean(MultipartConfigElement.class)
    MultipartConfigElement multipartConfigElement(
            @Value("${spring.servlet.multipart.location:}") String location,
            @Value("${spring.servlet.multipart.max-file-size:10MB}") DataSize maxFileSize,
            @Value("${spring.servlet.multipart.max-request-size:11MB}") DataSize maxRequestSize) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(location);
        factory.setMaxFileSize(maxFileSize);
        factory.setMaxRequestSize(maxRequestSize);
        return factory.createMultipartConfig();
    }
}
