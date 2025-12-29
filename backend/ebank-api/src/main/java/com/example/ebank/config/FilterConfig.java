package com.example.ebank.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ebank.observability.RequestIdFilter;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
    FilterRegistrationBean<RequestIdFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new RequestIdFilter());
    bean.setOrder(1); // できるだけ早く
    return bean;
  }
}
