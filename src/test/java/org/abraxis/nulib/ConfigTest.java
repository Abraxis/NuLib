package org.abraxis.nulib;

import org.junit.Test;

public class ConfigTest
{
	@Test
	public void testGetInstance() throws Exception
	{
		Config cfg = Config.getInstance();
		assert (cfg != null);
		assert (!cfg.getProperty(Bus.MQ_HOST_CONFIG_KEY).isEmpty());
	}

	@Test
	public void testSetProperty() throws Exception
	{
		Config cfg = Config.getInstance();
		String key = Bus.MQ_HOST_CONFIG_KEY;
		String oldVal = cfg.getProperty(key);
		assert (!oldVal.isEmpty());
		String newVal = oldVal + "_2";
		cfg.setProperty(key, newVal);
		assert (cfg.getProperty(key).equals(newVal));
		cfg.setProperty(key, oldVal);
		assert (cfg.getProperty(key).equals(oldVal));
	}
}
