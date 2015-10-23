package org.wuokko.robot.restlib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Request.class, RequestUtil.class, FileUtils.class })
public class RequestUtilTest {

	@Mock
	PropertiesConfiguration mockConfiguration;
	
	@InjectMocks
	RequestUtil util = new RequestUtil(mockConfiguration);

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Request.class);
        PowerMockito.mockStatic(FileUtils.class);
    }

    @Test
    public void testConstructor() {
        
        PropertiesConfiguration mockConfiguration = mock(PropertiesConfiguration.class);

        new RequestUtil(mockConfiguration);
        
        Mockito.verify(mockConfiguration, Mockito.times(0)).getInt(RequestUtil.KEY_CONNECTION_TIMEOUT);
        Mockito.verify(mockConfiguration, Mockito.times(0)).getInt(RequestUtil.KEY_USE_URI_CACHE);
        
        Mockito.when(mockConfiguration.containsKey(RequestUtil.KEY_CONNECTION_TIMEOUT)).thenReturn(Boolean.TRUE);
        Mockito.when(mockConfiguration.containsKey(RequestUtil.KEY_USE_URI_CACHE)).thenReturn(Boolean.TRUE);
        
        new RequestUtil(mockConfiguration);
        
        Mockito.verify(mockConfiguration, Mockito.times(1)).getInt(RequestUtil.KEY_CONNECTION_TIMEOUT);
        Mockito.verify(mockConfiguration, Mockito.times(1)).getBoolean(RequestUtil.KEY_USE_URI_CACHE);
        
    }
    
    @Test
    public void testConstructorNullConfiguration() {
        
        new RequestUtil(null);

    }
    
    @Test
    public void testReadSource() throws IOException {

        String expected = "{ \"foo\": bar }";

        String url = "http://example.com/test.json";

        RequestUtil spyUtil = PowerMockito.spy(util);
        
        PowerMockito.when(spyUtil.readSource(Matchers.eq(url), Matchers.eq("GET"))).thenReturn(expected);
        
        String json = spyUtil.readSource(url);

        assertNotNull("The json should not be null", json);

        assertEquals("The returned JSON should be as expected", expected, json);
        
        Mockito.verify(spyUtil, Mockito.times(0)).getURI(Matchers.anyString());
        Mockito.verify(spyUtil, Mockito.times(0)).loadURI(Matchers.any(URI.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
        
        PowerMockito.when(spyUtil.readSource(Matchers.eq(url), Matchers.eq("POST"), (String)Matchers.eq(null))).thenReturn(expected);
        
        json = spyUtil.readSource(url, "POST");

        assertNotNull("The json should not be null", json);

        assertEquals("The returned JSON should be as expected", expected, json);
        
        Mockito.verify(spyUtil, Mockito.times(0)).getURI(Matchers.anyString());
        Mockito.verify(spyUtil, Mockito.times(0)).loadURI(Matchers.any(URI.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
        
        PowerMockito.when(spyUtil.readSource(Matchers.eq(url), Matchers.eq("POST"), Matchers.eq("randomData"), (String)Matchers.eq(null))).thenReturn(expected);
        
        json = spyUtil.readSource(url, "POST", "randomData");

        assertNotNull("The json should not be null", json);

        assertEquals("The returned JSON should be as expected", expected, json);
        
        Mockito.verify(spyUtil, Mockito.times(0)).getURI(Matchers.anyString());
        Mockito.verify(spyUtil, Mockito.times(0)).loadURI(Matchers.any(URI.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());

        URI mockUri = PowerMockito.mock(URI.class);
        
        PowerMockito.doReturn(mockUri).when(spyUtil).getURI(Matchers.eq(url));
        PowerMockito.doReturn(expected).when(spyUtil).loadURI(Matchers.eq(mockUri), Matchers.eq("POST"), Matchers.eq("randomData"), Matchers.eq("contentType"));
        
        json = spyUtil.readSource(url, "POST", "randomData", "contentType");
        
        assertEquals("The returned JSON should be as expected", expected, json);
        
        Mockito.verify(spyUtil, Mockito.times(1)).getURI(Matchers.eq(url));
        Mockito.verify(spyUtil, Mockito.times(1)).loadURI(Matchers.any(URI.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());

    }

    @Test
    public void testReadNullSource() throws IOException {

    	RequestUtil spyUtil = PowerMockito.spy(util);
    	
        String json = spyUtil.readSource(null, "POST", "randomData", "contentType");

        assertNull("The json should be null", json);
        
        Mockito.verify(spyUtil, Mockito.times(0)).getURI(Matchers.anyString());
        Mockito.verify(spyUtil, Mockito.times(0)).loadURI(Matchers.any(URI.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());


    }

    @Test
    public void testReadEmptySource() throws IOException {

    	RequestUtil spyUtil = PowerMockito.spy(util);
    	
        String json = spyUtil.readSource("", "POST", "randomData", "contentType");

        assertNull("The json should be null", json);
        
        Mockito.verify(spyUtil, Mockito.times(0)).getURI(Matchers.anyString());
        Mockito.verify(spyUtil, Mockito.times(0)).loadURI(Matchers.any(URI.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());


    }
    
    @Test
    public void testReadSourceURINotFound() throws IOException {

    	String jsonSource = "{\"foo\":\"bar\"}";
    	
    	RequestUtil spyUtil = PowerMockito.spy(util);
    	
    	PowerMockito.doReturn(null).when(spyUtil).getURI(Matchers.eq(jsonSource));
    	
        String json = spyUtil.readSource(jsonSource, "POST", "randomData", "contentType");

        assertEquals("The returned JSON should be as expected", jsonSource, json);
        
        Mockito.verify(spyUtil, Mockito.times(1)).getURI(Matchers.eq(jsonSource));
        Mockito.verify(spyUtil, Mockito.times(0)).loadURI(Matchers.any(URI.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());


    }

    @Test
    public void testLoadURIFile() throws Exception {

    	String expected = "FooBar";

        URI mockURI = PowerMockito.mock(URI.class);

        Mockito.when(mockURI.getScheme()).thenReturn("file");
        
        File mockFile = Mockito.mock(File.class);
        PowerMockito.whenNew(File.class).withArguments(mockURI).thenReturn(mockFile);
        
        PowerMockito.when(FileUtils.readFileToString(Matchers.eq(mockFile))).thenReturn("FooBar");

        String content = util.loadURI(mockURI, "GET", null, null);

        assertEquals("The content should be as expected", expected, content);
    }

    @Test
    public void testLoadURINull() throws ClientProtocolException, IOException, URISyntaxException {

        String content = util.loadURI(null, null, null, null);

        assertNull("The content should be null", content);
    }
    
    @Test
    public void testLoadMethodEmpty() throws ClientProtocolException, IOException, URISyntaxException {

    	URI mockURI = PowerMockito.mock(URI.class);
    	
        String content = util.loadURI(mockURI, null, null, null);

        assertNull("The content should be null", content);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadURIInvalid() throws ClientProtocolException, IOException, URISyntaxException {

        URI uri = new URI("http://foobarfoobar.example.com/test.json");

        Request mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);

        PowerMockito.when(Request.Get(any(URI.class))).thenReturn(mockRequest);
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenThrow(IOException.class);

        String content = util.loadURI(uri, "GET", null, null);

        assertNull("The content should be as expected", content);
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void testLoadURIUseCache() throws ClientProtocolException, IOException, URISyntaxException {
    	
    	Map<URI, String> mockCache = Mockito.mock(Map.class);
    	
        System.setProperty("use.uri.cache", "true");
        
        util = new RequestUtil(mockConfiguration);
        
        // Set the mock cache
        util.uriCache = mockCache;
        
        String expected = "{ \"foo\": bar }";

        URI mockURI = PowerMockito.mock(URI.class);

        // Cache hit
        Mockito.when(mockCache.get(mockURI)).thenReturn(expected);
        
        String content = util.loadURI(mockURI, "GET", null, null);

        assertEquals("The content should be as expected", expected, content);
        
        // No cache hit
        Mockito.when(mockCache.get(mockURI)).thenReturn(null);
        
        Request mockRequest = Mockito.mock(Request.class, RETURNS_DEEP_STUBS);
        
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(expected);
        
        PowerMockito.when(Request.Get(mockURI)).thenReturn(mockRequest);
        
        content = util.loadURI(mockURI, "GET", null, null);

        assertEquals("The content should be as expected", expected, content);

    }
    
    @Test
    public void testLoadURIGet() throws ClientProtocolException, IOException, URISyntaxException {

        
        String expected = "{ \"foo\": bar }";

        URI mockURI = PowerMockito.mock(URI.class);

        Request mockRequest = Mockito.mock(Request.class, RETURNS_DEEP_STUBS);
        
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(expected);
        
        PowerMockito.when(Request.Get(Matchers.eq(mockURI))).thenReturn(mockRequest);
        
        Mockito.when(mockRequest.bodyString(Matchers.eq("data"), Matchers.any(ContentType.class))).thenReturn(mockRequest);
        
        String content = util.loadURI(mockURI, "GET", null, null);

        assertEquals("The content should be as expected", expected, content);
        
        PowerMockito.verifyStatic(Mockito.times(1));
        Request.Get(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Post(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Put(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Delete(mockURI);
    }
    
    @Test
    public void testLoadURIPost() throws ClientProtocolException, IOException, URISyntaxException {

        
        String expected = "{ \"foo\": bar }";

        URI mockURI = PowerMockito.mock(URI.class);

        Request mockRequest = Mockito.mock(Request.class, RETURNS_DEEP_STUBS);
        
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(expected);

        PowerMockito.when(Request.Post(Matchers.eq(mockURI))).thenReturn(mockRequest);
        
        Mockito.when(mockRequest.bodyString(Matchers.eq("data"), Matchers.any(ContentType.class))).thenReturn(mockRequest);
        
        String content = util.loadURI(mockURI, "POST", "data", "contentType");

        assertEquals("The content should be as expected", expected, content);
        
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Get(mockURI);
        PowerMockito.verifyStatic(Mockito.times(1));
        Request.Post(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Put(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Delete(mockURI);
    }
    
    @Test
    public void testLoadURIPut() throws ClientProtocolException, IOException, URISyntaxException {

        
        String expected = "{ \"foo\": bar }";

        URI mockURI = PowerMockito.mock(URI.class);

        Request mockRequest = Mockito.mock(Request.class, RETURNS_DEEP_STUBS);
        
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(expected);

        PowerMockito.when(Request.Put(Matchers.eq(mockURI))).thenReturn(mockRequest);
        
        Mockito.when(mockRequest.bodyString(Matchers.eq("data"), Matchers.any(ContentType.class))).thenReturn(mockRequest);
        
        String content = util.loadURI(mockURI, "PUT", "data", "contentType");

        assertEquals("The content should be as expected", expected, content);
        
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Get(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Post(mockURI);
        PowerMockito.verifyStatic(Mockito.times(1));
        Request.Put(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Delete(mockURI);
    }
    
    @Test
    public void testLoadURIDelete() throws ClientProtocolException, IOException, URISyntaxException {

        
        String expected = "{ \"foo\": bar }";

        URI mockURI = PowerMockito.mock(URI.class);

        Request mockRequest = Mockito.mock(Request.class, RETURNS_DEEP_STUBS);
        
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(expected);
        
        PowerMockito.when(Request.Delete(Matchers.eq(mockURI))).thenReturn(mockRequest);

        String content = util.loadURI(mockURI, "DELETE", null, null);

        assertEquals("The content should be as expected", expected, content);
        
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Get(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Post(mockURI);
        PowerMockito.verifyStatic(Mockito.times(0));
        Request.Put(mockURI);
        PowerMockito.verifyStatic(Mockito.times(1));
        Request.Delete(mockURI);
    }
    
    @Test
    public void testLoadURIInvalidRequest() throws ClientProtocolException, IOException, URISyntaxException {
        
        util = new RequestUtil(mockConfiguration);
        
        String expected = "{ \"foo\": bar }";

        URI mockURI = PowerMockito.mock(URI.class);
        
        Request mockRequest = Mockito.mock(Request.class, RETURNS_DEEP_STUBS);
        
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(expected);
        
        PowerMockito.when(Request.Get(mockURI)).thenReturn(mockRequest);
        
        String content = util.loadURI(mockURI, "FOO", null, null);

        assertNull("The content should be null", content);
    }
    
    @Test
    public void testGetURI() throws ClientProtocolException, IOException, URISyntaxException {
    
    	URI uri = util.getURI("http://example.com");
    	
    	assertEquals("The URI should be as expected", "http://example.com", uri.toString());
    	
    	uri = util.getURI("file:///usr/tmp");
    	
    	assertEquals("The URI should be as expected", "file:///usr/tmp", uri.toString());
    	
    	// Windows file path
    	uri = util.getURI("file:///C:\\tmp");
    	
    	assertEquals("The URI should be as expected", "file:///C:/tmp", uri.toString());
    }

}
