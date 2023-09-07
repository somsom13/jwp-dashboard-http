package org.apache.coyote.http11.message;

import java.util.Arrays;
import java.util.Optional;
import org.apache.coyote.http11.exception.UnsupportedContentTypeException;
import org.apache.coyote.http11.message.request.HttpRequest;
import org.apache.coyote.http11.message.request.RequestLine;

public enum ContentType {

    PLAIN_TEXT("text/plain"),
    HTML("text/html"),
    CSS("text/css"),
    JS("text/javascript"),
    ALL("*/*");

    public static final String ACCEPT_HEADER = "Accept";
    private static final ContentType DEFAULT = HTML;

    private final String type;

    ContentType(final String type) {
        this.type = type;
    }

    public static ContentType findResponseContentTypeFromRequest(final HttpRequest httpRequest) {
        final ContentType acceptedContentType = httpRequest.findFirstHeaderValue(ACCEPT_HEADER)
            .map(ContentType::findContentTypeByType)
            .orElse(ALL);

        // 1. content type이 없으면? 경로에서 찾음
        // 2.
        if (acceptedContentType != ALL) {
            return acceptedContentType;
        }
        return findContentTypeFromRequestPath(httpRequest.getRequestLine());
    }

    private static ContentType findContentTypeByType(final String type) {
        return Arrays.stream(values())
            .filter(contentType -> contentType.type.equals(type))
            .findAny()
            .orElseThrow(UnsupportedContentTypeException::new);
    }

    private static ContentType findContentTypeFromRequestPath(final RequestLine requestLine) {
        return requestLine.getFileExtension()
            .flatMap(ContentType::findContentTypeByName)
            .orElse(DEFAULT);
    }

    private static Optional<ContentType> findContentTypeByName(final String name) {
        return Arrays.stream(values())
            .filter(contentType -> contentType.name().equalsIgnoreCase(name))
            .findAny();
    }

    public String getType() {
        return type;
    }
}
