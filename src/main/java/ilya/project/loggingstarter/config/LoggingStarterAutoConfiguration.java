package ilya.project.loggingstarter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ilya.project.loggingstarter.adpect.LogExecutionTimeAspect;
import ilya.project.loggingstarter.config.property.FilterProperties;
import ilya.project.loggingstarter.filter.LogFilter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "logging-starter.enabled", matchIfMissing = true)
@ConfigurationPropertiesScan("ilya.project.loggingstarter.config.property")
public class LoggingStarterAutoConfiguration {

    private final static Logger log = LoggerFactory.getLogger(LoggingStarterAutoConfiguration.class);

    private final FilterProperties filterProperties;

    public LoggingStarterAutoConfiguration(FilterProperties filterProperties) {
        this.filterProperties = filterProperties;
    }

    @PostConstruct
    public void init() {
        log.info("--------------------------------");
        log.info("Logging-starter has been enabled");
        log.info("--------------------------------");
    }

    @Bean
    @ConditionalOnProperty(name = "logging-starter.log-execution-time.enabled", matchIfMissing = true)
    public LogExecutionTimeAspect logExecutionTimeAspect() {
        return new LogExecutionTimeAspect();
    }

    @Bean("loggingFilterRegistationBean")
    @ConditionalOnProperty(name = "logging-starter.filter.enabled", matchIfMissing = true)
    public LogFilter loggingFilter() {
        return new LogFilter(filterProperties);
    }

}