package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import org.apache.coyote.Controller;
import org.apache.coyote.Processor;
import org.apache.coyote.UrlHandlerMapping;
import org.apache.coyote.http11.exception.UnsupportedContentTypeException;
import org.apache.coyote.http11.message.HttpStatus;
import org.apache.coyote.http11.message.request.HttpRequest;
import org.apache.coyote.http11.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
            final var outputStream = connection.getOutputStream()) {

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            final HttpRequest httpRequest = HttpRequest.from(reader);
            final Controller handler = UrlHandlerMapping.getHandler(httpRequest);
            final HttpResponse httpResponse = HttpResponse.create();

            service(httpRequest, httpResponse, handler);

            outputStream.write(httpResponse.convertToMessage().getBytes());
            outputStream.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void service(final HttpRequest httpRequest, final HttpResponse httpResponse, final Controller handler) throws Exception {
        try {
            handler.service(httpRequest, httpResponse);
        } catch (UnsupportedContentTypeException e) {
            httpResponse.setHttpStatus(HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
