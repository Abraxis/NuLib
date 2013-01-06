package org.abraxis.nulib;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class LogTest
{
	@Test
	public void testGetLogger() throws Exception
	{
		Logger logger = Log.getLogger(this.getClass());

		logger.error("Testing error message, date is: {}", new Date().toString());
		logger.warn("Testing exception", new Exception("Testing exception"));
		logger.info("Testing info message");
		logger.debug("Testing debug message");

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);
	}
}
