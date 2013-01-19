package org.abraxis.nulib;

import com.rabbitmq.client.QueueingConsumer;
import org.junit.Test;

public class EventTest
{
	@Test
	public void testToJSON() throws Exception
	{
		String msg = "JSON serialization test msg, unicode: +ěščřžýáíé=úůÄäüöß";
		EventType et = EventType.STORAGE_REMOVED;
		Event ev = new Event(et);
		ev.setMessage(msg);
		String json = ev.toJSON();
		Event evNew = Event.fromJSON(json);
		assert(evNew.getEventType() == et);
		assert(msg.equals(evNew.getMessage()));
	}

	@Test
	public void testEmit() throws Exception
	{
		final String msg = "EmitEvent test msg, unicode: +ěščřžýáíé=úůÄäüöß";
		final EventType et = EventType.STORAGE_REMOVED;
		final Event ev = new Event(et);
		ev.setMessage(msg);

		Runnable recThread = new Runnable()
		{
			@Override
			public void run()
			{
				Bus busRecv = new Bus();
				String msgRec = null;
				try {
					QueueingConsumer consumer = busRecv.subscribeToEvents();
					msgRec = busRecv.getEvent(consumer);
				} catch (Exception e) {
					e.printStackTrace();
					assert (false);
				}
				Event evRec = Event.fromJSON(msgRec);
				assert (msg.equals(evRec.getMessage()));
				assert (evRec.getEventType() == et);
			}
		};

		Thread th = new Thread(recThread);
		th.start();
		Bus busSend = new Bus();
		Thread.sleep(1000);     // To make sure that subscriber is registered
		ev.emit();
		th.join();
	}
}
