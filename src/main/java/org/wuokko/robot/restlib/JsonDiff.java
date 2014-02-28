package org.wuokko.robot.restlib;

import java.util.Map.Entry;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class JsonDiff implements Diff {

    private JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    protected JSONObject loadJSON(String json) {

        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) this.parser.parse(json);
        } catch (ParseException e) {
            System.out.println("*ERROR* Could not parse JSON!");
        }

        return jsonObject;
    }

    @Override
    public boolean compare(String fromObject, String toObject) {
        
        boolean equal = true;
        
        if(fromObject != null && toObject != null) {
            
            JSONObject fromJson = loadJSON(fromObject);
            JSONObject toJson = loadJSON(toObject);
            
            equal = compareObjects(fromJson, toJson, "");
        } else {
            System.out.println("*ERROR* Either from: " + fromObject + " or to: " + toObject + "  was null");
            equal = false;
        }
        
        
        return equal;
        
    }
    
    protected boolean compareObjects(Object fromObject, Object toObject, String path) {

        boolean equal = true;

        if (fromObject != null && toObject != null) {

            if (fromObject.getClass().equals(toObject.getClass())) {

                if (fromObject instanceof JSONObject) {
                    equal = compareJsonObjects((JSONObject) fromObject, (JSONObject) toObject, path);
                } else if (fromObject instanceof JSONArray) {
                    equal = compareJsonArrays((JSONArray) fromObject, (JSONArray) toObject, path);
                } else if (fromObject instanceof String) {
                    equal = compareBasicObjects(String.valueOf(fromObject), String.valueOf(toObject), path);
                } else if (fromObject instanceof Boolean) {
                    equal = compareBasicObjects((Boolean) fromObject, (Boolean) toObject, path);
                } else if (fromObject instanceof Long) {
                    equal = compareBasicObjects((Long) fromObject, (Long) toObject, path);
                } else if (fromObject instanceof Double) {
                    equal = compareBasicObjects((Double) fromObject, (Double) toObject, path);
                } else if (fromObject instanceof Integer) {
                    equal = compareBasicObjects((Integer) fromObject, (Integer) toObject, path);
                } else {
                    System.out.println("*ERROR* Object was unsupported type " + fromObject.getClass().getName());
                    equal = false;
                }
            } else {
                System.out.println("*ERROR* The types differ to: " + fromObject.getClass().getName() + " (" + fromObject + ") from: " + toObject.getClass().getName() + " (" + toObject + ")");
                equal = false;
            }
        }  else if(fromObject == null && toObject == null) {
            System.out.println("*DEBUG* Both to and from were null");
        } else {
            System.out.println("*ERROR* There was null (path " + path + ") value from: " + fromObject + " to: " + toObject);
            equal = false;
        }

        return equal;
    }

    protected boolean compareJsonObjects(JSONObject fromJson, JSONObject toJson, String key2) {

        boolean equal = true;

        for (Entry<String, Object> entry : fromJson.entrySet()) {
            String path = key2 + " -> " + entry.getKey();

            if (!toJson.containsKey(entry.getKey())) {
                System.out.println("*ERROR* toJson does not contain key '" + path + "'");

                equal = false;
                break;

            } else {

                Object fromObject = fromJson.get(entry.getKey());
                Object toObject = toJson.get(entry.getKey());

                equal = compareObjects(fromObject, toObject, path);
            }

        }

        return equal;

    }

    protected boolean compareJsonArrays(JSONArray fromJson, JSONArray toJson, String path) {

        boolean equal = true;
        
        if (fromJson.size() != toJson.size()) {
            System.out.println("*ERROR* The array sizes differ from: " + fromJson.size() + " to: " + toJson.size());
            equal = false;
        }

        for (int i = 0; i < fromJson.size(); i++) {
            Object fromObject = fromJson.get(i);
            if (toJson.size() > i) {
                Object toObject = toJson.get(i);
                equal = compareObjects(fromObject, toObject, path + " -> array");

            } else {
                System.out.println("*ERROR* Ran out of values from toArray from value: " + fromObject);
                equal = false;
                break;
            }
        }

        return equal;
    }

    protected boolean compareBasicObjects(Object fromString, Object toString, String key) {
        
        boolean equal = true;
        
        if (fromString.equals(toString)) {
            System.out.println("*DEBUG* The values matched for key '" + key + "'");
        } else {
            System.out.println("*ERROR* The values for key " + key + " did not match \nfrom: " + fromString + " \nto: " + toString);
            equal = false;
        }
        
        return equal;
                
    }

}
