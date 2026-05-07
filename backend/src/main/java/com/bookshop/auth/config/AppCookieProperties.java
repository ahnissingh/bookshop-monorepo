package com.bookshop.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.cookie")
public class AppCookieProperties {
    private boolean secure;
    private String domain;
    private String sameSite;
    private String path;
}