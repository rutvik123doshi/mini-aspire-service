package com.rutvik.interview.aspire.miniaspireservice.config;

import com.rutvik.interview.aspire.miniaspireservice.api.filter.RequestMetadataHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
//@ComponentScan(value = "com.rutvik.aspire")
public class RequestHandlerConfig implements WebMvcConfigurer {

    @Autowired
    private RequestMetadataHandler requestMetadataHandler;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestMetadataHandler);
    }

}
