package org.wuokko.robot.restlib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

@RobotKeywords
public class JsonPathLibrary {

    private Diff diff = new JsonDiff();

    @RobotKeyword
    public boolean jsonShouldBeEqual(String from, String to) throws Exception {
        return jsonShouldBeEqual(from, to, false);
    }

    @RobotKeyword
    public boolean jsonShouldBeEqual(String from, String to, boolean useExactMatch) throws Exception {
        System.out.println("*DEBUG* Comparing JSON sources");

        boolean equal = false;

        String fromJson = readSource(from);
        String toJson = readSource(to);

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
            throw new JsonNotValidException("JSON strings are NOT equal by compare");
        }

        return equal;
    }

    @RobotKeyword
    public Object findJsonElement(String source, String jsonPath) throws Exception {
        System.out.println("*DEBUG* Reading jsonPath: " + jsonPath);

        String json = readSource(source);

        Object value;

        try {
            value = JsonPath.read(json, jsonPath);
         } catch(PathNotFoundException e) {
             throw new JsonElementNotFoundException("Path '" + jsonPath + "' was not found in JSON");
         }
        
        return value;
    }

    @RobotKeyword
    public List<Object> findJsonElementList(String source, String jsonPath) throws Exception {
        System.out.println("*DEBUG* Reading jsonPath: " + jsonPath);

        String json = readSource(source);

        List<Object> elements;
        
        try {
           elements = JsonPath.read(json, jsonPath);
        } catch(PathNotFoundException e) {
            throw new JsonElementNotFoundException("Path '" + jsonPath + "' was not found in JSON");
        }

        return elements;
    }

    @SuppressWarnings("unchecked")
    @RobotKeyword
    public boolean shouldHaveElementCount(String source, Integer count, String jsonPath) throws Exception {
        boolean match = false;

        System.out.println("*DEBUG* Reading jsonPath: " + jsonPath);

        String json = readSource(source);

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

    protected String readSource(String source) {

        String json = null;

        boolean isURL = false;

        if (StringUtils.isNotBlank(source)) {

            // Check if the source is an URL
            try {

                URI uri = getURI(source);

                if (uri != null) {

                    isURL = true;

                    System.out.println("*DEBUG* The source " + source + " is an URL");

                    System.out.println("*TRACE* Loading the JSON from the url");

                    if ("file".equals(uri.getScheme())) {
                        System.out.println("*DEBUG* Loading file system URI");
                        json = FileUtils.readFileToString(new File(uri));
                    } else {
                        System.out.println("*DEBUG* Loading external URI");
                        json = Request.Get(uri).connectTimeout(1000).socketTimeout(1000).execute().returnContent().asString();
                    }

                } else {
                    System.out.println("*DEBUG* The source is not an URL");
                }

            } catch (IOException e) {
                System.out.println("*ERROR* Could not load json from URL " + source + ", because " + e);
            }

            if (json == null && !isURL) {
                System.out.println("*DEBUG* The source is JSON");
                json = source;
            }

        } else {
            System.out.println("*ERROR* The source was empty or null: " + source);
        }

        return json;
    }

    private URI getURI(String url) {

        URI uri = null;

        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
        }

        return uri;
    }

}
