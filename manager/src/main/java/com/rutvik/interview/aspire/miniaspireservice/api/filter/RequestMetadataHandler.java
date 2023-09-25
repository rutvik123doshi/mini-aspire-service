package com.rutvik.interview.aspire.miniaspireservice.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutvik.interview.aspire.miniaspireservice.api.GenericResponse;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.User;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.UserAuth;
import com.rutvik.interview.aspire.miniaspireservice.service.AuthenticationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class RequestMetadataHandler implements HandlerInterceptor {
    private static final String X_AUTH_TOKEN = "X-Auth-Token";

    private final RequestMetadata requestMetadata;
    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;


    @Value("#{'${authentication.bypass.urls}'.split(',')}")
    private List<String> bypassUrlsList;


    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        requestMetadata.clearRequestMetadata();
        String token = request.getHeader(X_AUTH_TOKEN);
        return isRequestAllowedToByPass(request) || isTokenValid(token, response);
    }

    private boolean isTokenValid(String token, HttpServletResponse response) {
        Optional<UserAuth> userAuthOptional = authenticationService.validateToken(token);
        if (userAuthOptional.isPresent()) {
            requestMetadata.setAuthToken(token);
            requestMetadata.setCustomerId(userAuthOptional.get().getUserId());
            return true;
        }
        // populate api response
        try {
            response.getWriter()
                    .write(objectMapper.writeValueAsString(GenericResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .error("Invalid or missing API token!")
                            .build()));
            response.setStatus(200);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        } catch (IOException e) {
            log.error(String.format("Error while writing to response: %s", e.getMessage()), e);
        }
        return false;
    }

    private boolean isRequestAllowedToByPass(HttpServletRequest request) {
        String url = request.getRequestURI().replace("/mini-aspire-service", "");
        return bypassUrlsList.stream().anyMatch(url::contains);
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                           ModelAndView modelAndView) {
        requestMetadata.clearRequestMetadata();
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        requestMetadata.clearRequestMetadata();
    }
}
