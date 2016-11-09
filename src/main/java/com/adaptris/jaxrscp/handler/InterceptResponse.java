package com.adaptris.jaxrscp.handler;

/**
 * Created by irlap on 04/11/2016.
 */
public class InterceptResponse {

    private boolean proceed = true;
    private Object response;


    public boolean isProceed() {
        return proceed;
    }

    public void setProceed(boolean proceed) {
        this.proceed = proceed;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
