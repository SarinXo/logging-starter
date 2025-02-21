package ilya.project.loggingstarter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@AutoConfiguration("loggingStarterAutoConfiguration")
@ConfigurationPropertiesScan("ilya.project.loggingstarter.config.property")
public class PropertiesConfiguration {
}
