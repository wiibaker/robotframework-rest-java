package org.wuokko.robot.restlib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.http.client.fluent.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

@RunWith(MockitoJUnitRunner.class)
public class JsonDiffTest {

    JsonDiff diff = new JsonDiff();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Request.class);
    }

    @Test
    public void testCompare() throws IOException {

        boolean equal = diff.compare("{foo: bar}", "{foo: bar}");

        assertTrue("The jsons should have matched", equal);

    }
    
    @Test
    public void testCompareMultipleFields() throws IOException {

        boolean equal = diff.compare("{foo: bar, abc: xyz}", "{foo: bar, abc: xyz}");

        assertTrue("The jsons should have matched", equal);

    }
    
    @Test
    public void testCompareFieldNoMatch() throws IOException {

        boolean equal = diff.compare("{abc: bar}", "{foo: bar}");

        assertFalse("The jsons should not have matched", equal);

    }
    
    @Test
    public void testCompareValueNoMatch() throws IOException {

        boolean equal = diff.compare("{foo: abc}", "{foo: bar}");

        assertFalse("The jsons should not have matched", equal);

    }
    
    @Test
    public void testCompareMultipleFieldsValuesNoMatch() throws IOException {

        boolean equal = diff.compare("{foo: car, abc: xyz}", "{foo: bar, abc: xyz}");

        assertFalse("The jsons should NOT have matched", equal);
        
        equal = diff.compare("{foo: bar, abc: car}", "{foo: bar, abc: xyz}");

        assertFalse("The jsons should NOT have matched", equal);

    }
    
    @Test
    public void testCompareMultipleFieldsFieldNoMatch() throws IOException {

        boolean equal = diff.compare("{foo: bar, car: xyz}", "{foo: bar, abc: xyz}");

        assertFalse("The jsons should NOT have matched", equal);
        
        equal = diff.compare("{foo: bar, abc: car}", "{foo: bar, abc: xyz}");

        assertFalse("The jsons should NOT have matched", equal);

    }
    
    @Test
    public void testCompareNull() throws IOException {

        boolean equal = diff.compare(null, "{foo: bar}");

        assertFalse("The jsons should NOT have matched", equal);

        equal = diff.compare("{foo: bar}", null);

        assertFalse("The jsons should NOT have matched", equal);
    }
    
    @Test
    public void testCompareBasicObjects() throws IOException {

        boolean equal = diff.compareBasicObjects("foo", "foo", "key");

        assertTrue("The objects should have matched", equal);

        equal = diff.compareBasicObjects(new Integer(5), new Integer(5), "key");

        assertTrue("The objects should have matched", equal);

        equal = diff.compareBasicObjects(Boolean.FALSE, Boolean.FALSE, "key");

        assertTrue("The objects should have matched", equal);

    }

    @Test
    public void testCompareBasicObjectsNoMatch() throws IOException {

        boolean equal = diff.compareBasicObjects("foo", "bar", "key");

        assertFalse("The objects should NOT have matched", equal);

        equal = diff.compareBasicObjects(new Integer(5), new Integer(6), "key");

        assertFalse("The objects should NOT have matched", equal);

        equal = diff.compareBasicObjects(Boolean.FALSE, Boolean.TRUE, "key");

        assertFalse("The objects should NOT have matched", equal);

    }

    @Test
    public void testCompareJsonArrays() throws IOException {

        JSONArray fromArray = new JSONArray();
        JSONArray toArray = new JSONArray();

        // empty
        boolean equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertTrue("The arrays should have matched", equal);

        fromArray.add("foo");
        toArray.add("foo");

        equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertTrue("The arrays should have matched", equal);

        fromArray.add(new Integer(5));
        toArray.add(new Integer(5));

        equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertTrue("The arrays should have matched", equal);

        fromArray.add(Boolean.TRUE);
        toArray.add(Boolean.TRUE);

        equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertTrue("The arrays should have matched", equal);

    }

    @Test
    public void testCompareJsonArraysNoMatch() throws IOException {

        JSONArray fromArray = new JSONArray();
        JSONArray toArray = new JSONArray();

        fromArray.add("foo");

        // size diff
        boolean equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertFalse("The arrays should NOT have matched", equal);

        fromArray = new JSONArray();
        toArray = new JSONArray();
        fromArray.add("foo");
        toArray.add("bar");

        equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertFalse("The arrays should NOT have matched", equal);

        fromArray = new JSONArray();
        toArray = new JSONArray();
        fromArray.add(new Integer(5));
        toArray.add(new Integer(6));

        equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertFalse("The arrays should NOT have matched", equal);

        fromArray = new JSONArray();
        toArray = new JSONArray();
        fromArray.add(Boolean.TRUE);
        toArray.add(Boolean.FALSE);

        equal = diff.compareJsonArrays(fromArray, toArray, "path");

        assertFalse("The arrays should NOT have matched", equal);

    }

    @Test
    public void testCompareJsonObjects() throws IOException {

        JSONObject fromObject = new JSONObject();
        JSONObject toObjects = new JSONObject();

        // empty
        boolean equal = diff.compareJsonObjects(fromObject, toObjects, "path");

        assertTrue("The objects should have matched", equal);

        fromObject.put("first", "foo");
        toObjects.put("first", "foo");

        equal = diff.compareJsonObjects(fromObject, toObjects, "path");

        assertTrue("The objects should have matched", equal);

        fromObject.put("second", new Integer(5));
        toObjects.put("second", new Integer(5));

        equal = diff.compareJsonObjects(fromObject, toObjects, "path");

        assertTrue("The objects should have matched", equal);

        fromObject.put("third", Boolean.TRUE);
        toObjects.put("third", Boolean.TRUE);

        equal = diff.compareJsonObjects(fromObject, toObjects, "path");

        assertTrue("The objects should have matched", equal);

    }

    @Test
    public void testCompareJsonObjectsNoMatch() throws IOException {

        JSONObject fromObject = new JSONObject();
        JSONObject toObjects = new JSONObject();

        // size differ
        fromObject.put("first", "foo");
        boolean equal = diff.compareJsonObjects(fromObject, toObjects, "path");

        assertFalse("The objects should NOT have matched", equal);

        fromObject = new JSONObject();
        toObjects = new JSONObject();
        fromObject.put("first", "foo");
        toObjects.put("first", "bar");

        equal = diff.compareJsonObjects(fromObject, toObjects, "path");

        assertFalse("The objects should NOT have matched", equal);

    }

    @Test
    public void testCompareObjects() throws IOException {

        JSONObject fromObject = new JSONObject();
        JSONObject toObjects = new JSONObject();

        fromObject.put("first", "foo");
        toObjects.put("first", "foo");
        
        boolean equal = diff.compareObjects(fromObject, toObjects, "path");

        assertTrue("The objects should have matched", equal);
        
        JSONArray fromArray = new JSONArray();
        JSONArray toArray = new JSONArray();

        fromArray.add("foo");
        toArray.add("foo");
        
        equal = diff.compareObjects(fromArray, toArray, "path");

        assertTrue("The objects should have matched", equal);
        
        equal = diff.compareObjects("foo", "foo", "path");

        assertTrue("The objects should have matched", equal);
        
        equal = diff.compareObjects(Boolean.TRUE, Boolean.TRUE, "path");

        assertTrue("The objects should have matched", equal);
        
        equal = diff.compareObjects(new Long(123), new Long(123), "path");

        assertTrue("The objects should have matched", equal);
        
        equal = diff.compareObjects(new Double(456), new Double(456), "path");

        assertTrue("The objects should have matched", equal);
        
        equal = diff.compareObjects(new Integer(789), new Integer(789), "path");

        assertTrue("The objects should have matched", equal);
        
        equal = diff.compareObjects(null, null, "path");

        assertTrue("The objects should have matched", equal);

    }
    
    @Test
    public void testCompareObjectsNoMatch() throws IOException {

        JSONObject fromObject = new JSONObject();
        JSONObject toObjects = new JSONObject();

        fromObject.put("first", "foo");
        toObjects.put("first", "bar");
        
        boolean equal = diff.compareObjects(fromObject, toObjects, "path");

        assertFalse("The objects should have NOT matched", equal);
        
        JSONArray fromArray = new JSONArray();
        JSONArray toArray = new JSONArray();

        fromArray.add("foo");
        toArray.add("bar");
        
        equal = diff.compareObjects(fromArray, toArray, "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects("foo", "bar", "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects(Boolean.TRUE, Boolean.FALSE, "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects(new Long(123), new Long(312), "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects(new Double(456), new Double(654), "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects(new Integer(789), new Integer(987), "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects(null, "", "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects("", null, "path");

        assertFalse("The objects should have NOT matched", equal);
        
        equal = diff.compareObjects("foo", new Integer(123), "path");

        assertFalse("The objects should have NOT matched", equal);

    }
    
    @Test
    public void testCompareObjectsUnsupported() throws IOException {
       
        boolean equal = diff.compareObjects(new FooBar(), new FooBar(), "path");

        assertFalse("The objects should have NOT matched", equal);
    }
    
    class FooBar {
        
    }
}
