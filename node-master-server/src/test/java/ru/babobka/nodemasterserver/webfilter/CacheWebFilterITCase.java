package ru.babobka.nodemasterserver.webfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodeutils.container.ContainerException;
import ru.babobka.vsjws.enumerations.HttpMethod;
import ru.babobka.vsjws.enumerations.ResponseCode;
import ru.babobka.vsjws.model.FilterResponse;
import ru.babobka.vsjws.webserver.HttpRequest;
import ru.babobka.vsjws.webserver.HttpResponse;

public class CacheWebFilterITCase {

    private static final String DUMMY_REQUEST_BODY = "Hello World";

    private static final String VALID_JSON_RESPONSE = "{ \"name\":\"John\", \"age\":31, \"city\":\"New York\" }";

    private static CacheWebFilter cacheFilter;

    @BeforeClass
    public static void setUp() throws ContainerException, FileNotFoundException {
        MasterServer.initTestContainer();

        cacheFilter = new CacheWebFilter();
    }

    private HttpRequest createRequest(HttpMethod method, String body, String uri) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getBody()).thenReturn(body);
        if (uri != null) {
            when(request.getUri()).thenReturn(uri);
        }
        when(request.getMethod()).thenReturn(method);
        return request;
    }

    private HttpResponse createResponse(ResponseCode code, String body) {
        return HttpResponse.text(body, code);
    }

    @Test
    public void testGetRequestCache() {
        HttpRequest request = createRequest(HttpMethod.GET, DUMMY_REQUEST_BODY, "");
        HttpResponse response = createResponse(ResponseCode.OK, VALID_JSON_RESPONSE);
        FilterResponse filterResponse = cacheFilter.onFilter(request);
        assertTrue(filterResponse.isProceed());
        cacheFilter.afterFilter(request, response);
        filterResponse = cacheFilter.onFilter(request);
        assertFalse(filterResponse.isProceed());
        JSONObject filterJson = new JSONObject(new String(filterResponse.getResponse().getContent()));
        JSONObject expectedJson = new JSONObject(new String(response.getContent()));
        assertEquals(filterJson.toString(), expectedJson.toString());
    }

    @Test
    public void testPutRequestCache() {
        HttpRequest request = createRequest(HttpMethod.PUT, DUMMY_REQUEST_BODY, "");
        HttpResponse response = createResponse(ResponseCode.OK, VALID_JSON_RESPONSE);
        FilterResponse filterResponse = cacheFilter.onFilter(request);
        assertTrue(filterResponse.isProceed());
        cacheFilter.afterFilter(request, response);
        filterResponse = cacheFilter.onFilter(request);
        assertTrue(filterResponse.isProceed());
    }

}
