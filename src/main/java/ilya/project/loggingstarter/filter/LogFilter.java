package ilya.project.loggingstarter.filter;

import ilya.project.loggingstarter.config.LoggingStarterAutoConfiguration;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LogFilter extends HttpFilter {

    private final static Logger log = LoggerFactory.getLogger(LogFilter.class);

    private static String formatQueryString(HttpServletRequest request) {
        return Optional
                .ofNullable(request.getQueryString())
                .map(qs -> "?=" + qs)
                .orElse(Strings.EMPTY);
    }

    private static String inlineHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        request::getHeader)
                );
        return headersMap.entrySet().stream()
                .map(entry -> {
                    String headerName = entry.getKey();
                    String headerValue = entry.getValue();

                    return headerName + "=" + headerValue;
                })
                .collect(Collectors.joining(", "));
    }

    @Override
    protected void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        String requestId = request.getRequestId();
        String method = request.getMethod();
        String requestUri = request.getRequestURI() + formatQueryString(request);
        String headers = inlineHeaders(request);

        log.debug("Начало обработки запроса: {} метод: {} URI перехода: {} заголовки: {} ", requestId, method, requestUri, headers);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            super.doFilter(request, responseWrapper, chain);

            int status = response.getStatus();
            String responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());

            log.debug("Ответ на запрос: {} метод: {} URI перехода: {} статус: {} Тело ответа: {}", requestId, method, requestUri, status, responseBody);
        } finally {
            responseWrapper.copyBodyToResponse();
        }

    }

}
