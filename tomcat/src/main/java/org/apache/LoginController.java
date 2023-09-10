package org.apache;

import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.exception.UnauthorizedException;
import nextstep.jwp.model.User;
import org.apache.coyote.AbstractController;
import org.apache.coyote.http11.exception.FileNotReadableException;
import org.apache.coyote.http11.message.ContentType;
import org.apache.coyote.http11.message.HttpStatus;
import org.apache.coyote.http11.message.request.HttpRequest;
import org.apache.coyote.http11.message.response.HttpResponse;
import org.apache.coyote.http11.session.Session;
import org.apache.coyote.http11.session.SessionManager;

public class LoginController extends AbstractController {

    @Override
    protected void doGet(final HttpRequest httpRequest, final HttpResponse httpResponse) throws FileNotReadableException {
        final Session session = httpRequest.getSession(false);
        String fileContent = fileReader.readStaticFile("/login.html");
        if (session != null) {
            fileContent = fileReader.readStaticFile("/index.html");
        }
        httpResponse.setBody(fileContent, ContentType.findResponseContentTypeFromRequest(httpRequest));
        httpResponse.setHttpStatus(HttpStatus.FOUND);
    }

    @Override
    protected void doPost(final HttpRequest httpRequest, final HttpResponse httpResponse) throws FileNotReadableException {
        try {
            final User user = login(httpRequest);
            httpResponse.setHttpStatus(HttpStatus.FOUND);
            httpResponse.setHeader("Location", "/index.html");
            httpResponse.setJSessionCookieBySession(createUserSession(httpRequest, user));
        } catch (UnauthorizedException e) {
            httpResponse.setHttpStatus(HttpStatus.UNAUTHORIZED);
            httpResponse.setBody(fileReader.readStaticFile("/login.html"), ContentType.findResponseContentTypeFromRequest(httpRequest));
        }
    }

    private User login(final HttpRequest httpRequest) throws UnauthorizedException {
        return InMemoryUserRepository.findByAccount(httpRequest.getBodyOf("account"))
            .filter(foundUser -> foundUser.checkPassword(httpRequest.getBodyOf("password")))
            .orElseThrow(UnauthorizedException::new);
    }

    private Session createUserSession(final HttpRequest httpRequest, final User user) {
        final Session session = httpRequest.getSession(true);
        session.setAttribute("user", user);
        SessionManager.add(session);
        return session;
    }
}
