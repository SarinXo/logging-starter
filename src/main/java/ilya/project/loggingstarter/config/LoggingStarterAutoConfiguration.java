package ilya.project.loggingstarter.config;

import feign.Logger;
import ilya.project.loggingstarter.adpect.LogExecutionTimeAspect;
import ilya.project.loggingstarter.config.property.FilterProperties;
import ilya.project.loggingstarter.feign.FeignRequestLogger;
import ilya.project.loggingstarter.filter.LogFilter;
import ilya.project.loggingstarter.filter.WebLoggingRequestBodyAdvice;
import ilya.project.loggingstarter.service.LoggingService;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "logging-starter.enabled", matchIfMissing = true)
@ConfigurationPropertiesScan("ilya.project.loggingstarter.config.property")
public class LoggingStarterAutoConfiguration {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LoggingStarterAutoConfiguration.class);

    @PostConstruct
    public void init() {
        log.info("Logging-starter has been enabled");
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging-starter.log-execution-time", value = "enabled", matchIfMissing = true)
    public LogExecutionTimeAspect logExecutionTimeAspect() {
        return new LogExecutionTimeAspect();
    }

    @Bean("loggingFilterRegistationBean")
    @ConditionalOnProperty(prefix = "logging-starter.filter", value = "enabled", matchIfMissing = true)
    public LogFilter loggingFilter() {
        return new LogFilter();
    }

    @Bean
    @ConditionalOnBean(LogFilter.class)
    @ConditionalOnProperty(prefix = "logging-starter.filter", value = "log-body", havingValue = "true")
    public WebLoggingRequestBodyAdvice webLoggingRequestBodyAdvice() {
        return new WebLoggingRequestBodyAdvice();
    }

    @Bean
    public LoggingService loggingService() {
        return new LoggingService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging-starter.filter", value = "log-feign-requests", havingValue = "true")
    public FeignRequestLogger feignRequestLogger() {
        return new FeignRequestLogger();
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging-starter.filter", value = "log-feign-requests", havingValue = "true")
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }


}