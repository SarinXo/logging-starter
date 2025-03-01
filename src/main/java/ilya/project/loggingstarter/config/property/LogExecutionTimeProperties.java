package ilya.project.loggingstarter.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("logging-starter.log-execution-time")
public record LogExecutionTimeProperties(
        @DefaultValue("true")
        boolean enabled
) {
}
