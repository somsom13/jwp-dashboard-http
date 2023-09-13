package org.apache.catalina.controller;

import org.apache.coyote.http11.message.HttpMethod;
import org.apache.coyote.http11.message.request.HttpRequest;
import org.apache.coyote.http11.message.response.HttpResponse;

public abstract class AbstractController implements Controller {

    @Override
    public void service(final HttpRequest httpRequest, final HttpResponse httpResponse) throws Exception {
        final HttpMethod requestMethod = httpRequest.getMethod();

        if (requestMethod == HttpMethod.GET) {
            doGet(httpRequest, httpResponse);
            return;
        }
        if (requestMethod == HttpMethod.POST) {
            doPost(httpRequest, httpResponse);
            return;
        }
        throw new UnsupportedOperationException();
    }

    protected void doPost(HttpRequest httpRequest, HttpResponse httpResponse) { /* NOOP */ }
    protected void doGet(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception { /* NOOP */ }
}

