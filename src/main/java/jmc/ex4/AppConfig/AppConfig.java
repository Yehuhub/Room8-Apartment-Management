package jmc.ex4.AppConfig;

import jmc.ex4.interceptors.ResidenceInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@AllArgsConstructor
public class AppConfig implements WebMvcConfigurer {
    private final ResidenceInterceptor residenceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(residenceInterceptor)
                .addPathPatterns("/tenant/**"); // Apply to all tenant pages
    }
}