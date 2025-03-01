package ilya.project.loggingstarter.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "logging-starter")
public record LoggingStarterProperties(
        @DefaultValue("true")
        boolean enabled
) {
}
