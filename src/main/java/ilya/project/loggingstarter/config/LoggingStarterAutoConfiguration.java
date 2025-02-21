package ilya.project.loggingstarter.config;

import ilya.project.loggingstarter.adpect.LogExecutionTimeAspect;
import ilya.project.loggingstarter.filter.LogFilter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnProperty(name = "logging-starter.enabled", matchIfMissing = true)
public class LoggingStarterAutoConfiguration {

    private final static Logger log = LoggerFactory.getLogger(LoggingStarterAutoConfiguration.class);

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
    public FilterRegistrationBean<LogFilter> loggingFilter() {
        FilterRegistrationBean<LogFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new LogFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }

}
