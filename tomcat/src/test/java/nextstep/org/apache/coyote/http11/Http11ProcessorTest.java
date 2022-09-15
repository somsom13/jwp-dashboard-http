package nextstep.org.apache.coyote.http11;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import nextstep.jwp.config.AppConfiguration;
import nextstep.jwp.exception.UserNotFoundException;
import org.apache.catalina.core.controller.ControllerContainer;
import org.apache.coyote.http11.Http11Processor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.StubSocket;

class Http11ProcessorTest {

    @Test
    void process() {
        // given
        final var socket = new StubSocket();
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final var processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 12 ",
                "",
                "Hello world!");

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void index() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final Http11Processor processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/index.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Content-Length: " + responseBody.getBytes().length + " \r\n" +
                "\r\n" +
                responseBody;

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("응답 헤더를 text/css로 전송하면 css 파일을 반환한다.")
    @Test
    void 응답_헤더를_text_css로_전송하면_css_파일을_반환한다() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /css/styles.css HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/css,*/*;q=0.1",
                "Connection: keep-alive ",
                "");

        final var socket = new StubSocket(httpRequest);
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final Http11Processor processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/css/styles.css");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/css;charset=utf-8 \r\n" +
                "Content-Length: " + responseBody.getBytes().length + " \r\n" +
                "\r\n" +
                responseBody;

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("/login url로 접근하면 login.html파일을 전달한다.")
    @Test
    void 로그인_url로_접근하면_login_html_파일을_반환한다() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/html;charset=utf-8",
                "Connection: keep-alive ",
                "");

        final var socket = new StubSocket(httpRequest);
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final Http11Processor processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/login.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Content-Length: " + responseBody.getBytes().length + " \r\n" +
                "\r\n" +
                responseBody;

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("/login url에 POST로 접근할 때, 로그인을 실패하면 ./401.html로 접근한다.")
    @Test
    void fail_login() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/html;charset=utf-8",
                "Connection: keep-alive ",
                "Content-Length: 32",
                "",
                "account=gugu&password=invalidpassword");

        final var socket = new StubSocket(httpRequest);
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final Http11Processor processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/login.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

        var expected = "HTTP/1.1 302 FOUND \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Location: ./401.html \r\n" +
                "\r\n";

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("/register url에 GET 방식으로 접근하면 register.html을 응답한다.")
    @Test
    void get_register() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/html;charset=utf-8",
                "Connection: keep-alive ",
                "");

        final var socket = new StubSocket(httpRequest);
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final Http11Processor processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/register.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Content-Length: " + responseBody.getBytes().length + " \r\n" +
                "\r\n" +
                responseBody;

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("/register url에 POST 방식으로 접근하면 회원가입을 한다.")
    @Test
    void post_register() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 80",
                "Content-Type: application/x-www-form-urlencoded ",
                "Accept: */* ",
                "",
                "account=newId&password=password&email=hkkang%40woowahan.com ");

        final var socket = new StubSocket(httpRequest);
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final Http11Processor processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/register.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

        var expected = "HTTP/1.1 302 FOUND \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Location: ./index.html \r\n" +
                "\r\n";

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("존재하지 않는 resource로 접근시 ./401.html로 리다이렉트 시킨다.")
    @Test
    void not_found_resource() {
        // given
        final String httpRequest = String.join("\r\n",
                "GET invalidUrl HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");
        final var socket = new StubSocket();
        final ControllerContainer container = new ControllerContainer(new AppConfiguration());
        final var processor = new Http11Processor(socket, container);

        // when
        processor.process(socket);

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 12 ",
                "",
                "Hello world!");

        assertThat(socket.output()).isEqualTo(expected);
    }
}
