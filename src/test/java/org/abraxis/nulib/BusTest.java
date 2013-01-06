package org.abraxis.nulib;

import com.rabbitmq.client.QueueingConsumer;
import org.junit.Test;
import sun.security.pkcs11.wrapper.CK_SSL3_KEY_MAT_OUT;

public class BusTest
{
	@Test
	public void testEmitMessage() throws Exception
	{
		Bus bus = new Bus();
		String testQueue = "/nu/test_queue";
		String msg = "msg";
		bus.emitEvent(testQueue, msg);
	}

	@Test
	public void testCloseConnection() throws Exception
	{
		Bus bus = new Bus();
		bus.closeConnection();
	}

	@Test
	public void testGetEvent() throws Exception
	{
		final String msg = "msg, unicode: +ěščřžýáíé=úůÄäüöß";

		Runnable recThread = new Runnable()
		{
			@Override
			public void run()
			{
				Bus busRecv = new Bus();
				String msgRec = null;
				try {
					msgRec = busRecv.simpleGetEvent();
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
		Thread.sleep(1000);     // To make sure that subscriber is registered
		busSend.emitEvent(msg, "KEY");
		th.join();
	}

	@Test
	public void testGetEvents() throws Exception
	{
		final String msg = "msg, unicode: +ěščřžýáíé=úůÄäüöß";

		Runnable recThread = new Runnable()
		{
			@Override
			public void run()
			{
				Bus busRecv = new Bus();
				String msgRec = null;
				try {
					msgRec = busRecv.simpleGetEvent();
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
		Thread.sleep(1000);     // To make sure that subscribers are registered
		busSend.emitEvent(msg, "KEY");
		th1.join();
		th2.join();
	}

	@Test
	public void testRPC() throws Exception
	{
		final String request = "msg, unicode: +ěščřžýáíé=úůÄäüöß";
		final String expectedReply = request + request;
		final String service = "testService";

		Runnable recThread = new Runnable()
		{
			@Override
			public void run()
			{
				Integer req = -1;
				String body = "";
				Bus busRecv = new Bus();
				try {
					QueueingConsumer consumer = busRecv.subscribeToRPC(new String[]{service});
					QueueingConsumer.Delivery delivery = busRecv.getRPCRequest(consumer);
					assert(delivery != null);
					assert(delivery.getBody() != null);
					body = new String(delivery.getBody());
					assert(!body.isEmpty());
					String reply = body + body;
					busRecv.sendRPCReply(
							reply,
							delivery.getProperties().getCorrelationId(),
							delivery.getProperties().getReplyTo()
							);
				} catch (Exception e) {
					e.printStackTrace();
					assert(false);
				}
				assert(request.equals(body));
			}
		};

		Thread th = new Thread(recThread);
		th.start();
		Bus busSend = new Bus();
		Thread.sleep(1000);     // To make sure that subscriber is registered
		String reply = busSend.callRPC(service, request);
		th.join();
		assert(expectedReply.equals(reply));
	}
}
