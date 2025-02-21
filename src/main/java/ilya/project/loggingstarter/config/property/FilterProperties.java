package ilya.project.loggingstarter.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Set;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "logging-starter.filter")
public record FilterProperties(
        @DefaultValue("true")
        boolean enabled,
        /**
         * Паттерны по которым будут скрываться заголовки
         */
        @DefaultValue
        Set<Pattern> secure
) {
}
