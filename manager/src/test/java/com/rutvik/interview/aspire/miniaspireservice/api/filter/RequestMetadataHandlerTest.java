package com.rutvik.interview.aspire.miniaspireservice.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.UserAuth;
import com.rutvik.interview.aspire.miniaspireservice.service.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class RequestMetadataHandlerTest {

    @InjectMocks
    private RequestMetadataHandler requestMetadataHandler;

    @Mock
    private RequestMetadata requestMetadata;


    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationService authenticationService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        List<String> bypassUrlsList = new ArrayList<>();
        bypassUrlsList.add("/bypass");
        ReflectionTestUtils.setField(requestMetadataHandler, "bypassUrlsList", bypassUrlsList);

    }

    private UserAuth getUserAuth() {
        return UserAuth.builder().userId("some-user-id").authToken("some-auth-token").id(1L).build();
    }

    @Test
    public void testPreHandleWithValidTokenAndValidTokenRequiredApi() {
        request.addHeader("X-Auth-Token", "validToken");
        request.setRequestURI("/mini-aspire-service/some-random-endpoint");

        doNothing().when(requestMetadata).clearRequestMetadata();
        UserAuth userAuth = getUserAuth();
        when(authenticationService.validateToken(eq("validToken"))).thenReturn(Optional.of(userAuth));

        boolean result = requestMetadataHandler.preHandle(request, response, request);

        verify(requestMetadata).clearRequestMetadata();
        verify(requestMetadata).setAuthToken("validToken");
        verify(requestMetadata).setCustomerId("some-user-id");

        assertTrue(result);
    }

    @Test
    public void testPreHandleWithValidTokenAndValidTokenNotRequiredApi() {
        request.addHeader("X-Auth-Token", "validToken");
        request.setRequestURI("/mini-aspire-service/bypass");

        doNothing().when(requestMetadata).clearRequestMetadata();
        boolean result = requestMetadataHandler.preHandle(request, response, request);

        verify(requestMetadata).clearRequestMetadata();
        verify(requestMetadata, times(0)).setAuthToken("validToken");
        verify(requestMetadata, times(0)).setCustomerId("some-user-id");

        assertTrue(result);
    }

    @Test
    public void testPreHandleWithNoTokenAndValidTokenNotRequiredApi() {
        request.setRequestURI("/mini-aspire-service/bypass");

        doNothing().when(requestMetadata).clearRequestMetadata();

        boolean result = requestMetadataHandler.preHandle(request, response, request);

        verify(requestMetadata).clearRequestMetadata();
        verify(requestMetadata, times(0)).setAuthToken("validToken");
        verify(requestMetadata, times(0)).setCustomerId("some-user-id");

        assertTrue(result);
    }

    @Test
    public void testPreHandleWithNoTokenAndValidTokenRequiredApi() throws JsonProcessingException {
        request.setRequestURI("/mini-aspire-service/some-randome");

        doNothing().when(requestMetadata).clearRequestMetadata();
        when(authenticationService.validateToken(any())).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("Optional.empty()");

        boolean result = requestMetadataHandler.preHandle(request, response, request);

        verify(requestMetadata).clearRequestMetadata();
        verify(requestMetadata, times(0)).setAuthToken("validToken");
        verify(requestMetadata, times(0)).setCustomerId("some-user-id");

        assertFalse(result);
    }

    @Test
    public void testPostHandle() {
        requestMetadataHandler.postHandle(request, response, request, new ModelAndView());

        verify(requestMetadata).clearRequestMetadata();
    }

    @Test
    public void testAfterCompletion() {
        requestMetadataHandler.afterCompletion(request, response, request, null);

        verify(requestMetadata).clearRequestMetadata();
    }
}
