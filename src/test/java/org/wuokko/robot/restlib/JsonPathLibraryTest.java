package org.wuokko.robot.restlib;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
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
import org.wuokko.robot.restlib.exception.JsonElementNotFoundException;
import org.wuokko.robot.restlib.exception.JsonNotEqualException;
import org.wuokko.robot.restlib.exception.JsonNotValidException;
import org.wuokko.robot.restlib.util.PropertiesUtil;
import org.wuokko.robot.restlib.util.RequestUtil;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Request.class, JsonPathLibrary.class, PropertiesUtil.class, JsonPath.class})
public class JsonPathLibraryTest {
	
	@Mock
	RequestUtil util;
	
	@InjectMocks
    JsonPathLibrary lib = new JsonPathLibrary();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Request.class);
        PowerMockito.mockStatic(PropertiesUtil.class);
        PowerMockito.mockStatic(JsonPath.class);
    }

    @Test
    public void testConstructor() throws Exception {
        
        PropertiesConfiguration mockConfiguration = mock(PropertiesConfiguration.class);
        
        RequestUtil mockUtil = mock(RequestUtil.class);
        
        Mockito.when(PropertiesUtil.loadProperties(Matchers.eq("robot-rest-lib.properties"))).thenReturn(mockConfiguration);
        
        PowerMockito.whenNew(RequestUtil.class).withArguments(mockConfiguration).thenReturn(mockUtil);
        
        new JsonPathLibrary();
        
        PowerMockito.verifyStatic();
        PropertiesUtil.loadProperties("robot-rest-lib.properties");
        
        PowerMockito.verifyNew(RequestUtil.class).withArguments(mockConfiguration);
    }
    
    @Test
    public void testConstructorWithPropertiesFile() throws Exception {
        
        PropertiesConfiguration mockConfiguration = mock(PropertiesConfiguration.class);
        
        RequestUtil mockUtil = mock(RequestUtil.class);
        
        Mockito.when(PropertiesUtil.loadProperties(Matchers.eq("foo.properties"))).thenReturn(mockConfiguration);
        
        PowerMockito.whenNew(RequestUtil.class).withArguments(mockConfiguration).thenReturn(mockUtil);
        
        new JsonPathLibrary("foo.properties");
        
        PowerMockito.verifyStatic();
        PropertiesUtil.loadProperties("foo.properties");
        
        PowerMockito.verifyNew(RequestUtil.class).withArguments(mockConfiguration);
    }
    
    @Test
    public void testConstructorWithPropertiesFileNotFound() throws Exception {
        
        new JsonPathLibrary("non-existent");
        
   }

    @Test
    public void testJsonElementShouldMatch() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = "source";

        String path = "$.store.book[0].category";
        
        String value = "reference";
        
        doReturn(Boolean.TRUE).when(spyLib).jsonElementShouldMatch(eq(source), eq(path), eq(value), (String)eq(null));
        
        boolean match = spyLib.jsonElementShouldMatch(source, "$.store.book[0].category", "reference");

        assertTrue("The element count should have matched", match);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonElementShouldMatch(eq(source), eq(path), eq(value), eq("FOO"), (String)eq(null));
        
        match = spyLib.jsonElementShouldMatch(source, "$.store.book[0].category", "reference", "FOO");

        assertTrue("The element count should have matched", match);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonElementShouldMatch(eq(source), eq(path), eq(value), eq("FOO"), eq("DATA"), (String)eq(null));
        
        match = spyLib.jsonElementShouldMatch(source, "$.store.book[0].category", "reference", "FOO", "DATA");

        assertTrue("The element count should have matched", match);
        
        doReturn("reference").when(spyLib).findJsonElement(eq(source), eq(path), eq("FOO"), eq("DATA"), eq("CONTENTTYPE"));
        
        match = spyLib.jsonElementShouldMatch(source, "$.store.book[0].category", "reference", "FOO", "DATA", "CONTENTTYPE");

        assertTrue("The element count should have matched", match);

    }

    @Test(expected = JsonNotEqualException.class)
    public void testJsonElementShouldMatchNoMatch() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = "source";

        doReturn("foobar").when(spyLib).findJsonElement(eq(source), eq("PATH"), eq("FOO"), eq("DATA"), eq("CONTENTTYPE"));
        
        spyLib.jsonElementShouldMatch(source, "PATH", "reference", "FOO", "DATA", "CONTENTTYPE");

        fail("The element should not have matched");

    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonElementShouldMatchNull() throws Exception {
    	
        String source = "source";

        lib.jsonElementShouldMatch(source, "PATH", null, "FOO", "DATA", "CONTENTTYPE");

        fail("The element should not have matched");
    }

    @Test
    public void testShouldHaveElementCount() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = "source";

        String path = "$.store.book[0].category";
        
        doReturn(Boolean.TRUE).when(spyLib).jsonShouldHaveElementCount(eq(source), eq(path), eq(5), (String)eq(null));
        
        boolean match = spyLib.jsonShouldHaveElementCount(source, path, 5);

        assertTrue("The element count should have matched", match);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonShouldHaveElementCount(eq(source), eq(path), eq(5), eq("METHOD"), (String)eq(null));
        
        match = spyLib.jsonShouldHaveElementCount(source, path, 5, "METHOD");

        assertTrue("The element count should have matched", match);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonShouldHaveElementCount(eq(source), eq(path), eq(5), eq("METHOD"), eq("DATA"), (String)eq(null));
        
        match = spyLib.jsonShouldHaveElementCount(source, path, 5, "METHOD", "DATA");

        assertTrue("The element count should have matched", match);
        
        when(util.readSource(eq(source), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn("JSONDATA");
        
        when(JsonPath.read(eq("JSONDATA"), eq(path))).thenReturn("SINGE_ITEM");
        
        match = spyLib.jsonShouldHaveElementCount(source, path, 1, "METHOD", "DATA", "CONTENTTYPE");

        assertTrue("The element count should have matched", match);
        
        when(JsonPath.read(eq("JSONDATA"), eq(path))).thenReturn(Arrays.asList("1", "2", "3"));
        
        match = spyLib.jsonShouldHaveElementCount(source, path, 3, "METHOD", "DATA", "CONTENTTYPE");

        assertTrue("The element count should have matched", match);
  
    }
    
    @Test(expected = JsonElementNotFoundException.class)
    public void testShouldHaveElementCountSingleTooMany() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = "source";

        String path = "$.store.book[0].category";
        
        when(util.readSource(eq(source), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn("JSONDATA");
        
        when(JsonPath.read(eq("JSONDATA"), eq(path))).thenReturn("SINGE_ITEM");
        
        spyLib.jsonShouldHaveElementCount(source, path, 5, "METHOD", "DATA", "CONTENTTYPE");

        fail("Should have gotten exception");
  
    }
    
    @Test(expected = JsonNotEqualException.class)
    public void testShouldHaveElementCountManyTooSingle() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = "source";

        String path = "$.store.book[0].category";
        
        when(util.readSource(eq(source), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn("JSONDATA");
        
        when(JsonPath.read(eq("JSONDATA"), eq(path))).thenReturn(Arrays.asList("1", "2"));
        
        spyLib.jsonShouldHaveElementCount(source, path, 1, "METHOD", "DATA", "CONTENTTYPE");

        fail("Should have gotten exception");
  
    }
    
    @Test(expected = JsonElementNotFoundException.class)
    public void testShouldHaveElementCountNotFound() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = "source";

        String path = "$.store.book[0].category";
        
        when(util.readSource(eq(source), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn("JSONDATA");
        
        when(JsonPath.read(eq("JSONDATA"), eq(path))).thenReturn(null);
        
        spyLib.jsonShouldHaveElementCount(source, path, 1, "METHOD", "DATA", "CONTENTTYPE");

        fail("Should have gotten exception");
  
    }
    
    @Test
    public void testFindJsonElement() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("test.json"));

        String expected = "reference";
        
        String path = "$.store.book[0].category";

        doReturn(expected).when(spyLib).findJsonElement(eq(source), eq(path), (String)eq(null));
        
        Object element = spyLib.findJsonElement(source, path);

        assertEquals("The elements should be equal", expected, element);
        
        doReturn(expected).when(spyLib).findJsonElement(eq(source), eq(path), eq("METHOD"), (String)eq(null));
        
        element = spyLib.findJsonElement(source, path, "METHOD");

        assertEquals("The elements should be equal", expected, element);
        
        doReturn(expected).when(spyLib).findJsonElement(eq(source), eq(path), eq("METHOD"), eq("DATA"), (String)eq(null));
        
        element = spyLib.findJsonElement(source, path, "METHOD", "DATA");

        assertEquals("The elements should be equal", expected, element);
        
        Mockito.when(util.readSource(source, "METHOD", "DATA", "CONTENTTYPE")).thenReturn("JSON");
        
        Mockito.when(JsonPath.read("JSON", path)).thenReturn(expected);
        
        element = spyLib.findJsonElement(source, path, "METHOD", "DATA", "CONTENTTYPE");

        assertEquals("The elements should be equal", expected, element);
    }

    @Test(expected = JsonElementNotFoundException.class)
    public void testFindJsonElementNotFound() throws Exception {

    	Mockito.when(util.readSource("SOURCE", "METHOD", "DATA", "CONTENTTYPE")).thenReturn("JSON");
        
        Mockito.when(JsonPath.read("JSON", "PATH")).thenThrow(new PathNotFoundException(""));
        
        lib.findJsonElement("SOURCE", "PATH", "METHOD", "DATA", "CONTENTTYPE");

        fail("Should have thrown exception");
    }

    @Test
    public void testFindJsonElementList() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String source = "SOURCE";

        List<String> expected = Arrays.asList("1", "2", "3");
        
        String path = "$.store.book[0].category";

        doReturn(expected).when(spyLib).findJsonElementList(eq(source), eq(path), (String)eq(null));
        
        Object element = spyLib.findJsonElementList(source, path);

        assertEquals("The elements should be equal", expected, element);
        
        doReturn(expected).when(spyLib).findJsonElementList(eq(source), eq(path), eq("METHOD"), (String)eq(null));
        
        element = spyLib.findJsonElementList(source, path, "METHOD");

        assertEquals("The elements should be equal", expected, element);
        
        doReturn(expected).when(spyLib).findJsonElementList(eq(source), eq(path), eq("METHOD"), eq("DATA"), (String)eq(null));
        
        element = spyLib.findJsonElementList(source, path, "METHOD", "DATA");

        assertEquals("The elements should be equal", expected, element);
        
        Mockito.when(util.readSource(source, "METHOD", "DATA", "CONTENTTYPE")).thenReturn("JSON");
        
        Mockito.when(JsonPath.read("JSON", path)).thenReturn(expected);
        
        element = spyLib.findJsonElementList(source, path, "METHOD", "DATA", "CONTENTTYPE");

        assertEquals("The elements should be equal", expected, element);
    }
    
    @Test(expected = JsonElementNotFoundException.class)
    public void testFindJsonElementListNotFound() throws Exception {

    	Mockito.when(util.readSource("SOURCE", "METHOD", "DATA", "CONTENTTYPE")).thenReturn("JSON");
        
        Mockito.when(JsonPath.read("JSON", "PATH")).thenThrow(new PathNotFoundException(""));
        
        lib.findJsonElementList("SOURCE", "PATH", "METHOD", "DATA", "CONTENTTYPE");

        fail("Should have thrown exception");
    }

    @Test
    public void testJsonShouldBeEqual() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String from = "{foo: bar}";
        String to = "{foo: bar}";

        Mockito.when(util.readSource(eq(from), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(from);
        Mockito.when(util.readSource(eq(to), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(to);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonShouldBeEqual(from, to, false);
        
        boolean equal = spyLib.jsonShouldBeEqual(from, to);

        assertTrue("The elements should be equal", equal);
        
        // Reset the spy
        spyLib = Mockito.spy(lib);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonShouldBeEqual(eq(from), eq(to), eq(false), (String)eq(null));
        
        equal = spyLib.jsonShouldBeEqual(from, to, false);

        assertTrue("The elements should be equal", equal);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonShouldBeEqual(eq(from), eq(to), eq(false), eq("METHOD"), (String)eq(null));
        
        equal = spyLib.jsonShouldBeEqual(from, to, false, "METHOD");

        assertTrue("The elements should be equal", equal);
        
        doReturn(Boolean.TRUE).when(spyLib).jsonShouldBeEqual(eq(from), eq(to), eq(false), eq("METHOD"), eq("DATA"), (String)eq(null));
        
        equal = spyLib.jsonShouldBeEqual(from, to, false, "METHOD", "DATA");

        assertTrue("The elements should be equal", equal);
        
        equal = spyLib.jsonShouldBeEqual(from, to, false, "METHOD", "DATA", "CONTENTTYPE");

        assertTrue("The elements should be equal", equal);
    }
    
    @Test
    public void testJsonShouldBeEqualNotExact() throws Exception {

    	JsonPathLibrary spyLib = Mockito.spy(lib);
    	
        String from = "{foo: bar, abc: xyz}";
        String to = "{abc: xyz, foo: bar}";

        Mockito.when(util.readSource(eq(from), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(from);
        Mockito.when(util.readSource(eq(to), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(to);
        
        boolean equal = spyLib.jsonShouldBeEqual(from, to, false, "METHOD", "DATA", "CONTENTTYPE");

        assertTrue("The elements should be equal", equal);
    }

    @Test(expected = JsonNotEqualException.class)
    public void testJsonShouldBeEqualNoMatch() throws Exception {

        String from = "{foo: bar}";
        String to = "{foo: xyz}";

        Mockito.when(util.readSource(eq(from), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(from);
        Mockito.when(util.readSource(eq(to), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(to);
        
        lib.jsonShouldBeEqual(from, to, false, "METHOD", "DATA", "CONTENTTYPE");

    }

    @Test(expected = JsonNotValidException.class)
    public void testJsonShouldBeEqualNoMatchBlank() throws Exception {

    	String from = "";
        String to = "";

        Mockito.when(util.readSource(eq(from), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(from);
        Mockito.when(util.readSource(eq(to), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(to);
        
        lib.jsonShouldBeEqual(from, to, false, "METHOD", "DATA", "CONTENTTYPE");
    }

    @Test(expected = JsonNotValidException.class)
    public void testJsonShouldBeEqualNoMatchNull() throws Exception {

    	String from = "from";
        String to = "to";

        Mockito.when(util.readSource(eq(from), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(null);
        Mockito.when(util.readSource(eq(to), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(null);
        
        lib.jsonShouldBeEqual(from, to, false, "METHOD", "DATA", "CONTENTTYPE");

    }

    @Test
    public void testJsonShouldBeEqualExact() throws Exception {

    	String from = "{foo: bar}";
        String to = "{foo: bar}";

        Mockito.when(util.readSource(eq(from), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(from);
        Mockito.when(util.readSource(eq(to), eq("METHOD"), eq("DATA"), eq("CONTENTTYPE"))).thenReturn(to);
        
        boolean equal = lib.jsonShouldBeEqual(from, to, true, "METHOD", "DATA", "CONTENTTYPE");

        assertTrue("The elements should be equal", equal);
    }
//
//    @Test(expected = JsonNotEqualException.class)
//    public void testJsonShouldBeEqualExactNoMatch() throws Exception {
//
//        String from = "{foo: bar}";
//        String to = "{foo: xyz}";
//
//        Mockito.when(util.readSource(Matchers.eq(from))).thenReturn(from);
//        Mockito.when(util.readSource(Matchers.eq(to))).thenReturn(to);
//        
//        lib.jsonShouldBeEqual(from, to, true);
//    }

}
