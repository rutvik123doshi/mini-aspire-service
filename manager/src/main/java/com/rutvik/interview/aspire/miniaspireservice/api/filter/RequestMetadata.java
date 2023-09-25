package com.rutvik.interview.aspire.miniaspireservice.api.filter;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;


@Component
public class RequestMetadata {
    public static final String CUSTOMER_ID = "customerId";
    public static final String AUTH_TOKEN = "authToken";

    public String customerId() {
        return ThreadContext.get(CUSTOMER_ID);
    }

    public void removeCustomerId() {
        ThreadContext.remove(CUSTOMER_ID);
    }

    public void setCustomerId(String customerId) {ThreadContext.put(CUSTOMER_ID, customerId);}

    public String authToken() {
        return ThreadContext.get(AUTH_TOKEN);
    }

    public void removeAuthToken() {
        ThreadContext.remove(AUTH_TOKEN);
    }

    public void setAuthToken(String authToken) {ThreadContext.put(AUTH_TOKEN, authToken);}


    public void clearRequestMetadata() {
        ThreadContext.clearMap();
        ThreadContext.clearStack();
        ThreadContext.clearAll();
    }
}
