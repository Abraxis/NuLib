package org.abraxis.nulib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{

	public static Logger getLogger(Class c)
	{
		Logger logger = LoggerFactory.getLogger(c);

		return logger;
	}

}
