package com.example.ebank.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ebank.observability.RequestIdFilter;

@Configuration
public class FilterConfig {

  private static final Logger log = LogManager.getLogger(FilterConfig.class);

  @Bean
  public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
    log.info("requestIdFilter bean initialized");
    FilterRegistrationBean<RequestIdFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new RequestIdFilter());
    bean.setOrder(1); // できるだけ早く
    return bean;
  }
}
