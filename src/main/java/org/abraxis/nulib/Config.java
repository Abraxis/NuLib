package org.abraxis.nulib;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class Config
{
	private Logger logger = Log.getLogger(this.getClass());
	private static Config instance = new Config();
	private File configFile;
	private boolean defaultConfigFile = false;
	private HierarchicalINIConfiguration iniConfig;

	public Config()
	{
		this(null);
	}

	public Config(File configFile)
	{
		if (configFile == null) {
			logger.debug("Loading with default config file");
			readDefaultConfig();
		} else {
			this.configFile = configFile;
			logger.debug("Loading with config file: {}", configFile.getAbsolutePath());
			readFileConfig();
		}
		printDebugFile();
	}

	private void readDefaultConfig()
	{
		this.defaultConfigFile = true;
		URL url = this.getClass().getClassLoader().getResource("default.conf");
		logger.debug("Default config url: {}", url);
		try {
			this.iniConfig = new HierarchicalINIConfiguration(url);
		} catch (ConfigurationException ex) {
			logger.error("Failed to read default config! Should never happen!", ex);
		}
	}

	private void readFileConfig()
	{
		try {
			this.iniConfig = new HierarchicalINIConfiguration(configFile);
		} catch (ConfigurationException ex) {
			logger.error("Failed to read config file, trying to read default config...", ex);
			readDefaultConfig();
		}
	}

	private void printDebugFile()
	{
		logger.debug("Debugging config file - START");
		Set setOfSections = iniConfig.getSections();
		Iterator sectionNames = setOfSections.iterator();

		while (sectionNames.hasNext()) {
			String sectionName = sectionNames.next().toString();
			SubnodeConfiguration sObj = iniConfig.getSection(sectionName);
			Iterator it1 = sObj.getKeys();
			while (it1.hasNext()) {
				// Get element
				Object key = it1.next();
				logger.debug("Key: " + sectionName + "." + key + " = " + sObj.getString(key.toString()));
			}
		}
		logger.debug("Debugging config file - END");
	}

	private void saveConfig()
	{
		// TODO!
	}

	public static Config getInstance()
	{
		return instance;
	}

	public String getProperty(String key)
	{
		String ret = (String) iniConfig.getProperty(key);
		logger.debug("Reading settings: {} = {}", key, ret);
		return ret;
	}

	public void setProperty(String key, String val)
	{
		logger.info("Setting settings: {} = {}", key, val);
		iniConfig.setProperty(key, val);
		if (this.defaultConfigFile) {
			logger.warn("Trying to set property on default config file, not saving file.");
		} else {
			try {
				logger.debug("Saving config file");
				iniConfig.save();
			} catch (ConfigurationException e) {
				logger.error("Failed to save config file");
			}
		}
	}
}