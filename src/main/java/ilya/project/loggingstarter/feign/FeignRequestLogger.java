package ilya.project.loggingstarter.feign;

import feign.Logger;
import feign.Request;
import feign.Response;
import ilya.project.loggingstarter.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignRequestLogger extends Logger {

    @Autowired
    private LoggingService loggingService;

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        loggingService.logFeignRequest(request);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
        String responseBody = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);

        loggingService.logFeignResponse(response, responseBody);

        return response.toBuilder()
                .body(responseBody, StandardCharsets.UTF_8)
                .build();
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        // Реализация не требуется
    }
}
