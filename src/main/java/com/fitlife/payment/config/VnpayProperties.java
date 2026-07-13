package com.fitlife.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vnpay")
public class VnpayProperties {

    private String payUrl;

    private String returnUrl;

    private String ipnUrl;

    private String tmnCode;

    private String hashSecret;

    private String version;

    private String command;

    private String orderType;

    private String locale;

    private String frontendResultUrl;
}