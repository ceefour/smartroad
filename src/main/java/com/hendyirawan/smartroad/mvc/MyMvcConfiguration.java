package com.hendyirawan.smartroad.mvc;

import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan(basePackageClasses = MyMvcConfiguration.class)
@EnableTransactionManagement
@EnableWebMvc
@EnableHypermediaSupport(type=HypermediaType.HAL)
@Profile("daemon")
//@EnableSwagger
public class MyMvcConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        // re-enable Spring Boot's static content
        registry.addResourceHandler("/**").addResourceLocations(
                "classpath:/static/");
        // common: WebJars - http://spring.io/blog/2014/01/03/utilizing-webjars-in-spring-boot
        if (!registry.hasMappingForPattern("/webjars/**")) {
            registry.addResourceHandler("/webjars/**").addResourceLocations(
                    "classpath:/META-INF/resources/webjars/");
        }
//        if (!registry.hasMappingForPattern("/**")) {
//            registry.addResourceHandler("/**").addResourceLocations(
//                    RESOURCE_LOCATIONS);
//        }
        // gigastic places
//        final String tenantDataDir = System.getProperty("user.home") + "/culinary_culinary_dev/gigastic";
//        registry.addResourceHandler("/pic/place_image/**")
//                .addResourceLocations("/", "file://" + tenantDataDir + "/place_image/");
    }

    @Bean
    public JodaModule jodaModule() {
        return new JodaModule();
    }

//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        super.addArgumentResolvers(argumentResolvers);
//        argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
//    }

}
