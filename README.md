Robot Framework library to test REST APIs
=========================================

This is a Robot Framework library to test REST APIs. Currently it
supports JSON output, in future also XML. The main purpose is to
have a library to run smoke tests against deployed REST applications.

Usage
-----

The easiest way to use this library is with Robot Framerowk JavalibCore
https://github.com/robotframework/JavalibCore

To use this library by adding the following dependencies to 
your pom.xml:

    <dependency>
		<groupId>org.robotframework</groupId>
		<artifactId>javalib-core</artifactId>
		<version>1.2</version>
	</dependency>
	
	<dependency>
		<groupId>org.wuokko.robot</groupId>
		<artifactId>robotframework-rest-java</artifactId>
		<version>0.1.0</version>
	</dependency>

To use the library in your Robot Framework test, add the following in Settings part

	*** Settings ***
	Library	 	org.robotframework.javalib.library.AnnotationLibrary	 org/wuokko/robot/restlib/*.class

The library uses JSON Path to find the elements. Read more about it and its syntax from
http://goessner.net/articles/JsonPath/

You have following keywords to use

	Find Json Element			JSON/URI	JSONPath
	Find Json Element List		JSON/URI	JSONPath
	Json Element Should Match	JSON/URI	JSONPath	Match value
	Json Should Be Equal		JSON/URI	JSON/URI
	Json Should Be Equal		JSON/URI	JSON/URI	useExactMatch (boolean)
	Should Have Element Count	JSON/URI	Count		JSONPath

You can pass either URI to the JSON (ie. your REST api output) or the JSON as string.

You can also add system property "use.uri.cache" to use simple in-memory cache
to cache the results of the URI requests. ie

	mvn robotframework:run -Duse.uri.cache=true

The caching currently only works
within test cases. So you can do multiple checks on the same URI within a test
case without reloading the JSON every time. 

Dependencies
------------

You need following dependencies to use this library

	com.jayway.jsonpath:json-path-assert:jar:0.9.1
	org.slf4j:slf4j-api:jar:1.7.5
	com.thoughtworks.paranamer:paranamer:jar:1.1.2
	org.hamcrest:hamcrest-library:jar:1.3
	org.apache.httpcomponents:fluent-hc:jar:4.3.2
	org.apache.httpcomponents:httpcore:jar:4.3.1
	commons-io:commons-io:jar:2.4
	commons-logging:commons-logging:jar:1.1.3
	org.apache.httpcomponents:httpclient:jar:4.3.2
	org.robotframework:javalib-core:jar:1.2
	commons-collections:commons-collections:jar:3.2
	net.minidev:json-smart:jar:1.2
	org.hamcrest:hamcrest-core:jar:1.3
	org.apache.commons:commons-lang3:jar:3.1
	com.jayway.jsonpath:json-path:jar:0.9.1
	commons-codec:commons-codec:jar:1.6
