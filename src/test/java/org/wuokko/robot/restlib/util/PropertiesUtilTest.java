package org.wuokko.robot.restlib.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertiesUtil.class})
public class PropertiesUtilTest {
    
    @Test
    public void testReadJsonSource() throws Exception {
       
        PropertiesConfiguration mockConfiguration = mock(PropertiesConfiguration.class);
        
        PowerMockito.whenNew(PropertiesConfiguration.class).withArguments("mock-configuration.properties").thenReturn(mockConfiguration);
        
        Configuration configuration = PropertiesUtil.loadProperties("mock-configuration.properties");
        
        PowerMockito.verifyNew(PropertiesConfiguration.class).withArguments("mock-configuration.properties");

        assertNotNull(configuration);
        
    }
    
    @Test
    public void testReadJsonSourceNotFound() throws Exception {
       
    	Configuration configuration = PropertiesUtil.loadProperties("foo-bar");

        assertNull(configuration);
        
    }

}
