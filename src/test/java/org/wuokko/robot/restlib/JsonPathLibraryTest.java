package org.wuokko.robot.restlib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.jayway.jsonpath.PathNotFoundException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Request.class})
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

        assertNull("The json should not null", json);
        
    }
    
    @Test
    public void testReadEmptySource() throws IOException {

        String json = lib.readSource("");

        assertNull("The json should not null", json);
        
    }

    @Test
    public void testShouldHaveElementCount() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.shouldHaveElementCount(source, 2, "$.store.book[*]");

        assertTrue("The element count should have matched", match);
    }
    
    @Test
    public void testShouldHaveElementCountNotFound() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.shouldHaveElementCount(source, 2, "$.foo.bar[*]");

        assertFalse("The element count should not have matched", match);
    }
    
    @Test
    public void testShouldHaveElementCountNoMatch() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.shouldHaveElementCount(source, 4, "$.store.book[*]");

        assertFalse("The element count should not have matched", match);
    }
    
    @Test
    public void testShouldHaveElementCountString() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.shouldHaveElementCount(source, 1, "$.store.bicycle.color");

        assertTrue("The element count should have matched", match);
    }
    
    @Test
    public void testShouldHaveElementCountStringWrongCount() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        boolean match = lib.shouldHaveElementCount(source, 2, "$.store.bicycle.color");

        assertFalse("The element count should not have matched", match);
    }
    
    @Test
    public void testFindJsonElement() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        String expected = "reference";
        
        Object element = lib.findJsonElement(source, "$.store.book[0].category");

        assertEquals("The elements should be equal", expected, element);
    }
    
    @Test
    public void testFindJsonElementNotFound() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));
        
        Object element = lib.findJsonElement(source, "$.store.book[0].foo");

        assertNull("The element should be null", element);
    }
    
    @Test
    public void testFindJsonElementList() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        List<String> expected = Arrays.asList("reference", "fiction");
        
        List<Object> element = lib.findJsonElementList(source, "$.store.book[*].category");

        assertEquals("The elements should be equal", expected, element);
    }
    
    @Test
    public void testFindJsonElementListNotFound() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));
        
        List<Object> element = lib.findJsonElementList(source, "$.store.book[*].foo");

        assertArrayEquals("The element list should be empty", Collections.emptyList().toArray(), element.toArray());
    }
    
    @Test(expected = PathNotFoundException.class)
    public void testFindJsonElementListPathNotFound() throws IOException {

        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));
        
        List<Object> element = lib.findJsonElementList(source, "$.store.foo[*]");

        assertArrayEquals("The element list should be empty", Collections.emptyList().toArray(), element.toArray());
    }
    
    @Test
    public void testJsonShouldBeEqual() throws IOException {

        String from = "{foo: bar}";
        String to = "{foo: bar}";
        
        boolean equal = lib.jsonShouldBeEqual(from, to);

        assertTrue("The elements should be equal", equal);
    }
    
    @Test
    public void testJsonShouldBeEqualNoMatch() throws IOException {

        String from = "{foo: bar}";
        String to = "{foo: xyz}";
        
        boolean equal = lib.jsonShouldBeEqual(from, to);

        assertFalse("The elements should NOT be equal", equal);
        
        equal = lib.jsonShouldBeEqual("", "");

        assertFalse("The elements should NOT be equal", equal);
        
        equal = lib.jsonShouldBeEqual(null, null);

        assertFalse("The elements should NOT be equal", equal);
        
        equal = lib.jsonShouldBeEqual(from, null);

        assertFalse("The elements should NOT be equal", equal);
    }
    
    @Test
    public void testJsonShouldBeEqualExact() throws IOException {

        String from = "{foo: bar}";
        String to = "{foo: bar}";
        
        boolean equal = lib.jsonShouldBeEqual(from, to, true);

        assertTrue("The elements should be equal", equal);
    }
    
    @Test
    public void testJsonShouldBeEqualExactNoMatch() throws IOException {

        String from = "{foo: bar}";
        String to = "{foo: xyz}";
        
        boolean equal = lib.jsonShouldBeEqual(from, to, true);

        assertFalse("The elements should NOT be equal", equal);
    }
}
