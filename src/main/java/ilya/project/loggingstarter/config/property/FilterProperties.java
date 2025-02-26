package ilya.project.loggingstarter.config.property;

import ilya.project.loggingstarter.validator.JsonPathCollection;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "logging-starter.filter")
public record FilterProperties(
        @DefaultValue("true")
        boolean enabled,
        /**
         * Заголовки, которые будут отключены.
         */
        @DefaultValue
        Set<String> secureHeaders,
        /**
         * Пути для json объектов, по которым будут скрываться переменные.
         * Должны соответствовать паттерну (json переменная).(json переменная). ... и т.д.
         */
        @DefaultValue
        @JsonPathCollection
        Set<String> secureJsonBodyPaths,
        /**
         * Отключение логирования для эндпоинтов с такими url. Проверяется через AntPathMatcher
         */
        @DefaultValue
        Set<String> withoutLogging
) {
}
