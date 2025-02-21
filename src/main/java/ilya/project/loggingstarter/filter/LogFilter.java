package ilya.project.loggingstarter.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ilya.project.loggingstarter.config.property.FilterProperties;
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
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class LogFilter extends HttpFilter {

    private final static Logger log = LoggerFactory.getLogger(LogFilter.class);
    private static final String DEFAULT_SECURE_VALUE = "***";

    private final FilterProperties filterProperties;
    private final ObjectMapper objectMapper;

    public LogFilter(FilterProperties filterProperties, ObjectMapper objectMapper) {
        this.filterProperties = filterProperties;
        this.objectMapper = objectMapper;
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
        String headerValue = secureHeaderIfNecessary(header.getValue());

        return headerName + "=" + headerValue;
    }

    public String secureHeaderIfNecessary(String headerValue) {
        return isSecuredValue(headerValue, filterProperties.secureHeaderPatterns())
                ? DEFAULT_SECURE_VALUE
                : headerValue;
    }

    public boolean isSecuredValue(String input, Set<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(input).matches()) {
                return true;
            }
        }
        return false;
    }

    private String getSecuredBody(ContentCachingResponseWrapper responseWrapper) throws IOException {
        String responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
        JsonNode jsonBody = objectMapper.readTree(responseBody);
        JsonNode secureJsonBody = recursiveSecureJson(jsonBody, "");

        return secureJsonBody.toString();
    }

    private JsonNode recursiveSecureJson(JsonNode node, String prefix) {
        if (node.isObject()) {
            ObjectNode newNode = objectMapper.createObjectNode();

            node.fields().forEachRemaining(n -> {
                String newPrefix = !prefix.isEmpty()
                        ? prefix + "." + n.getKey()
                        : n.getKey();

                newNode.set(n.getKey(), recursiveSecureJson(n.getValue(), newPrefix));
            });

            return newNode;
        } else if (node.isArray()) {
            ArrayNode newArray = objectMapper.createArrayNode();

            for (JsonNode item : node) {
                newArray.add(recursiveSecureJson(item, prefix));
            }

            return newArray;
        } else {
            return isSecuredValue(prefix)
                    ? objectMapper.getNodeFactory().textNode(DEFAULT_SECURE_VALUE)
                    : node;
        }
    }

    private boolean isSecuredValue(String prefix) {
        for (var securePath : filterProperties.secureJsonBodyPaths()) {
            if (securePath.length() > prefix.length())
                continue;

            if (securePath.equals(prefix.substring(0, securePath.length())))
                return true;
        }
        return false;
    }

}
