package org.abraxis.nulib;

import org.junit.Test;

import java.io.IOException;

public class BusTest
{
	@Test
	public void testEmitMessage() throws Exception
	{
		Bus bus = new Bus();
		String testQueue = "/nu/test_queue";
		String msg = "msg";
		bus.emitMessage(testQueue, msg);
	}

	@Test
	public void testCloseConnection() throws Exception
	{
		Bus bus = new Bus();
		bus.closeConnection();
	}

	@Test
	public void testGetMessage() throws Exception
	{
		final String testQueue = "/test/queue";
		final String msg = "msg";

		Runnable recThread = new Runnable()
		{
			@Override
			public void run()
			{
				Bus busRecv = new Bus();
				String msgRec = null;
				try {
					msgRec = busRecv.getMessage(testQueue);
				} catch (Exception e) {
					e.printStackTrace();
					assert(false);
				}
				assert(msg.equals(msgRec));
			}
		};

		Thread th = new Thread(recThread);
		th.start();
		Bus busSend = new Bus();
		busSend.emitMessage(testQueue, msg, "KEY");
		th.join();
	}

	@Test
	public void testGetMessages() throws Exception
	{
		final String testQueue = "/test/topic";
		final String msg = "msg";

		Runnable recThread = new Runnable()
		{
			@Override
			public void run()
			{
				Bus busRecv = new Bus();
				String msgRec = null;
				try {
					msgRec = busRecv.getMessage(testQueue);
				} catch (Exception e) {
					e.printStackTrace();
					assert(false);
				}
				assert(msg.equals(msgRec));
			}
		};

		Thread th1 = new Thread(recThread);
		th1.start();
		Thread th2 = new Thread(recThread);
		th2.start();
		Bus busSend = new Bus();
		busSend.emitMessage(testQueue, msg, "KEY");
		th1.join();
		th2.join();
	}
}
