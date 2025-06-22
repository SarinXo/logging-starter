package ilya.project.loggingstarter.service;

import feign.Request;
import feign.Response;
import ilya.project.loggingstarter.config.property.FilterProperties;
import ilya.project.loggingstarter.constant.RequestDirection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoggingService {

    @Autowired
    private FilterProperties filterProperties;

    private static final Logger log = LoggerFactory.getLogger(LoggingService.class);

    public void logRequest(HttpServletRequest request, String headers) {
        String method = request.getMethod();
        String requestUri = request.getRequestURI() + formatQueryString(request);

        log.debug("Запрос {} {} {} {}", RequestDirection.IN, method, requestUri, headers);
    }

    public void logFeignRequest(Request request) {
        String method = request.httpMethod().name();
        String requestURI = request.url();
        String headers = inlineHeaders(request.headers());
        String body = filterProperties.logBody() ? new String(request.body(), StandardCharsets.UTF_8) : Strings.EMPTY;

        log.info("Запрос: {} {} {} {} {}", RequestDirection.IN, method, requestURI, headers, body);
    }

    public void logRequestBody(HttpServletRequest request, Object body) {
        String method = request.getMethod();
        String url = request.getRequestURI() + formatQueryString(request);

        Object bodyToLog = filterProperties.logBody() ? body : Strings.EMPTY;

        log.info("Тело запроса: {} {} {} {}", RequestDirection.IN, method, url, bodyToLog);
    }

    public void logResponse(HttpServletRequest request, HttpServletResponse response, String responseBody) {
        String method = request.getMethod();
        String url = request.getRequestURI() + formatQueryString(request);
        String status = String.valueOf(response.getStatus());

        String bodyToLog = filterProperties.logBody() ? responseBody : Strings.EMPTY;

        log.debug("Запрос: {} {} {} {} {}",RequestDirection.OUT, method, url, status, bodyToLog);
    }

    public void logFeignResponse(Response response, String responseBody) {
        String url = response.request().url();
        String method = response.request().httpMethod().name();
        int status = response.status();

        Object bodyToLog = filterProperties.logBody() ? responseBody : Strings.EMPTY;

        log.info("Ответ: {} {} {} {} body={}", RequestDirection.OUT, method, url, status, bodyToLog);
    }

    private String inlineHeaders(Map<String, Collection<String>> headersMap) {
        String inlineHeaders = headersMap.entrySet().stream()
                .map(entry -> {
                    String headerName = entry.getKey();
                    String headerValue = String.join(",", entry.getValue());

                    return headerName + "=" + headerValue;
                })
                .collect(Collectors.joining(","));

        return "headers={" + inlineHeaders + "}";
    }

    private String formatQueryString(HttpServletRequest request) {
        return Optional.ofNullable(request.getQueryString())
                .map(qs -> "?" + qs)
                .orElse(Strings.EMPTY);
    }

}

