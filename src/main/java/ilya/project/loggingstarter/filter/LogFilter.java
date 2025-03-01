package ilya.project.loggingstarter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import ilya.project.loggingstarter.config.property.FilterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LogFilter extends HttpFilter {

    private static final String DEFAULT_SECURE_VALUE = "***";
    private static final Logger log = LoggerFactory.getLogger(LogFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final Configuration jsonConfiguration = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    private final FilterProperties filterProperties;

    public LogFilter(FilterProperties filterProperties) {
        this.filterProperties = filterProperties;
    }

    @Override
    protected void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        for (String pattern : filterProperties.withoutLogging()) {
            String path = request.getRequestURI();
            if(pathMatcher.match(pattern, path)) {
                super.doFilter(request, response, chain);
                return;
            }
        }

        String requestId = request.getRequestId();
        String method = request.getMethod();
        String requestUri = request.getRequestURI() + formatQueryString(request);
        String headers = getSecuredHeaders(request);

        log.debug("Начало обработки запроса: {} метод: {} URI перехода: {} заголовки: {} ", requestId, method, requestUri, headers);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            super.doFilter(request, responseWrapper, chain);

            int status = response.getStatus();
            String responseBody = getSecuredBody(responseWrapper);

            log.debug("Ответ на запрос: {} метод: {} URI перехода: {} статус: {} Тело ответа: {}", requestId, method, requestUri, status, responseBody);
        } finally {
            responseWrapper.copyBodyToResponse();
        }

    }

    private String formatQueryString(HttpServletRequest request) {
        return Optional
                .ofNullable(request.getQueryString())
                .map(qs -> "?=" + qs)
                .orElse(Strings.EMPTY);
    }

    private String getSecuredHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        request::getHeader)
                );
        return headersMap.entrySet().stream()
                .map(this::securedHeaderString)
                .collect(Collectors.joining(", "));
    }

    private String securedHeaderString(Map.Entry<String, String> header) {
        String headerName = header.getKey();
        String headerValue = filterProperties.secureHeaders().contains(headerName)
                ? DEFAULT_SECURE_VALUE
                : header.getValue();

        return headerName + "=" + headerValue;
    }

    private String getSecuredBody(ContentCachingResponseWrapper responseWrapper) throws IOException {
        String responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
        DocumentContext json = JsonPath.using(jsonConfiguration).parse(responseBody);

        for (String path : filterProperties.secureJsonBodyPaths()) {
            securePathVariable(json, path);
        }

        return json.jsonString();
    }

    private void securePathVariable(DocumentContext context, String jsonPath) {
        Object data = context.read(jsonPath);

        if (data instanceof Map || data instanceof List) {
            context.set(jsonPath, maskAllValues(data));
        } else {
            context.set(jsonPath, DEFAULT_SECURE_VALUE);
        }
    }

    @SuppressWarnings("unchecked")
    private Object maskAllValues(Object obj) {
        if (obj instanceof Map map) {
            map.replaceAll((k, v) -> maskAllValues(v));
            return map;
        } else if (obj instanceof List<?> list) {
            return list.stream().map(this::maskAllValues).toList();
        } else {
            return DEFAULT_SECURE_VALUE;
        }
    }

}
