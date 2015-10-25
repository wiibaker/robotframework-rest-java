package org.wuokko.robot.restlib.mockserver;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockserver.matchers.Times.*;
import static org.mockserver.model.NottableString.*;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.matchers.ExactStringMatcher;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;

public class MockserverInitialization implements ExpectationInitializer {

	@Override
	public void initializeExpectations(MockServerClient mockServer) {
		
		mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/hello"),
                unlimited()
        )
        .respond(
                response()
                        .withStatusCode(200)
                        .withBody("{ message: 'hello world' }")
        );
		
		mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/login")
                        .withBody(exact("{username: 'foo', password: 'bar'}")),
                unlimited()
        )
        .respond(
                response()
                        .withStatusCode(200)
                        .withBody("{ message: 'Welcome', status: 'success' }")
        );
		
		mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/login"),
                unlimited()
        )
        .respond(
                response()
                        .withStatusCode(200)
                        .withBody("{ message: 'Access denied', status: 'failed' }")
        );
		
		mockServer.when(
                request()
                        .withMethod("PUT")
                        .withPath("/add")
                        .withBody(exact("{title: 'car', value: '1500'}")),
                unlimited()
        )
        .respond(
                response()
                        .withStatusCode(200)
                        .withBody("{ modified: '1' }")
        );
		
		mockServer.when(
                request()
                        .withMethod("DELETE")
                        .withPath("/delete")
                        .withQueryStringParameter("id", "123"),
                unlimited()
        )
        .respond(
                response()
                        .withStatusCode(200)
                        .withBody("{ deleted: '1' }")
        );
		
	}

}
