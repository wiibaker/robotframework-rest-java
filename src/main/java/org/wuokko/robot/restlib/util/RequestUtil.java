package org.wuokko.robot.restlib.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

public class RequestUtil {
	
	public static final String KEY_CONNECTION_TIMEOUT = "connection.timeout";
	
	public static final String KEY_USE_URI_CACHE = "use.uri.cache";
	
    private static final int MAX_CACHE_SIZE = 100;

    private static int CONNECTION_TIMEOUT = 1000;

    protected Map<URI, String> uriCache = Collections.synchronizedMap(new LRUMap<URI, String>(MAX_CACHE_SIZE));

    private Boolean useCache = Boolean.valueOf(System.getProperty("use.uri.cache"));

    public RequestUtil(Configuration config) {
    	if(config != null) {
            
            if(config.containsKey(KEY_CONNECTION_TIMEOUT)) {
                CONNECTION_TIMEOUT = config.getInt(KEY_CONNECTION_TIMEOUT);
                System.out.println("[Robot-Rest-Lib] Set connection time to '" + CONNECTION_TIMEOUT + "'");
            }
            
            if(config.containsKey(KEY_USE_URI_CACHE)) {
                useCache = config.getBoolean(KEY_USE_URI_CACHE);
                System.out.println("[Robot-Rest-Lib] Using URI cache: " + useCache);
            }
        }
	}
    
    public String readSource(String source) {
    	return readSource(source, "GET");
    }
    
    public String readSource(String source, String method) {
    	return readSource(source, method, null);
    }
    
    public String readSource(String source, String method, String data) {
    	return readSource(source, method, data, null);
    }
    
	public String readSource(String source, String method, String data, String contentType) {

        String json = null;

        if (StringUtils.isNotBlank(source)) {

            URI uri = getURI(source);

            if (uri != null) {
                json = loadURI(uri, method, data, contentType);
            } else {
                System.out.println("*DEBUG* The source is JSON");
                json = source;
            }

        } else {
            System.out.println("*ERROR* The source was empty or null: " + source);
        }

        return json;
    }

    protected String loadURI(URI uri, String method, String data, String contentTypeString) {

        String json = null;
        
        if (uri != null && StringUtils.isNotBlank(method)) {

            System.out.println("*DEBUG* Use cache: " + useCache);

            if (useCache) {
                json = uriCache.get(uri);
            }

            if (json == null) {

                System.out.println("*DEBUG* Did not find result from cache");

                // Check if the source is an URL
                try {

                    System.out.println("*TRACE* Loading the JSON from the URI");

                    if ("file".equals(uri.getScheme())) {
                        System.out.println("*DEBUG* Loading file system URI");
                        json = FileUtils.readFileToString(new File(uri));
                    } else {
                        System.out.println("*DEBUG* Loading external URI");
                        
                        ContentType contentType = ContentType.APPLICATION_JSON;
                        
                        if(StringUtils.isNotBlank(contentTypeString)) {
                        	contentType = ContentType.create(contentTypeString);
                        	System.out.println("*DEBUG* Created content type: " + contentType);
                        }
                        
                        Request request = null;

                        System.out.println("*DEBUG* Using method: " + method);
                        
                        switch (method) {
						case "GET":
							request = Request.Get(uri);
							break;
						case "POST":
							request = Request.Post(uri).bodyString(data, contentType);
							break;
						case "DELETE":
							request = Request.Delete(uri); 
							break;
						case "PUT":
							request = Request.Put(uri).bodyString(data, contentType); 
							break;
						default:
							break;
						}
                        
                        if(request != null) {
                        	json = request.connectTimeout(CONNECTION_TIMEOUT).socketTimeout(CONNECTION_TIMEOUT).execute().returnContent().asString();
                        	System.out.println("JSON: " + json);
                        } else {
                        	System.out.println("*ERROR* Could not find out request method, was: " + method);
                        }
                    }

                    if (json != null && useCache) {
                        System.out.println("*DEBUG* Storing value to the cache");
                        uriCache.put(uri, json);
                    }

                } catch (IOException e) {
                    System.out.println("*ERROR* Could not load json from URI " + uri + ", because " + e);
                }

            } else {
                System.out.println("*DEBUG* Found the result from cache");
            }
        } else {
            System.out.println("*DEBUG* Got invalid parameters - method: " + method + " - uri: " + uri);
        }

        return json;
    }

    protected URI getURI(String url) {

        URI uri = null;

        try {
        	// To be able to support Operation System Variables in Windows also
        	// we need to change all \ into /
        	url = url.replaceAll("\\\\", "/");
        	
            uri = new URI(url);
            System.out.println("*DEBUG* The source " + url + " is an URL");
        } catch (URISyntaxException e) {
        }

        return uri;
    }
	
}
