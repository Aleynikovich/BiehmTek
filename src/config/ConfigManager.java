package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Singleton configuration manager that loads properties from files.
 * Supports loading multiple configuration files (robot.properties, plc.properties, etc.)
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 */
public class ConfigManager {
    
    private static ConfigManager instance;
    private Properties robotConfig;
    private Properties plcConfig;
    private String configBasePath;
    
    private ConfigManager() {
        this.robotConfig = new Properties();
        this.plcConfig = new Properties();
        this.configBasePath = "/home/KRC/configs/";
    }
    
    /**
     * Get singleton instance of ConfigManager
     * @return ConfigManager instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * Set the base path for configuration files
     * @param path Base path on KRC drive
     */
    public void setConfigBasePath(String path) {
        this.configBasePath = path;
    }
    
    /**
     * Load robot configuration from robot.properties
     * @throws IOException if file cannot be read
     */
    public void loadRobotConfig() throws IOException {
        String filePath = configBasePath + "robot.properties";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            robotConfig.load(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
    
    /**
     * Load PLC configuration from plc.properties
     * @throws IOException if file cannot be read
     */
    public void loadPlcConfig() throws IOException {
        String filePath = configBasePath + "plc.properties";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            plcConfig.load(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
    
    /**
     * Get robot configuration property
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    public String getRobotProperty(String key, String defaultValue) {
        return robotConfig.getProperty(key, defaultValue);
    }
    
    /**
     * Get PLC configuration property
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    public String getPlcProperty(String key, String defaultValue) {
        return plcConfig.getProperty(key, defaultValue);
    }
    
    /**
     * Get robot configuration property as integer
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value as integer or default
     */
    public int getRobotPropertyInt(String key, int defaultValue) {
        String value = robotConfig.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get PLC configuration property as integer
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value as integer or default
     */
    public int getPlcPropertyInt(String key, int defaultValue) {
        String value = plcConfig.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
