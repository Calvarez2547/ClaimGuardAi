package com.claimguardai.common;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApiErrorResponseFactory {

    public ErrorResponse build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ApiErrorDetail> details) {

        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                resolveCorrelationId(request),
                details == null ? List.of() : List.copyOf(details));
    }

    public ErrorResponse build(HttpStatus status, String message, HttpServletRequest request) {
        return build(status, message, request, List.of());
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        Object attributeValue = request.getAttribute(CorrelationIdConstants.REQUEST_ATTRIBUTE);
        if (attributeValue instanceof String correlationId && StringUtils.hasText(correlationId)) {
            return correlationId;
        }

        String headerValue = request.getHeader(CorrelationIdConstants.HEADER_NAME);
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }

        return UUID.randomUUID().toString();
    }
}
