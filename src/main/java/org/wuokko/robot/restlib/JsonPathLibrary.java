package org.wuokko.robot.restlib;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.wuokko.robot.restlib.exception.JsonElementNotFoundException;
import org.wuokko.robot.restlib.exception.JsonNotEqualException;
import org.wuokko.robot.restlib.exception.JsonNotValidException;
import org.wuokko.robot.restlib.util.PropertiesUtil;
import org.wuokko.robot.restlib.util.RequestUtil;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * Robot Framework REST Library is a library for testing REST APIs in JSON
 * format.
 * 
 * It uses Javalib Core (https://github.com/robotframework/JavalibCore) as
 * framework.
 * 
 * The source for the JSON to test can be given either as URI (file or http) or
 * as the full JSON content itself.
 * 
 * = Runtime options =
 * 
 * There is possibility to cache the URI results into a simple in-memory cache
 * with system property "use.uri.cache". The cache works within a single test
 * case only.
 * 
 * Example:
 * 
 * mvn robotframework:run -Duse.uri.cache=true
 * 
 * = Properties =
 * 
 * Also you can use properties file to set few options. The file named
 * 'robot-rest-lib.properties' is looked from the classpath. The options are:
 * 
 *  | *Property* | *Type* | *Default* |
 *  | connection.timeout | int | 1000 |
 *  | use.uri.cache | boolean | false |
 * 
 */
@RobotKeywords
public class JsonPathLibrary {

    private Diff diff = new JsonDiff();

    private static final String DEFAULT_PROPERTIES_FILE = "robot-rest-lib.properties";

    private Configuration config;
    
    private RequestUtil requestUtil;
    
    /**
     * Default constructor with no arguments.
     * 
     * Instantiates a library using the default 'robot-rest-lib.properties' properties file.
     */
    public JsonPathLibrary() {
    	this(DEFAULT_PROPERTIES_FILE);
    }
    
    /**
     * Instantiates a library with custom named properties file. Pass the properties file name as an argument to the library.
     */
    public JsonPathLibrary(String propertiesFile) {
    	config = PropertiesUtil.loadProperties(propertiesFile);
    	requestUtil = new RequestUtil(config);
    }
    
    @RobotKeyword
    public boolean jsonElementShouldMatch(String source, String jsonPath, Object value) throws Exception {
    	return jsonElementShouldMatch(source, jsonPath, value, null);
    }
    
    @RobotKeyword
    public boolean jsonElementShouldMatch(String source, String jsonPath, Object value, String method) throws Exception {
    	return jsonElementShouldMatch(source, jsonPath, value, method, null);
    }
    
    @RobotKeyword
    public boolean jsonElementShouldMatch(String source, String jsonPath, Object value, String method, String data) throws Exception {
    	return jsonElementShouldMatch(source, jsonPath, value, method, data, null);
    }
    
    /**
     * Checks if the given value matches the one found by the `jsonPath` from
     * the `source`.
     * 
     * Source can be either URI or the actual JSON content
     * 
     * You can add optional method (ie GET, POST, PUT), data or content type as parameters.
     * Method defaults to GET.
     * 
     * Example:
     * | Json Element Should Match | http://example.com/test.json | $.element.param | hello |
     * | Json Element Should Match | { element: { param:hello } } | $.element.param | hello |
     */
    @RobotKeyword
    public boolean jsonElementShouldMatch(String source, String jsonPath, Object value, String method, String data, String contentType) throws Exception {

        boolean match = false;

        if (value == null) {
            throw new IllegalArgumentException("Given value was null");
        }

        String found = String.valueOf(findJsonElement(source, jsonPath, method, data, contentType));

        if (found.equals(value)) {
            System.out.println("*DEBUG* The values '" + found + "' and '" + value + "' did match");
            match = true;
        } else {
            System.out.println("*ERROR* The values '" + found + "' and '" + value + "' did not match");
            throw new JsonNotEqualException("The found value did not match, found '" + found + "', expected '" + value + "'");
        }

        return match;
    }

    /**
     * Checks if the given JSON contents are equal. See `Json Should Be Equal`
     * for more details
     * 
     * `from` and `to` can be either URI or the actual JSON content.
     * 
     * You can add optional method (ie GET, POST, PUT), data or content type as parameters.
     * Method defaults to GET.
     * 
     * Example:
     * | Json Should Be Equal | http://example.com/test.json | http://foobar.com/test.json |
     * | Json Should Be Equal | { element: { param:hello } } | { element: { param:hello } } |
     */
    
    @RobotKeyword
    public boolean jsonShouldBeEqual(String from, String to) throws Exception {
        return jsonShouldBeEqual(from, to, false);
    }
    
    @RobotKeyword
    public boolean jsonShouldBeEqual(String from, String to, boolean useExactMatch) throws Exception {
        return jsonShouldBeEqual(from, to, useExactMatch, null);
    }

    @RobotKeyword
    public boolean jsonShouldBeEqual(String from, String to, boolean useExactMatch, String method) throws Exception {
        return jsonShouldBeEqual(from, to, useExactMatch, method, null);
    }
    
    @RobotKeyword
    public boolean jsonShouldBeEqual(String from, String to, boolean useExactMatch, String method, String data) throws Exception {
        return jsonShouldBeEqual(from, to, useExactMatch, method, data, null);
    }

    /**
     * Checks if the given JSON contents are equal. The third parameter
     * specifies whether exact string match should be used or diffing by the
     * JSON objects ie. the order of the attributes does not matter.
     * 
     * `from` and `to` can be either URI or the actual JSON content.
     * 
     * You can add optional method (ie GET, POST, PUT), data or content type as parameters.
     * Method defaults to GET.
     * 
     * Example:
     * | Json Should Be Equal | http://example.com/test.json | http://foobar.com/test.json | true |
     * | Json Should Be Equal | { element: { param:hello, foo:bar } } | { element: { foo:bar, param:hello } } | true |
     * 
     */
    @RobotKeyword
    public boolean jsonShouldBeEqual(String from, String to, boolean useExactMatch, String method, String data, String contentType) throws Exception {
        System.out.println("*DEBUG* Comparing JSON sources");

        boolean equal = false;

        String fromJson = requestUtil.readSource(from, method, data, contentType);
        String toJson = requestUtil.readSource(to, method, data, contentType);
        
        if (StringUtils.isNotBlank(fromJson) && StringUtils.isNotBlank(toJson)) {
            if (useExactMatch) {
                if (fromJson.equals(toJson)) {
                    System.out.println("*DEBUG* JSON strings are equal by exact compare");
                    equal = true;
                } else {
                    System.out.println("*ERROR* JSON strings are NOT equal by exact compare");
                    equal = false;
                    throw new JsonNotEqualException("JSON strings are NOT equal by exact compare");
                }
            } else {
                equal = diff.compare(fromJson, toJson);
                if (!equal) {
                    throw new JsonNotEqualException("JSON strings are NOT equal by compare");
                }
            }
        } else {
            System.out.println("*ERROR* Either from or to JSON was empty");
            throw new JsonNotValidException("One of the JSON strings is empty");
        }

        return equal;
    }

    @RobotKeyword
    public Object findJsonElement(String source, String jsonPath) throws Exception {
    	return findJsonElement(source, jsonPath, null);
    }
    
    @RobotKeyword
    public Object findJsonElement(String source, String jsonPath, String method) throws Exception {
    	return findJsonElement(source, jsonPath, method, null);
    }
    
    @RobotKeyword
    public Object findJsonElement(String source, String jsonPath, String method, String data) throws Exception {
    	return findJsonElement(source, jsonPath, method, data, null);
    }
    
    /**
     * Find JSON element by `jsonPath` from the `source` and return its value if found.
     * 
     * `source` can be either URI or the actual JSON content.
     * 
     * You can add optional method (ie GET, POST, PUT), data or content type as parameters.
     * Method defaults to GET.
     * 
     * Example:
     * | Find Json Element | http://example.com/test.json | $.foo.bar |
     * | Find Json Element | {element: { param:hello, foo:bar } } | $.element.foo |
     * 
     */
    @RobotKeyword
    public Object findJsonElement(String source, String jsonPath, String method, String data, String contentType) throws Exception {
        System.out.println("*DEBUG* Reading jsonPath: " + jsonPath);

        String json = requestUtil.readSource(source, method, data, contentType);

        Object value;

        try {
            value = JsonPath.read(json, jsonPath);
        } catch (PathNotFoundException e) {
            throw new JsonElementNotFoundException("Path '" + jsonPath + "' was not found in JSON");
        }

        return value;
    }
    
    public List<Object> findJsonElementList(String source, String jsonPath) throws Exception {
    	return findJsonElementList(source, jsonPath, null);
    }
    
    public List<Object> findJsonElementList(String source, String jsonPath, String method) throws Exception {
    	return findJsonElementList(source, jsonPath, method, null);
    }
    
    public List<Object> findJsonElementList(String source, String jsonPath, String method, String data) throws Exception {
    	return findJsonElementList(source, jsonPath, method, data, null);
    }

    /**
     * Find JSON element list by `jsonPath` from the `source` and return its value if found.
     * 
     * `source` can be either URI or the actual JSON content.
     * 
     * You can add optional method (ie GET, POST, PUT), data or content type as parameters.
     * Method defaults to GET.
     * 
     * Example:
     * | Find Json Element | http://example.com/test.json | $.foo[*] |
     * | Find Json Element | {element: [ {param:hello}, {foo:bar} ] } | $.element[*] |
     * 
     */
    @RobotKeyword
    public List<Object> findJsonElementList(String source, String jsonPath, String method, String data, String contentType) throws Exception {
        System.out.println("*DEBUG* Reading jsonPath: " + jsonPath);

        String json = requestUtil.readSource(source, method, data, contentType);

        List<Object> elements;

        try {
            elements = JsonPath.read(json, jsonPath);
        } catch (PathNotFoundException e) {
            throw new JsonElementNotFoundException("Path '" + jsonPath + "' was not found in JSON");
        }

        return elements;
    }

    @RobotKeyword
    public boolean jsonShouldHaveElementCount(String source, String jsonPath, Integer count) throws Exception {
    	return jsonShouldHaveElementCount(source, jsonPath, count, null);
    }
    
    @RobotKeyword
    public boolean jsonShouldHaveElementCount(String source, String jsonPath, Integer count, String method) throws Exception {
    	return jsonShouldHaveElementCount(source, jsonPath, count, method, null);
    }
    
    @RobotKeyword
    public boolean jsonShouldHaveElementCount(String source, String jsonPath, Integer count, String method, String data) throws Exception {
    	return jsonShouldHaveElementCount(source, jsonPath, count, method, data, null);
    }
    
    /**
     * Find JSON element by `jsonPath` from the `source` and check if the amount of found elements matches the given `count`.
     * 
     * `source` can be either URI or the actual JSON content.
     * 
     * You can add optional method (ie GET, POST, PUT), data or content type as parameters.
     * Method defaults to GET.
     * 
     * Example:
     * | Json Should Have Element Count | http://example.com/test.json | $.foo[*] | 3 |
     * | Json Should Have Element Count | {element: [ {param:hello}, {foo:bar} ] } | $.element[*] | 2 |
     * 
     */
    @SuppressWarnings("unchecked")
    @RobotKeyword
    public boolean jsonShouldHaveElementCount(String source, String jsonPath, Integer count, String method, String data, String contentType) throws Exception {
        boolean match = false;

        System.out.println("*DEBUG* Reading jsonPath: " + jsonPath);

        String json = requestUtil.readSource(source, method, data, contentType);

        List<Object> elements = null;

        Object object = null;

        try {

            object = JsonPath.read(json, jsonPath);

        } catch (PathNotFoundException e) {
            throw new JsonElementNotFoundException("Path '" + jsonPath + "' was not found in JSON");
        }
        if (object != null) {
            // TODO: Find a way to do this without suppressing the warning
            if (object instanceof List<?>) {
                elements = (List<Object>) object;
                if (CollectionUtils.isNotEmpty(elements)) {
                    match = (elements.size() == count);

                    if (!match) {
                        System.out.println("*ERROR* Element counts did not match. Expected '" + count + "', got '" + elements.size() + "'");
                        throw new JsonNotEqualException("Element counts did not match. Expected '" + count + "', got '" + elements.size() + "'");
                    }

                } else {
                    // In practice, it's impossible to end here.
                    System.out.println("*ERROR* Could not find elements from '" + jsonPath + "'");
                    throw new JsonElementNotFoundException("Could not find elements from '" + jsonPath + "'");
                }
            } else if (count == 1) {
                System.out.println("*DEBUG* Found 1 item as expected from '" + jsonPath + "'");
                match = true;
            } else {
                System.out.println("*ERROR* Found 1 item, but expected '" + count + "'");
                throw new JsonElementNotFoundException("Found 1 item, but expected '" + count + "'");
            }
        } else {
            System.out.println("*ERROR* Could not find elements from '" + jsonPath + "'");
            throw new JsonElementNotFoundException("Could not find elements from '" + jsonPath + "'");
        }

        return match;
    }

}
