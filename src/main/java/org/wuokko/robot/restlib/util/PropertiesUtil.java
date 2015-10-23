package org.wuokko.robot.restlib.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertiesUtil {

	public static Configuration loadProperties(String propertiesFile) {
		
		Configuration config = null;
		
        try {
            config = new PropertiesConfiguration(propertiesFile);
            System.out.println("config: " + config);
            System.out.println("[Robot-Rest-Lib] Found properties file '" + propertiesFile + "'");
        } catch (ConfigurationException e) {
            System.out.println("[Robot-Rest-Lib] Did not find properties file '" + propertiesFile + "', using defaults");
        }

        return config;
        
    }
	
}
