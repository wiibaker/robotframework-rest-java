package org.wuokko.robot.restlib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wuokko.robot.restlib.exception.JsonElementNotFoundException;
import org.wuokko.robot.restlib.exception.JsonNotEqualException;
import org.wuokko.robot.restlib.exception.JsonNotValidException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Request.class })
public class JsonPathLibraryTest {

    JsonPathLibrary lib = new JsonPathLibrary();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Request.class);
    }

    @Test
    public void testReadJsonSource() throws IOException {

        String expected = "{ \"foo\": bar }";

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("simple.test.json"));

        String json = lib.readSource(source);

        assertNotNull("The json should not be null", json);

        assertEquals("The returned JSON should be same as given", expected, json);

    }

    @Test
    public void testReadFileUrlSource() throws IOException {

        String expected = "{ \"foo\": bar }";

        File source = new File("src/test/resources/simple.test.json");

        String url = "file://" + source.toURI().getPath();

        String json = lib.readSource(url);

        assertNotNull("The json should not be null", json);

        assertEquals("The returned JSON should be as expected", expected, json);
    }

    @Test
    public void testReadHttpUrlSource() throws IOException {

        String expected = "{ \"foo\": bar }";

        String url = "http://example.com/test.json";

        Request mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);

        PowerMockito.when(Request.Get(any(URI.class))).thenReturn(mockRequest);
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(expected);

        String json = lib.readSource(url);

        assertNotNull("The json should not be null", json);

        assertEquals("The returned JSON should be as expected", expected, json);
    }

    @Test
    public void testReadNullSource() throws IOException {

        String json = lib.readSource(null);

        assertNull("The json should be null", json);

    }

    @Test
    public void testReadEmptySource() throws IOException {

        String json = lib.readSource("");

        assertNull("The json should be null", json);

    }

    @Test
    public void testJsonElementShouldMatch() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.jsonElementShouldMatch(source, "$.store.book[0].category", "reference");

        assertTrue("The element count should have matched", match);

        match = lib.jsonElementShouldMatch(source, "$.store.book[0].price", "8.95");

        assertTrue("The element count should have matched", match);

        match = lib.jsonElementShouldMatch(source, "$.store.notAvailabe", "[]");

        assertTrue("The element count should have matched", match);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonElementShouldMatchNoMatch() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        lib.jsonElementShouldMatch(source, "$.store.book[0].category", null);

    }

    @Test(expected = JsonNotEqualException.class)
    public void testJsonElementShouldMatchNull() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        lib.jsonElementShouldMatch(source, "$.store.book[0].category", "fiction");
    }

    @Test
    public void testShouldHaveElementCount() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.jsonShouldHaveElementCount(source, "$.store.book[*]", 2);

        assertTrue("The element count should have matched", match);
    }

    @Test(expected = JsonElementNotFoundException.class)
    public void testShouldHaveElementCountNotFound() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        lib.jsonShouldHaveElementCount(source, "$.foo.bar[*]", 2);

    }

    @Test(expected = JsonNotEqualException.class)
    public void testShouldHaveElementCountNoMatch() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        lib.jsonShouldHaveElementCount(source, "$.store.book[*]", 4);

    }

    @Test
    public void testShouldHaveElementCountString() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.jsonShouldHaveElementCount(source, "$.store.bicycle.color", 1);

        assertTrue("The element count should have matched", match);
    }

    @Test(expected = JsonElementNotFoundException.class)
    public void testShouldHaveElementCountStringWrongCount() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        lib.jsonShouldHaveElementCount(source, "$.store.bicycle.color", 2);

    }

    @Test
    public void testFindJsonElement() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        String expected = "reference";

        Object element = lib.findJsonElement(source, "$.store.book[0].category");

        assertEquals("The elements should be equal", expected, element);
    }

    @Test
    public void testFindJsonElementNotFound() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        Object element = lib.findJsonElement(source, "$.store.book[0].foo");

        assertNull("The element should be null", element);
    }

    @Test(expected = JsonElementNotFoundException.class)
    public void testFindJsonElementPathNotFound() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        lib.findJsonElement(source, "$.store.foo[0].bar");
    }

    @Test
    public void testFindJsonElementList() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        List<String> expected = Arrays.asList("reference", "fiction");

        List<Object> element = lib.findJsonElementList(source, "$.store.book[*].category");

        assertEquals("The elements should be equal", expected, element);
    }

    @Test
    public void testFindJsonElementListNotFound() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        List<Object> element = lib.findJsonElementList(source, "$.store.book[*].foo");

        assertArrayEquals("The element list should be empty", Collections.emptyList().toArray(), element.toArray());
    }

    @Test(expected = JsonElementNotFoundException.class)
    public void testFindJsonElementListPathNotFound() throws Exception {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        lib.findJsonElementList(source, "$.store.foo[*]");
    }

    @Test
    public void testJsonShouldBeEqual() throws Exception {

        String from = "{foo: bar}";
        String to = "{foo: bar}";

        boolean equal = lib.jsonShouldBeEqual(from, to);

        assertTrue("The elements should be equal", equal);
    }

    @Test(expected = JsonNotEqualException.class)
    public void testJsonShouldBeEqualNoMatch() throws Exception {

        String from = "{foo: bar}";
        String to = "{foo: xyz}";

        lib.jsonShouldBeEqual(from, to);

    }

    @Test(expected = JsonNotValidException.class)
    public void testJsonShouldBeEqualNoMatchBlank() throws Exception {

        lib.jsonShouldBeEqual("", "");
    }

    @Test(expected = JsonNotValidException.class)
    public void testJsonShouldBeEqualNoMatchNull() throws Exception {

        lib.jsonShouldBeEqual(null, null);

    }

    @Test(expected = JsonNotValidException.class)
    public void testJsonShouldBeEqualNoMatchNullPartial() throws Exception {

        String from = "{foo: bar}";

        lib.jsonShouldBeEqual(from, null);

    }

    @Test
    public void testJsonShouldBeEqualExact() throws Exception {

        String from = "{foo: bar}";
        String to = "{foo: bar}";

        boolean equal = lib.jsonShouldBeEqual(from, to, true);

        assertTrue("The elements should be equal", equal);
    }

    @Test(expected = JsonNotEqualException.class)
    public void testJsonShouldBeEqualExactNoMatch() throws Exception {

        String from = "{foo: bar}";
        String to = "{foo: xyz}";

        lib.jsonShouldBeEqual(from, to, true);
    }

    @Test
    public void testLoadURIHttp() throws ClientProtocolException, IOException, URISyntaxException {

        URI uri = new URI("http://example.com/test.json");

        Request mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);

        String mockContent = "Foo";

        PowerMockito.when(Request.Get(any(URI.class))).thenReturn(mockRequest);
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenReturn(mockContent);

        String content = lib.loadURI(uri);

        assertEquals("The content should be as expected", mockContent, content);
    }

    @Test
    public void testLoadURIFile() throws ClientProtocolException, IOException, URISyntaxException {

        String expected = "{ \"foo\": bar }";

        File source = new File("src/test/resources/simple.test.json");

        URI uri = new URI("file://" + source.toURI().getPath());

        String content = lib.loadURI(uri);

        assertEquals("The content should be as expected", expected, content);
    }

    @Test
    public void testLoadURINull() throws ClientProtocolException, IOException, URISyntaxException {

        String content = lib.loadURI(null);

        assertNull("The content should be as expected", content);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadURIInvalid() throws ClientProtocolException, IOException, URISyntaxException {

        URI uri = new URI("http://foobarfoobar.example.com/test.json");

        Request mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);

        PowerMockito.when(Request.Get(any(URI.class))).thenReturn(mockRequest);
        Mockito.when(mockRequest.connectTimeout(anyInt()).socketTimeout(anyInt()).execute().returnContent().asString()).thenThrow(IOException.class);

        String content = lib.loadURI(uri);

        assertNull("The content should be as expected", content);
    }
    
    @Test
    public void testLoadURIUseCache() throws ClientProtocolException, IOException, URISyntaxException {

        System.setProperty("use.uri.cache", "true");
        
        lib = new JsonPathLibrary();
        
        String expected = "{ \"foo\": bar }";

        File source = new File("src/test/resources/simple.test.json");

        URI uri = new URI("file://" + source.toURI().getPath());

        String content = lib.loadURI(uri);

        assertEquals("The content should be as expected", expected, content);
        
        content = lib.loadURI(uri);

        assertEquals("The content should be as expected", expected, content);
    }

}
