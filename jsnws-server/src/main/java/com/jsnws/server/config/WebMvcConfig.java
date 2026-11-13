package com.jsnws.server.config;

import com.jsnws.server.common.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * Web 配置：CORS（前端 8080 直连 8081）、鉴权拦截器、上传文件静态映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${jsnws.upload-base-dir}")
    private String uploadBaseDir;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * 用 Filter 处理 CORS：预检请求（OPTIONS）不会携带 Authorization，
     * 必须在到达鉴权拦截器之前就返回跨域响应头，否则 /admin/** 会被拦截成 code=300
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /resource/upload 落盘在 uploadBaseDir/{key}，key 形如 articlePicture/xxx.png
        File base = new File(uploadBaseDir);
        if (!base.exists()) {
            base.mkdirs();
        }
        String location = "file:" + base.getAbsolutePath().replace("\\", "/") + "/";
        registry.addResourceHandler("/resource/file/**").addResourceLocations(location);
    }
}
