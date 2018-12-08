package com.qh.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "qhpay", name = "spring-session-open", havingValue = "true")
public class SpringSessionConfig {

}
